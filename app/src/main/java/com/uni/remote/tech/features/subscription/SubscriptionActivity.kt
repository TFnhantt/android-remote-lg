package com.uni.remote.tech.features.subscription

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uni.remote.tech.R
import com.uni.remote.tech.base.BaseActivity
import com.uni.remote.tech.billing.billing
import com.uni.remote.tech.billing.model.IAPProduct
import com.uni.remote.tech.common.RemoteBilling
import com.uni.remote.tech.common.premium
import com.uni.remote.tech.databinding.ActivitySubscriptionBinding
import com.uni.remote.tech.extensions.applyInsetsVerticalPadding
import com.uni.remote.tech.extensions.invisible
import com.uni.remote.tech.extensions.openWebPage
import com.uni.remote.tech.extensions.safeClickListener
import com.uni.remote.tech.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

typealias ProductWithSelection = Pair<IAPProduct, Boolean>

@AndroidEntryPoint
class SubscriptionActivity : BaseActivity<ActivitySubscriptionBinding, SubscriptionVM>() {

    private val productAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ProductAdapter(
            this
        ) { _, index ->
            selectedIndexS.tryEmit(index)
        }
    }

    private var selectedProduct: IAPProduct? = null
    private val selectedIndexS = MutableStateFlow(0)

    private val billingManager by lazy {
        RemoteBilling.billing
    }

    private val premiumManager by lazy {
        RemoteBilling.premium
    }

    override val viewModel: SubscriptionVM by viewModels()

    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        binding.root.applyInsetsVerticalPadding()

        binding.rvProductItems.run {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(this@SubscriptionActivity, RecyclerView.VERTICAL, false)
            adapter = productAdapter
            isNestedScrollingEnabled = false
        }

        lifecycleScope.launch {
            delay(3.seconds)
            binding.ivClose.visible()
        }
    }

    private fun initListener() {
        binding.tvPolicy.safeClickListener {
            openWebPage(getString(R.string.privacy_policy_url)) {}
        }

        binding.tvTermOfUse.safeClickListener {
            openWebPage(getString(R.string.term_of_use_url)) {}
        }

        binding.ivClose.safeClickListener {
            showBackwardInterstitial {
                finish()
            }
        }

        binding.tvContinue.safeClickListener {
            selectedProduct?.let { product ->
                billingManager.buyBasePlan(this@SubscriptionActivity, product)
            }
        }
    }

    private fun bindViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingManager.iapProductsFlow
                    .map { products -> products.filter { it.productDetails != null } }
                    .combine(selectedIndexS) { items, selectedIndex ->
                        items.mapIndexed { index, iapProduct ->
                            Pair(
                                iapProduct,
                                selectedIndex == index
                            )
                        }
                    }
                    .collect { products ->
                        selectedProduct =
                            products.firstOrNull { it.second }?.first?.also { product ->
                                if (product.hasFreeTrial) {
                                    binding.tvNoPaymentNow.visible()
                                } else {
                                    binding.tvNoPaymentNow.invisible()
                                }
                            }
                        productAdapter.submitList(products)
                    }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingManager.nonConsumePurchasesFlow
                    .filter { it.isNotEmpty() }
                    .mapToUnit()
                    .collect {
                        finishActivityResult(true)
                    }
            }
        }
    }

    override fun setupViewBinding(inflater: LayoutInflater) =
        ActivitySubscriptionBinding.inflate(inflater)

    private fun finishActivityResult(isBillingSuccess: Boolean) {
        setResult(if (isBillingSuccess) RESULT_OK else RESULT_CANCELED)
        finish()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun <T, R> Flow<T>.mapTo(value: R): Flow<R> =
        transform { return@transform emit(value) }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun <T> Flow<T>.mapToUnit(): Flow<Unit> = mapTo(Unit)

}