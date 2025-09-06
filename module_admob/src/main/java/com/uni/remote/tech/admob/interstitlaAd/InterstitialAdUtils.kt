package com.uni.remote.tech.admob.interstitlaAd

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.uni.remote.tech.admob.AdsManager
import com.uni.remote.tech.admob.PrepareLoadingAdsDialog
import com.uni.remote.tech.admob.base.BaseAdUtils
import com.uni.remote.tech.admob.ump.GoogleMobileAdsConsentManager
import com.uni.remote.tech.common.premium.IPremiumManager
import timber.log.Timber

class InterstitialAdUtils internal constructor(
    private val interstitialAdId: String,
    private val context: Context,
    private val adsManager: AdsManager,
    private val premiumManager: IPremiumManager,
    private val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
) : BaseAdUtils(premiumManager) {
    private var mInterstitialAd: InterstitialAd? = null
    private var mAdIsLoading: Boolean = false
    private var isStartShowingAd = false
    private var prepareLoadingAdsDialog: PrepareLoadingAdsDialog? = null

    internal fun loadAd() {
        if (!appAllowShowAd) {
            Timber.d("App not allow to show ad.")
            return
        }

        if (!googleMobileAdsConsentManager.canRequestAds) {
            Timber.d("Mobile Ads consent manager cannot request ads.")
            return
        }

        InterstitialAd.load(
            context,
            interstitialAdId,
            defaultAdRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Timber.e("Ad failed to load, domain: ${adError.domain}, code: ${adError.code}, message: ${adError.message}.")
                    mInterstitialAd = null
                    mAdIsLoading = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Timber.d("Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mAdIsLoading = true
                }
            }
        )
    }

    fun showAd(
        activity: AppCompatActivity,
        onAdsShowed: () -> Unit,
        onAdsClosed: () -> Unit
    ) {
        /**
         * Trong 1 vài trường hợp, hàm showInterstitial được gọi liên tục khi chưa trả về callback
         * (vd: nhấp liên tục vào button để show ad)
         * trên các device yếu thì ad sẽ bị show chậm hơn dẫn đến callback trong fullScreenContentCallback chưa được gọi
         * sử dụng biến isStartShowingAd để tránh trường hợp này, khi hàm showInterstitial được gọi nếu vẫn đang thục hiện việc show ad thì bỏ qua, ko trả về callback
         */
        if (isStartShowingAd) return

        if (!appAllowShowAd) {
            Timber.d("App not allow to show ad.")
            onAdsClosed()
            return
        }

        if (mInterstitialAd != null) {
            /**
             * Kiểm tra xem có được phép show interstitial ad sau khi đã show app open ad hay không
             * Mặc định sẽ là 60 giây sau khi show app open ad thì sẽ được show interstitial ad
             */
            if (System.currentTimeMillis() - adsManager.getLastTimeShowOpenAd() < adsManager.getTimeIntervalShowFullAd().inWholeMilliseconds) {
                Timber.d("Skip show ad with time interval show full ad.")
                onAdsClosed.invoke()
                return
            }

            /**
             * Kiểm tra xem có được phép show interstitial ad giữa các lần với nhau hay không
             * Mặc định sẽ là 30 giây sau khi show interstitial ad thì sẽ được show interstitial ad tiếp theo
             */
            if (System.currentTimeMillis() - adsManager.getLastTimeShowInterstitialAd() < adsManager.getTimeIntervalShowInterstitialAd().inWholeMilliseconds) {
                Timber.d("Skip show ad with time interval show interstitial ad.")
                onAdsClosed.invoke()
                return
            }

            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Timber.d("Ad dismissed full screen content.")
                    mInterstitialAd = null
                    isStartShowingAd = false
                    adsManager.updateIsShowingFullScreenAd(false)
                    adsManager.updateLastTimeShowInterstitialAd(System.currentTimeMillis())
                    onAdsClosed()
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Timber.e("Ad failed to show full screen content: ${adError.message}")
                    mInterstitialAd = null
                    isStartShowingAd = false
                    adsManager.updateIsShowingFullScreenAd(false)
                    onAdsClosed()
                    loadAd()
                }

                override fun onAdShowedFullScreenContent() {
                    Timber.d("Ad showed fullscreen content.")
                    isStartShowingAd = false
                    onAdsShowed()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }
            }

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

                Handler(Looper.getMainLooper()).postDelayed({
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                if (prepareLoadingAdsDialog != null && prepareLoadingAdsDialog!!.isShowing && !activity.isDestroyed) prepareLoadingAdsDialog!!.dismiss()
                            },
                            1000
                        )
                        isStartShowingAd = true
                        if (mInterstitialAd != null) {
                            mInterstitialAd!!.show(activity)
                        } else {
                            adsManager.updateIsShowingFullScreenAd(false)
                            onAdsClosed()
                        }
                    } else {
                        if (prepareLoadingAdsDialog != null && prepareLoadingAdsDialog!!.isShowing && !activity.isDestroyed) prepareLoadingAdsDialog!!.dismiss()
                        adsManager.updateIsShowingFullScreenAd(false)
                        onAdsClosed()
                    }
                }, 500)
            } else {
                onAdsClosed()
            }
        } else {
            Timber.d("The interstitial ad wasn't ready yet.")
            loadAd()
            onAdsClosed()
        }
    }
}
