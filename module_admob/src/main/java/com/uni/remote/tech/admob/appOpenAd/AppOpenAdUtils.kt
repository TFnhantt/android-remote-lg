package com.uni.remote.tech.admob.appOpenAd

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.uni.remote.tech.admob.AdsManager
import com.uni.remote.tech.admob.base.BaseAdUtils
import com.uni.remote.tech.admob.ump.GoogleMobileAdsConsentManager
import com.uni.remote.tech.common.premium.IPremiumManager
import timber.log.Timber
import java.util.Date

class AppOpenAdUtils internal constructor(
    private val appOpenAdId: String,
    private val application: Application,
    private val adsManager: AdsManager,
    private val premiumManager: IPremiumManager,
    private val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager,
    private val disableAppOpenAdActivities: List<Class<*>>
) : BaseAdUtils(premiumManager),
    DefaultLifecycleObserver {
    private val activityLifecycleCallbacks = AdActivityLifecycleCallbacks()
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var countLoadAppOpenAds = 0
    private var isPendingShowAd = false
    internal var isShowingAd = false

    /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
    private var loadTime: Long = 0

    init {
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * Load an ad.
     */
    internal fun loadAd() {
        if (!appAllowShowAd) {
            Timber.d("App not allow to show ad.")
            return
        }

        if (!googleMobileAdsConsentManager.canRequestAds) {
            Timber.d("Mobile Ads consent manager cannot request ads.")
            return
        }

        // Do not load ad if there is an unused ad or one is already loading.
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        ++countLoadAppOpenAds
        isLoadingAd = true

        AppOpenAd.load(
            application,
            appOpenAdId,
            defaultAdRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    super.onAdLoaded(ad)
                    Timber.d("Ad was loaded.")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    countLoadAppOpenAds = 0
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    Timber.e("Ad failed to load, domain: ${adError.domain}, code: ${adError.code}, message: ${adError.message}")
                    isLoadingAd = false
                    if (countLoadAppOpenAds < MAX_RETRY_LOAD_AD) {
                        loadAd()
                    }
                }
            }
        )
    }

    /** Check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown. */
    fun isAdAvailable(): Boolean = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(
        activity: AppCompatActivity,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Timber.d("The app open ad is already showing.")
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Timber.d("Ad dismissed full screen content.")
                appOpenAd = null
                isShowingAd = false
                adsManager.updateIsShowingFullScreenAd(false)
                adsManager.updateLastTimeShowOpenAd(System.currentTimeMillis())
                onShowAdCompleteListener.onShowAdComplete()
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Timber.e("Ad failed to show full screen content: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                adsManager.updateIsShowingFullScreenAd(false)
                onShowAdCompleteListener.onShowAdComplete()
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                Timber.d("Ad showed full screen content")
                isShowingAd = true
                adsManager.updateIsShowingFullScreenAd(true)
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }
        }

        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Timber.d("Will show ad.")
            appOpenAd?.show(activity)
//            isShowingAd = true
//            adsManager.updateIsShowingFullScreenAd(true)
        } else {
            Timber.d("The ad can not be shown when app is not in foreground.")
        }
    }

    fun setPendingShowAd(isPending: Boolean) {
        isPendingShowAd = isPending
    }

    /**
     * [DefaultLifecycleObserver]
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Timber.d("onStart")

        if (isPendingShowAd) {
            Timber.d("App was pending show ad.")
            return
        }

        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Timber.d("The app open ad is already showing.")
            return
        }

        // Check that another full screen ad are being displayed, do not show app open ad over overlapping full screen ad.
        if (adsManager.checkIsShowingFullScreenAd()) {
            Timber.d("Another full screen ad are being displayed. Do not show app open ad over overlapping")
            return
        }

        if (!appAllowShowAd) {
            Timber.d("App not allow to show ad.")
            return
        }

        // If the app open ad is not available yet, invoke the callback.
        if (!isAdAvailable()) {
            Timber.d("The app open ad is not ready yet.")
            loadAd()
            return
        }

        activityLifecycleCallbacks.currentActivity?.let { currentActivity ->
            if (disableAppOpenAdActivities.any { it.name == currentActivity.javaClass.name }) {
                Timber.d("${currentActivity.javaClass.name} disabled show app open ad.")
            } else {
                showWelcomeBackScreen(currentActivity)
            }
        }
    }

    private inner class AdActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        var currentActivity: Activity? = null

        override fun onActivityCreated(
            activity: Activity,
            bundle: Bundle?
        ) {
        }

        override fun onActivityStarted(activity: Activity) {
            Timber.d("onActivityStarted: $activity")
            currentActivity = activity
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
            Timber.d("onActivityStopped: $activity")
        }

        override fun onActivitySaveInstanceState(
            activity: Activity,
            bundle: Bundle
        ) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            Timber.d("onActivityDestroyed: $activity")
            currentActivity = null
        }
    }

    private fun showWelcomeBackScreen(activity: Activity) {
        activity.startActivity(Intent(activity, WelcomeBackActivity::class.java))
    }

    companion object {
        private const val MAX_RETRY_LOAD_AD = 3
    }
}
