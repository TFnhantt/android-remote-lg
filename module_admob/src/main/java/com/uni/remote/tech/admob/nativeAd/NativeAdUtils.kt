package com.uni.remote.tech.admob.nativeAd

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import com.uni.remote.tech.admob.base.BaseAdUtils
import com.uni.remote.tech.admob.ump.GoogleMobileAdsConsentManager
import com.uni.remote.tech.common.premium.IPremiumManager
import timber.log.Timber

class NativeAdUtils internal constructor(
    private val nativeAdId: String,
    private val numberOfAdsToLoad: Int,
    private val context: Context,
    private val premiumManager: IPremiumManager,
    private val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
) : BaseAdUtils(premiumManager) {
    private val adLoader: AdLoader
    private val nativeAdMutableList = mutableListOf<NativeAd>()
    private val _nativeAdsFlow = MutableStateFlow<List<NativeAd>>(emptyList())
    val nativeAdsFlow = _nativeAdsFlow
        .combine(premiumManager.getSubscribedStateFlow()) { ads, purchased -> if (purchased) emptyList() else ads }

    init {
        val videoOptions = VideoOptions
            .Builder()
            .setStartMuted(true)
            .build()

        val nativeAdOptions = NativeAdOptions
            .Builder()
            .setVideoOptions(videoOptions)
            .build()

        val adListener = object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)
                Timber.e("Ad failed to load, domain: ${adError.domain}, code: ${adError.code}, message: ${adError.message}.")
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }
        }

        adLoader = AdLoader
            .Builder(context, nativeAdId)
            .forNativeAd { ad ->
                Timber.d("Loaded nativeAd = $ad.")
                nativeAdMutableList.add(ad)
                _nativeAdsFlow.tryEmit(nativeAdMutableList)
            }.withAdListener(adListener)
            .withNativeAdOptions(nativeAdOptions)
            .build()
    }

    internal fun loadAds() {
        if (!appAllowShowAd) {
            Timber.d("App not allow to show ad.")
            return
        }

        if (!googleMobileAdsConsentManager.canRequestAds) {
            Timber.d("Mobile Ads consent manager cannot request ads.")
            return
        }

        adLoader.loadAds(
            defaultAdRequest,
            numberOfAdsToLoad
        )
    }
}
