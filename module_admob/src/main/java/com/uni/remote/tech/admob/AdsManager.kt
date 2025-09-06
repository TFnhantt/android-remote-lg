package com.uni.remote.tech.admob

import android.app.Activity
import android.app.Application
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.uni.remote.tech.admob.appOpenAd.AppOpenAdUtils
import com.uni.remote.tech.admob.bannerAd.BannerAdUtils
import com.uni.remote.tech.admob.constants.AbstractAdsConstants
import com.uni.remote.tech.admob.interstitlaAd.InterstitialAdUtils
import com.uni.remote.tech.admob.nativeAd.NativeAdUtils
import com.uni.remote.tech.admob.nativeAd.SingleNativeAdUtils
import com.uni.remote.tech.admob.rewardedAd.RewardedAdUtils
import com.uni.remote.tech.admob.ump.GoogleMobileAdsConsentManager
import com.uni.remote.tech.common.premium.IPremiumManager
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AdsManager private constructor(
    private val application: Application,
    private val premiumManager: IPremiumManager
) {
    lateinit var appOpenAdUtils: AppOpenAdUtils
    lateinit var interstitialAdUtils: InterstitialAdUtils
    lateinit var backwardInterstitialAdUtils: InterstitialAdUtils
    lateinit var nativeAdUtils: NativeAdUtils
    lateinit var singleNativeAdUtils: SingleNativeAdUtils
    lateinit var bannerAdUtils: BannerAdUtils
    lateinit var rewardedAdUtils: RewardedAdUtils

    private var lastTimeShowInterstitialAd = 0L
    private var timeIntervalShowInterstitialAd: Duration =
        DEFAULT_TIME_INTERVAL_SHOW_INTERSTITIAL_AD

    private var lastTimeShowOpenAd = 0L
    private var timeIntervalShowFullAd: Duration =
        DEFAULT_TIME_INTERVAL_SHOW_FULL_AD

    private var isShowingFullScreenAd = AtomicBoolean(false)

    private val googleMobileAdsConsentManager =
        GoogleMobileAdsConsentManager.getInstance(application)
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)

    fun configure(
        adsConstants: AbstractAdsConstants,
        disableAppOpenAdActivities: List<Class<*>> = emptyList(),
        numberOfAdsToLoad: Int = DEFAULT_NUMBER_OF_NATIVE_AD
    ) {
        AudienceNetworkAds.initialize(application)

        appOpenAdUtils = AppOpenAdUtils(
            adsConstants.ADMOB_APP_OPEN_ID,
            application,
            this,
            premiumManager,
            googleMobileAdsConsentManager,
            disableAppOpenAdActivities
        )

        interstitialAdUtils = InterstitialAdUtils(
            adsConstants.ADMOB_INTERSTITIAL_ID,
            application,
            this,
            premiumManager,
            googleMobileAdsConsentManager
        )

        backwardInterstitialAdUtils = InterstitialAdUtils(
            adsConstants.ADMOB_BACKWARD_INTERSTITIAL_ID,
            application,
            this,
            premiumManager,
            googleMobileAdsConsentManager
        )

        nativeAdUtils = NativeAdUtils(
            adsConstants.ADMOB_NATIVE_ID,
            numberOfAdsToLoad,
            application,
            premiumManager,
            googleMobileAdsConsentManager
        )

        singleNativeAdUtils = SingleNativeAdUtils(
            premiumManager,
            googleMobileAdsConsentManager
        )

        bannerAdUtils = BannerAdUtils(
            adsConstants.ADMOB_BANNER_ID,
            premiumManager,
            googleMobileAdsConsentManager
        )

        rewardedAdUtils = RewardedAdUtils(
            adsConstants.ADMOB_REWARDED_ID,
            this,
            premiumManager,
            googleMobileAdsConsentManager
        )
    }

    fun gatherConsent(
        activity: Activity,
        listener: OnGatherConsentListener
    ) {
        googleMobileAdsConsentManager.gatherConsent(activity) { consentError ->
            if (consentError != null) {
                // Consent not obtained in current session.
                Timber.w(String.format("%s: %s", consentError.errorCode, consentError.message))
            }

            if (googleMobileAdsConsentManager.canRequestAds) {
                initializeMobileAdsSdk()
            }

            listener.onCompletion(consentError?.message)
        }
        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds) {
            initializeMobileAdsSdk()
        }
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(application)

            withContext(Dispatchers.Main) {
                // Load an ad on the main thread.
                appOpenAdUtils.loadAd()
                interstitialAdUtils.loadAd()
                backwardInterstitialAdUtils.loadAd()
            }
        }
    }

    fun updateTimeIntervalShowInterstitialAd(interval: Duration) {
        timeIntervalShowInterstitialAd = interval
    }

    internal fun getTimeIntervalShowInterstitialAd() = timeIntervalShowInterstitialAd

    internal fun updateLastTimeShowInterstitialAd(value: Long) {
        lastTimeShowInterstitialAd = value
    }

    internal fun getLastTimeShowInterstitialAd() = lastTimeShowInterstitialAd

    fun updateTimeIntervalShowFullAd(interval: Duration) {
        timeIntervalShowFullAd = interval
    }

    internal fun getTimeIntervalShowFullAd() = timeIntervalShowFullAd

    internal fun updateLastTimeShowOpenAd(value: Long) {
        lastTimeShowOpenAd = value
    }

    internal fun getLastTimeShowOpenAd() = lastTimeShowOpenAd

    internal fun checkIsShowingFullScreenAd() = isShowingFullScreenAd.get()

    internal fun updateIsShowingFullScreenAd(value: Boolean) {
        isShowingFullScreenAd.set(value)
    }

    interface OnGatherConsentListener {
        fun onCompletion(error: String?)
    }

    companion object {
        private val DEFAULT_TIME_INTERVAL_SHOW_INTERSTITIAL_AD: Duration = 30.seconds
        private val DEFAULT_TIME_INTERVAL_SHOW_FULL_AD: Duration = 60.seconds
        private const val DEFAULT_NUMBER_OF_NATIVE_AD: Int = 5

        @Volatile
        private var instance: AdsManager? = null

        /**
         * Returns the singleton instance of AdsManager.
         *
         * @return The AdsManager instance.
         */
        internal fun getInstance(): AdsManager =
            instance ?: synchronized(this) {
                instance ?: throw AssertionError("You have to call initialize first")
            }

        internal fun initialize(
            application: Application,
            premiumManager: IPremiumManager
        ): AdsManager {
            if (instance != null) throw AssertionError("You already initialized me")
            return AdsManager(application, premiumManager).also { instance = it }
        }
    }
}
