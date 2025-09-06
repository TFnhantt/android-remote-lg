package com.uni.remote.tech.features.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.google.android.gms.ads.nativead.NativeAd
import com.uni.remote.tech.R
import com.uni.remote.tech.base.BaseActivity
import com.uni.remote.tech.common.extension.flow.collectIn
import com.uni.remote.tech.common.utils.inAppReview.showInAppReview
import com.uni.remote.tech.databinding.ActivityMainBinding
import com.uni.remote.tech.extensions.safeClickListener
import com.uni.remote.tech.features.subscription.SubscriptionActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override val viewModel: MainViewModel by viewModels()

    private var nativeAd: NativeAd? = null

    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        showInAppReview()
    }

    private fun initListener() {
        binding.mbInterstitial.safeClickListener {
            showInterstitialAd {
                // Callback
            }
        }
        binding.mbNative.safeClickListener {
            loadSingleNativeAd(
                adId = resources.getString(R.string.ads_native_id),
                onAdLoaded = {
                    nativeAd?.destroy()
                    nativeAd = it
                    binding.nativeView.populate(it)
                    updateLastTimeLoadNativeAd(System.currentTimeMillis())
                }
            )
        }
        binding.mbBanner.safeClickListener {
            loadBannerAd(adview = binding.bannerView, adId = getString(R.string.admob_banner_id))
        }
        binding.mbPremium.safeClickListener {
            startActivity(Intent(this, SubscriptionActivity::class.java))
        }
    }

    private fun bindViewModel() {
        viewModel.hasPurchased.collectIn(this) {
            binding.nativeView.isVisible = !it
            binding.bannerView.isVisible = !it
            if (!it) {

            }
        }
    }

    override fun onDestroy() {
        nativeAd?.destroy()
        super.onDestroy()
    }

    override fun setupViewBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(inflater)
}