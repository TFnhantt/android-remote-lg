package com.uni.remote.tech.billing.manager

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.uni.remote.tech.billing.listener.BillingClientListener
import com.uni.remote.tech.billing.listener.PurchaseServiceListener
import com.uni.remote.tech.billing.listener.SubscriptionServiceListener
import com.uni.remote.tech.billing.model.IAPProduct
import com.uni.remote.tech.billing.model.PurchaseInfo

abstract class IBillingManager internal constructor() {
    private val billingClientListeners = mutableListOf<BillingClientListener>()
    private val purchaseServiceListeners = mutableListOf<PurchaseServiceListener>()
    private val subscriptionServiceListeners = mutableListOf<SubscriptionServiceListener>()

    abstract fun configure(iapProducts: List<IAPProduct>)

    abstract fun buyBasePlan(activity: Activity, product: IAPProduct)

    abstract fun isConnected(): Boolean

    abstract fun getPricedProducts(): List<IAPProduct>

    fun addBillingClientListener(billingClientListener: BillingClientListener) {
        billingClientListeners.add(billingClientListener)
    }

    fun removeBillingClientListener(billingClientListener: BillingClientListener) {
        billingClientListeners.remove(billingClientListener)
    }

    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        purchaseServiceListeners.add(purchaseServiceListener)
    }

    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        purchaseServiceListeners.remove(purchaseServiceListener)
    }

    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        subscriptionServiceListeners.add(subscriptionServiceListener)
    }

    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        subscriptionServiceListeners.remove(subscriptionServiceListener)
    }

    internal fun isBillingClientConnected(isConnected: Boolean, responseCode: Int) {
        findUiHandler().post {
            billingClientListeners.forEach { billingClientConnectionListener ->
                billingClientConnectionListener.onConnected(isConnected, responseCode)
            }
        }
    }

    internal fun productDetailsUpdated(products: List<IAPProduct>) {
        findUiHandler().post {
            billingClientListeners.forEach { billingClientConnectionListener ->
                billingClientConnectionListener.onQueryProductDetailComplete(products)
            }
        }
    }

    internal fun productOwned(purchaseInfo: PurchaseInfo, isRestore: Boolean) {
        findUiHandler().post {
            purchaseServiceListeners.forEach { purchaseServiceListener ->
                if (isRestore) {
                    purchaseServiceListener.onProductRestored(purchaseInfo)
                } else {
                    purchaseServiceListener.onProductPurchased(purchaseInfo)
                }
            }
        }
    }

    internal fun productPurchasePending(purchaseInfo: PurchaseInfo) {
        findUiHandler().post {
            purchaseServiceListeners.forEach { purchaseServiceListener ->
                purchaseServiceListener.onProductPurchasePending(purchaseInfo)
            }
        }
    }

    internal fun subscriptionOwned(purchaseInfo: PurchaseInfo, isRestore: Boolean) {
        findUiHandler().post {
            subscriptionServiceListeners.forEach { subscriptionServiceListener ->
                if (isRestore) {
                    subscriptionServiceListener.onSubscriptionRestored(purchaseInfo)
                } else {
                    subscriptionServiceListener.onSubscriptionPurchased(purchaseInfo)
                }
            }
        }
    }

    internal fun subscriptionPurchasePending(purchaseInfo: PurchaseInfo) {
        findUiHandler().post {
            subscriptionServiceListeners.forEach { subscriptionServiceListener ->
                subscriptionServiceListener.onSubscriptionPurchasePending(purchaseInfo)
            }
        }
    }

    internal fun launchBillingFlowComplete(isSuccess: Boolean) {
        findUiHandler().post {
            billingClientListeners.forEach { billingClientListener ->
                billingClientListener.onLaunchPurchaseComplete(isSuccess)
            }
        }
    }

    internal fun close() {
        billingClientListeners.clear()
        purchaseServiceListeners.clear()
        subscriptionServiceListeners.clear()
    }
}

fun findUiHandler(): Handler = Handler(Looper.getMainLooper())
