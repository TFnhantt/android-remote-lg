package com.uni.remote.tech.admob.rewardedAd

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.uni.remote.tech.admob.AdsManager
import com.uni.remote.tech.admob.PrepareLoadingAdsDialog
import com.uni.remote.tech.admob.base.BaseAdUtils
import com.uni.remote.tech.admob.ump.GoogleMobileAdsConsentManager
import com.uni.remote.tech.common.premium.IPremiumManager
import timber.log.Timber

class RewardedAdUtils internal constructor(
    private val rewardedAdId: String,
    private val adsManager: AdsManager,
    private val premiumManager: IPremiumManager,
    private val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
) : BaseAdUtils(premiumManager) {
    private var prepareLoadingAdsDialog: PrepareLoadingAdsDialog? = null

    fun showRewardedAd(
        activity: AppCompatActivity,
        onAdShowed: () -> Unit,
        onAdDismissed: () -> Unit,
        onLoadFailed: () -> Unit,
        onRewarded: (RewardItem) -> Unit
    ) {
        if (ProcessLifecycleOwner
                .get()
                .lifecycle.currentState
                .isAtLeast(Lifecycle.State.RESUMED)
        ) {
            adsManager.updateIsShowingFullScreenAd(true)

            try {
                if (prepareLoadingAdsDialog != null && prepareLoadingAdsDialog!!.isShowing) prepareLoadingAdsDialog!!.dismiss()
                prepareLoadingAdsDialog = PrepareLoadingAdsDialog(activity)
                prepareLoadingAdsDialog?.show()
            } catch (e: Exception) {
                prepareLoadingAdsDialog = null
                e.printStackTrace()
            }

            loadRewardedAd(
                context = activity,
                onAdLoaded = { rewardedAd ->
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    if (prepareLoadingAdsDialog != null && prepareLoadingAdsDialog!!.isShowing && !activity.isDestroyed) prepareLoadingAdsDialog!!.dismiss()
                                },
                                1000
                            )

                            rewardedAd.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        Timber.d("Ad dismissed full screen content.")
                                        adsManager.updateIsShowingFullScreenAd(false)
                                        onAdDismissed()
                                    }

                                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                        Timber.e("Ad failed to show full screen content: ${adError.message}")
                                        adsManager.updateIsShowingFullScreenAd(false)
                                        onLoadFailed()
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        Timber.d("Ad showed fullscreen content.")
                                        onAdShowed()
                                    }
                                }
                            rewardedAd.show(activity) {
                                Timber.d("User earned the reward.")
                                onRewarded(it)
                            }
                        } else {
                            if (prepareLoadingAdsDialog != null && prepareLoadingAdsDialog!!.isShowing && !activity.isDestroyed) prepareLoadingAdsDialog!!.dismiss()
                            adsManager.updateIsShowingFullScreenAd(false)
                            onLoadFailed()
                        }
                    }, 500)
                },
                onLoadFailed = {
                    if (prepareLoadingAdsDialog != null && prepareLoadingAdsDialog!!.isShowing && !activity.isDestroyed) prepareLoadingAdsDialog!!.dismiss()
                    adsManager.updateIsShowingFullScreenAd(false)
                    onLoadFailed()
                }
            )
        } else {
            onLoadFailed()
        }
    }

    private fun loadRewardedAd(
        context: Context,
        onAdLoaded: (RewardedAd) -> Unit,
        onLoadFailed: (() -> Unit)?
    ) {
        if (!googleMobileAdsConsentManager.canRequestAds) {
            Timber.d("Mobile Ads consent manager cannot request ads.")
            onLoadFailed?.invoke()
            return
        }

        RewardedAd.load(
            context,
            rewardedAdId,
            defaultAdRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Timber.e("Ad failed to load, domain: ${adError.domain}, code: ${adError.code}, message: ${adError.message}.")
                    onLoadFailed?.invoke()
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Timber.d("Ad was loaded.")
                    onAdLoaded(rewardedAd)
                }
            }
        )
    }
}
