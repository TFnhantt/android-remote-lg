package com.uni.remote.tech.base

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.android.gms.ads.nativead.NativeAd
import com.uni.remote.tech.R
import com.uni.remote.tech.admob.admob
import com.uni.remote.tech.common.RemoteBilling
import com.uni.remote.tech.extensions.applyInsetsVerticalPadding
import com.uni.remote.tech.utils.AppConstants

abstract class BaseActivity<ViewBindingType : ViewBinding, ViewModelType : BaseViewModel> :
    AppCompatActivity(),
    ViewBindingHolder<ViewBindingType> by ViewBindingHolderImpl() {
    protected abstract val viewModel: ViewModelType

    protected val binding: ViewBindingType
        get() = requireBinding()

    private var lastTimeLoadNativeAd = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            initBinding(
                binding = setupViewBinding(layoutInflater),
                lifecycle = lifecycle,
                className = this::class.simpleName,
                onBound = null
            )
        )

        applyEdgeAndTransparentNav()
        binding.root.applyInsetsVerticalPadding()

        onBackPressedDispatcher.addCallback {
            showInterstitialAd {
                finish()
            }
        }

        init(savedInstanceState)
    }

    abstract fun init(savedInstanceState: Bundle?)

    abstract fun setupViewBinding(inflater: LayoutInflater): ViewBindingType

    fun applyEdgeAndTransparentNav() {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    fun showInterstitialAd(onAdsClosed: () -> Unit) {
        RemoteBilling.admob.interstitialAdUtils.showAd(
            this,
            onAdsShowed = {},
            onAdsClosed = {
                onAdsClosed()
            }
        )
    }

    fun showBackwardInterstitial(onAdsClosed: () -> Unit) {
        RemoteBilling.admob.backwardInterstitialAdUtils.showAd(
            this,
            onAdsShowed = {},
            onAdsClosed = {
                onAdsClosed()
            }
        )
    }

    fun loadSingleNativeAd(
        adId: String = resources.getString(R.string.ads_native_id),
        onAdLoaded: ((NativeAd) -> Unit)
    ) {
        if (System.currentTimeMillis() - lastTimeLoadNativeAd < AppConstants.DEFAULT_TIME_INTERVAL_LOAD_NATIVE_AD.inWholeMilliseconds) {
            return
        }

        RemoteBilling.admob.singleNativeAdUtils.loadAd(
            activity = this,
            adId = adId,
            numberOfAdsToLoad = 1,
            onLoadFailed = {},
            onAdLoaded = {
                onAdLoaded(it)
            }
        )
    }

    fun updateLastTimeLoadNativeAd(value: Long) {
        lastTimeLoadNativeAd = value
    }

    fun loadBannerAd(
        adview: FrameLayout,
        adId: String = resources.getString(R.string.ads_banner_id)
    ) {
        RemoteBilling.admob.bannerAdUtils.loadAdaptiveBanner(
            adViewContainer = adview,
            activity = this,
            adId = adId
        )
    }
}

