package com.uni.remote.tech

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import com.uni.remote.tech.admob.admob
import com.uni.remote.tech.billing.billing
import com.uni.remote.tech.billing.model.IAPProduct
import com.uni.remote.tech.billing.model.IAPProductType
import com.uni.remote.tech.common.RemoteBilling
import com.uni.remote.tech.features.launcher.LauncherActivity
import com.uni.remote.tech.utils.AdsConstants
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        instance = this

        configureFrameworks()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    override val workManagerConfiguration: Configuration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    private fun configureFrameworks() {
        RemoteBilling.billing.configure(
            iapProducts = listOf(
                IAPProduct(
                    productType = IAPProductType.Subscription,
                    productId = resources.getString(R.string.billing_sub_week)
                ),
                IAPProduct(
                    productType = IAPProductType.Subscription,
                    productId = resources.getString(R.string.billing_sub_month)
                ),
                IAPProduct(
                    productType = IAPProductType.Subscription,
                    productId = resources.getString(R.string.billing_sub_year)
                ),
                IAPProduct(
                    productType = IAPProductType.InApp,
                    productId = resources.getString(R.string.billing_inapp_onetime)
                )
            )
        )

        RemoteBilling.admob.configure(
            adsConstants = AdsConstants,
            disableAppOpenAdActivities = listOf(
                LauncherActivity::class.java,
//                SubscriptionActivity::class.java
            )
        )
    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}
