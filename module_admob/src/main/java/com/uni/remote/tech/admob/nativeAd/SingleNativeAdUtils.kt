package com.uni.remote.tech.admob.nativeAd

import android.app.Activity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.uni.remote.tech.admob.base.BaseAdUtils
import com.uni.remote.tech.admob.ump.GoogleMobileAdsConsentManager
import com.uni.remote.tech.common.premium.IPremiumManager
import timber.log.Timber

class SingleNativeAdUtils internal constructor(
    private val premiumManager: IPremiumManager,
    private val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
) : BaseAdUtils(premiumManager) {
    fun loadAd(
        activity: Activity,
        numberOfAdsToLoad: Int,
        adId: String,
        onLoadFailed: () -> Unit,
        onAdLoaded: (NativeAd) -> Unit
    ) {
        if (!appAllowShowAd) {
            Timber.d("App not allow to show ad.")
            onLoadFailed()
            return
        }

        if (!googleMobileAdsConsentManager.canRequestAds) {
            Timber.d("Mobile Ads consent manager cannot request ads.")
            onLoadFailed()
            return
        }

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
                onLoadFailed()
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }
        }

        val adLoader = AdLoader
            .Builder(activity, adId)
            .forNativeAd { ad ->
                Timber.d("Loaded nativeAd = $ad.")
                // If this callback occurs after the activity is destroyed, you must call
                // destroy and return or you may get a memory leak.
                if (activity.isDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                    ad.destroy()
                    return@forNativeAd
                }

                onAdLoaded(ad)
            }.withAdListener(adListener)
            .withNativeAdOptions(nativeAdOptions)
            .build()

        if (numberOfAdsToLoad > 1) {
            adLoader.loadAds(defaultAdRequest, numberOfAdsToLoad)
        } else {
            adLoader.loadAd(defaultAdRequest)
        }
    }
}
