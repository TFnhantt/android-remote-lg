package com.uni.remote.tech.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.gms.ads.nativead.NativeAd
import com.uni.remote.tech.R
import com.uni.remote.tech.admob.admob
import com.uni.remote.tech.common.RemoteBilling
import com.uni.remote.tech.utils.AppConstants

abstract class BaseFragment<ViewBindingType : ViewBinding, ViewModelType : BaseViewModel> :
    Fragment(),
    ViewBindingHolder<ViewBindingType> by ViewBindingHolderImpl() {
    protected abstract val viewModel: ViewModelType

    private var _mContext: Context? = null

    protected val mContext: Context
        get() = requireNotNull(_mContext)

    protected val binding: ViewBindingType
        get() = requireBinding()

    private var lastTimeLoadNativeAd = 0L

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initBinding(
        binding = setupViewBinding(inflater, container),
        lifecycle = viewLifecycleOwner.lifecycle,
        className = this::class.simpleName,
        onBound = null
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    abstract fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ViewBindingType

    abstract fun init(savedInstanceState: Bundle?)

    override fun onDetach() {
        _mContext = null
        super.onDetach()
    }

    fun showInterstitialAd(onAdsClosed: () -> Unit) {
        (activity as? BaseActivity<*, *>)?.showInterstitialAd(
            onAdsClosed = onAdsClosed
        )
    }

    fun loadSingleNativeAd(
        adId: String = resources.getString(R.string.ads_native_id),
        onAdLoaded: ((NativeAd) -> Unit)
    ) {
        if (System.currentTimeMillis() - lastTimeLoadNativeAd < AppConstants.DEFAULT_TIME_INTERVAL_LOAD_NATIVE_AD.inWholeMilliseconds) {
            return
        }
        activity?.let { activity ->
            RemoteBilling.admob.singleNativeAdUtils.loadAd(
                activity = activity,
                adId = adId,
                numberOfAdsToLoad = 1,
                onLoadFailed = {},
                onAdLoaded = {
                    updateLastTimeLoadNativeAd(System.currentTimeMillis())
                    onAdLoaded(it)
                }
            )
        }
    }

    private fun updateLastTimeLoadNativeAd(value: Long) {
        lastTimeLoadNativeAd = value
    }

    fun loadBannerAd(
        adview: FrameLayout,
        adId: String = resources.getString(R.string.ads_banner_id)
    ) {
        activity?.let { activity ->
            RemoteBilling.admob.bannerAdUtils.loadAdaptiveBanner(
                adViewContainer = adview,
                activity = activity,
                adId = adId
            )
        }
    }
}
