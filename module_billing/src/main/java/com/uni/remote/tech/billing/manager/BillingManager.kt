package com.uni.remote.tech.billing.manager

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.uni.remote.tech.billing.extensions.biggestSubscriptionOfferDetailsToken
import com.uni.remote.tech.billing.manager.BillingManager.RetryPolicies.connectionRetryPolicy
import com.uni.remote.tech.billing.manager.BillingManager.RetryPolicies.resetConnectionRetryPolicyCounter
import com.uni.remote.tech.billing.model.IAPProduct
import com.uni.remote.tech.billing.model.IAPProductType
import com.uni.remote.tech.billing.model.PurchaseInfo
import com.uni.remote.tech.billing.model.priceAmountMicros
import com.uni.remote.tech.common.premium.IPremiumManager
import com.uni.remote.tech.common.utils.connectivity.ConnectivityObserver
import com.uni.remote.tech.common.utils.connectivity.NetworkObserver
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

class BillingManager private constructor(
    private val applicationContext: Context,
    private val premiumManager: IPremiumManager,
    private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : IBillingManager(),
    DefaultLifecycleObserver,
    PurchasesUpdatedListener,
    BillingClientStateListener {
    private lateinit var mBillingClient: BillingClient
    private var mIapProducts = emptyList<IAPProduct>()
    private var mIapProductMap = mapOf<String, IAPProduct>()
    private val networkObserver by lazy { NetworkObserver(applicationContext) }

    private val _iapProductsFlow = MutableStateFlow<List<IAPProduct>>(emptyList())
    val iapProductsFlow = _iapProductsFlow.asStateFlow()

    private val _nonConsumePurchasesFlow =
        MutableSharedFlow<List<Purchase>>(extraBufferCapacity = Int.MAX_VALUE)
    val nonConsumePurchasesFlow = _nonConsumePurchasesFlow.asSharedFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        externalScope.launch {
            networkObserver.observe()
                .collect { status ->
                    when (status) {
                        ConnectivityObserver.Status.Available -> {
                            if (isConnected() && mIapProductMap.isEmpty()) {
                                val pricedProducts = queryProductDetails()
                                productDetailsUpdated(pricedProducts)
                                _iapProductsFlow.emit(pricedProducts)
                            }
                        }

                        else -> {}
                    }
                }
        }
    }

    /**
     * [DefaultLifecycleObserver]
     */
    override fun onDestroy(owner: LifecycleOwner) {
        Timber.d("End billing client connection")
        if (::mBillingClient.isInitialized && mBillingClient.isReady) {
            mBillingClient.endConnection()
        }
        close()
    }

    override fun configure(iapProducts: List<IAPProduct>) {
        mIapProducts = iapProducts
        mBillingClient = BillingClient
            .newBuilder(applicationContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            ).build()

        connectToGooglePlayBillingService()
    }

    override fun isConnected() = ::mBillingClient.isInitialized && mBillingClient.isReady

    override fun buyBasePlan(activity: Activity, product: IAPProduct) {
        if (!product.isProductReady()) {
            Timber.d("Buy base plan: Product is not ready")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams
                .newBuilder()
                .setProductDetails(product.productDetails!!)
                .apply {
                    if (product.productDetails!!.productType == BillingClient.ProductType.SUBS) {
                        val selectedOfferToken =
                            product.productDetails!!.biggestSubscriptionOfferDetailsToken()?.offerToken
                                ?: ""
                        setOfferToken(selectedOfferToken)
                    }
                }.build()
        )

        val billingFlowParams = BillingFlowParams
            .newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        launchBillingFlow(activity, billingFlowParams)
    }

    override fun getPricedProducts(): List<IAPProduct> = mIapProductMap.map { it.value }

    /**
     * [BillingClientStateListener]
     * */

    override fun onBillingSetupFinished(result: BillingResult) {
        Timber.d("onBillingSetupFinished: ${result.responseCode} ${result.debugMessage}")
        if (result.isOk()) {
            isBillingClientConnected(true, result.responseCode)
            resetConnectionRetryPolicyCounter()
            externalScope.launch {
                val purchases = async { queryPurchases() }
                val products = async { queryProductDetails() }
                purchases.await()
                val pricedProducts = products.await()
                productDetailsUpdated(pricedProducts)
                _iapProductsFlow.emit(pricedProducts)
            }
        } else {
            isBillingClientConnected(false, result.responseCode)
        }
    }

    override fun onBillingServiceDisconnected() {
        // Reconnect to Service
        Timber.d("onBillingServiceDisconnected")
        connectionRetryPolicy { connectToGooglePlayBillingService() }
    }

    /**
     * [PurchasesUpdatedListener]
     * */

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Timber.d("On purchases updated: OK")
                if (!purchases.isNullOrEmpty()) {
                    externalScope.launch {
                        handlePurchases(
                            purchases = purchases.toSet(),
                            isRestore = false
                        )
                    }
                }
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Timber.d("On purchases updated: Item already owned")
                externalScope.launch { queryPurchases() }
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                Timber.d("On purchases updated: Service disconnected")
                connectToGooglePlayBillingService()
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.d("On purchases updated: User canceled")
            }

            else -> {
                Timber.d("On purchases updated: ${result.debugMessage}")
            }
        }
    }

    /**
     * Functions helper
     * */

    private fun connectToGooglePlayBillingService() {
        if (::mBillingClient.isInitialized && !mBillingClient.isReady) {
            Timber.d("Start connection!")
            mBillingClient.startConnection(this)
        } else {
            Timber.d("BillingClient not ready!")
        }
    }

    private suspend fun queryProductDetails(): List<IAPProduct> {
        val (productSubscriptions, productInApps) = mIapProducts
            .filter { it.productId.isNotEmpty() }
            .partition { it.productType == IAPProductType.Subscription }

        val pricedProducts = mutableListOf<IAPProduct>()

        if (productSubscriptions.isNotEmpty()) {
            val productDetailsResult =
                productSubscriptions.queryProductDetails(BillingClient.ProductType.SUBS)
            if (productDetailsResult.billingResult.isOk() && productDetailsResult.productDetailsList != null) {
                val productWithDetails = productSubscriptions
                    .joinBy(productDetailsResult.productDetailsList!!) { it.first.productId == it.second.productId }
                    .map { it.first.copy(productDetails = it.second) }
                pricedProducts.addAll(productWithDetails)
            }
        }

        if (productInApps.isNotEmpty()) {
            val productDetailsResult =
                productInApps.queryProductDetails(BillingClient.ProductType.INAPP)
            if (productDetailsResult.billingResult.isOk() && productDetailsResult.productDetailsList != null) {
                val productWithDetails = productInApps
                    .joinBy(productDetailsResult.productDetailsList!!) { it.first.productId == it.second.productId }
                    .map { it.first.copy(productDetails = it.second) }
                pricedProducts.addAll(productWithDetails)
            }
        }

        Timber.d("Query product details: $pricedProducts")

        return pricedProducts
            .filter { it.productDetails != null }
            .distinctBy { it.productId }
            .sortedByDescending { it.priceAmountMicros() }
            .sortedByDescending { it.hasFreeTrial }
            .apply { mIapProductMap = this.associateBy { it.productId } }
    }

    private suspend fun List<IAPProduct>.queryProductDetails(type: String): ProductDetailsResult {
        val queryProductList = this.map {
            QueryProductDetailsParams.Product
                .newBuilder()
                .setProductId(it.productId)
                .setProductType(type)
                .build()
        }
        return mBillingClient.queryProductDetails(
            QueryProductDetailsParams
                .newBuilder()
                .setProductList(queryProductList)
                .build()
        )
    }

    private suspend fun queryPurchases() {
        val purchasesResult = mutableSetOf<Purchase>()

        val inAppResult = mBillingClient.queryPurchasesAsync(
            QueryPurchasesParams
                .newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        if (inAppResult.billingResult.isOk()) {
            purchasesResult.addAll(inAppResult.purchasesList)
        } else {
            Timber.d("Query purchases: Query in-app purchases failed ${inAppResult.billingResult.debugMessage}")
        }

        val subsResult = mBillingClient.queryPurchasesAsync(
            QueryPurchasesParams
                .newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        if (subsResult.billingResult.isOk()) {
            purchasesResult.addAll(subsResult.purchasesList)
        } else {
            Timber.d("Query purchases: Query subscription purchases failed ${subsResult.billingResult.debugMessage}")
        }

        Timber.d("Query purchases: $purchasesResult")

        handlePurchases(purchases = purchasesResult, isRestore = true)
    }

    private suspend fun handlePurchases(purchases: Set<Purchase>, isRestore: Boolean) {
        val consumableProductIds = mIapProducts
            .filter { it.isConsumable }
            .map { it.productId }
            .distinct()

        val (consumables, nonConsumables) = purchases.partition { purchase ->
            purchase.products.any { consumableProductIds.contains(it) }
        }

        Timber.d("Handle purchases: \n - Consumable content: $consumables \n - Non consumable content: $nonConsumables")

        handleConsumablePurchases(consumables, isRestore)
        handleNonConsumablePurchases(nonConsumables, isRestore)
    }

    private suspend fun handleNonConsumablePurchases(
        purchases: List<Purchase>,
        isRestore: Boolean
    ) {
        val acknowledgedNonConsumablePurchases = mutableListOf<Purchase>()

        purchases.forEach { purchase ->
            if (purchase.isPurchased()) {
                if (!purchase.isAcknowledged) {
                    if (acknowledgePurchase(purchase)) {
                        acknowledgedNonConsumablePurchases.add(purchase)
                        subscriptionOwned(getPurchaseInfo(purchase), isRestore)
                        if (!isRestore) logSubscriptionPurchasedEvent(purchase)
                    }
                } else {
                    Timber.d("Acknowledge purchase: isAcknowledged $purchase")
                    acknowledgedNonConsumablePurchases.add(purchase)
                    subscriptionOwned(getPurchaseInfo(purchase), isRestore)
                    if (!isRestore) logSubscriptionPurchasedEvent(purchase)
                }
            } else if (purchase.isPending()) {
                subscriptionPurchasePending(getPurchaseInfo(purchase))
            }
        }

        Timber.d("Handle non consumable purchases: $acknowledgedNonConsumablePurchases")

        premiumManager.updateSubscribedState(acknowledgedNonConsumablePurchases.isNotEmpty())
        _nonConsumePurchasesFlow.emit(acknowledgedNonConsumablePurchases)
    }

    private suspend fun acknowledgePurchase(purchase: Purchase): Boolean {
        val ackPurchaseResult = mBillingClient.acknowledgePurchase(
            AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        )
        return if (ackPurchaseResult.isOk()) {
            Timber.d("Acknowledge purchase: OK $purchase")
            true
        } else {
            Timber.d("Acknowledge purchase: ${ackPurchaseResult.debugMessage}")
            false
        }
    }

    private suspend fun handleConsumablePurchases(purchases: List<Purchase>, isRestore: Boolean) {
        purchases.forEach { purchase ->
            if (purchase.isPurchased()) {
                consumePurchase(purchase)
                productOwned(getPurchaseInfo(purchase), isRestore)
            } else if (purchase.isPending()) {
                productPurchasePending(getPurchaseInfo(purchase))
            }
        }
    }

    private suspend fun consumePurchase(purchase: Purchase) {
        val consumeResult = mBillingClient.consumePurchase(
            ConsumeParams
                .newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        )
        if (consumeResult.billingResult.isOk()) {
            Timber.d("Consume purchase: OK $purchase")
        } else {
            Timber.d("Consume purchase: ${consumeResult.billingResult.debugMessage}")
        }
    }

    private fun getPurchaseInfo(purchase: Purchase): PurchaseInfo = PurchaseInfo(
        purchase.purchaseState,
        purchase.developerPayload,
        purchase.isAcknowledged,
        purchase.isAutoRenewing,
        purchase.orderId,
        purchase.originalJson,
        purchase.packageName,
        purchase.purchaseTime,
        purchase.purchaseToken,
        purchase.signature,
        purchase.products[0],
        purchase.accountIdentifiers
    )

    private fun launchBillingFlow(activity: Activity, params: BillingFlowParams) {
        val result = mBillingClient.launchBillingFlow(activity, params)
        launchBillingFlowComplete(result.isOk())
    }

    private fun logSubscriptionPurchasedEvent(purchase: Purchase) {
        mIapProductMap[purchase.products[0]]?.let { product ->
        }
    }

    private fun BillingResult.isOk(): Boolean =
        this.responseCode == BillingClient.BillingResponseCode.OK

    private fun BillingResult.canFailGracefully(): Boolean =
        this.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED

    private fun BillingResult.isRecoverableError(): Boolean = this.responseCode in setOf(
        BillingClient.BillingResponseCode.ERROR,
        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED
    )

    private fun BillingResult.isNonrecoverableError(): Boolean = this.responseCode in setOf(
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
        BillingClient.BillingResponseCode.DEVELOPER_ERROR
    )

    private fun BillingResult.isTerribleFailure(): Boolean = this.responseCode in setOf(
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
        BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
        BillingClient.BillingResponseCode.ITEM_NOT_OWNED,
        BillingClient.BillingResponseCode.USER_CANCELED
    )

    private fun Purchase.isPurchased(): Boolean =
        this.purchaseState == Purchase.PurchaseState.PURCHASED

    private fun Purchase.isPending(): Boolean = this.purchaseState == Purchase.PurchaseState.PENDING

    private fun IAPProduct.isProductReady(): Boolean =
        mIapProductMap.contains(this.productId) && this.productDetails != null

    private fun <T : Any, U : Any> List<T>.joinBy(
        collection: List<U>,
        filter: (Pair<T, U>) -> Boolean
    ): List<Pair<T, U?>> = map { t ->
        val filtered = collection.firstOrNull { filter(Pair(t, it)) }
        Pair(t, filtered)
    }

    /**
     * [RetryPolicies]
     * */

    private object RetryPolicies {
        private const val MAX_RETRY = 5
        private const val BASE_DELAY_MILLIS = 500
        private const val TASK_DELAY = 500L
        private var retryCounter = AtomicInteger(1)

        fun resetConnectionRetryPolicyCounter() {
            retryCounter.set(1)
        }

        /**
         * This works because it actually only makes one call. Then it waits for success or failure.
         * onSuccess it makes no more calls and resets the retryCounter to 1. onFailure another
         * call is made, until too many failures cause retryCounter to reach MAX_RETRY and the
         * policy stops trying. This is a safe algorithm: the initial calls to
         * connectToPlayBillingService from instantiateAndConnectToPlayBillingService is always
         * independent of the RetryPolicies. And so the Retry Policy exists only to help and never
         * to hurt.
         */
        fun connectionRetryPolicy(block: () -> Unit) {
            Timber.d("connectionRetryPolicy")
            val scope = CoroutineScope(Job() + Dispatchers.Default)
            scope.launch {
                val counter = retryCounter.getAndIncrement()
                if (counter < MAX_RETRY) {
                    val waitTime: Long = (2f.pow(counter) * BASE_DELAY_MILLIS).toLong()
                    delay(waitTime)
                    block()
                }
            }
        }

        /**
         * All this is doing is check that billingClient is connected and if it's not, request
         * connection, wait x number of seconds and then proceed with the actual task.
         */
        fun taskExecutionRetryPolicy(
            billingClient: BillingClient,
            listener: BillingClientStateListener,
            task: () -> Unit
        ) {
            val scope = CoroutineScope(Job() + Dispatchers.Default)
            scope.launch {
                if (!billingClient.isReady) {
                    Timber.d("taskExecutionRetryPolicy billing not ready")
                    billingClient.startConnection(listener)
                    delay(TASK_DELAY)
                }
                task()
            }
        }
    }

    companion object {
        @Volatile
        private var instance: BillingManager? = null

        /**
         * Returns the singleton instance of BillingManager.
         *
         * @return The BillingManager instance.
         */
        internal fun getInstance(): BillingManager = instance ?: synchronized(this) {
            instance ?: throw AssertionError("You have to call initialize first")
        }

        internal fun initialize(
            applicationContext: Context,
            premiumManager: IPremiumManager
        ): BillingManager {
            if (instance != null) throw AssertionError("You already initialized me")
            return BillingManager(
                applicationContext,
                premiumManager
            ).also { instance = it }
        }
    }
}
