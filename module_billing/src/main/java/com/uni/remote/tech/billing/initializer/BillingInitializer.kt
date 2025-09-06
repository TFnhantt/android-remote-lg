package com.uni.remote.tech.billing.initializer

import android.content.Context
import androidx.startup.Initializer
import com.uni.remote.tech.billing.manager.BillingManager
import com.uni.remote.tech.common.RemoteBilling
import com.uni.remote.tech.common.premium
import com.uni.remote.tech.common.premium.PremiumInitializer
import com.uni.remote.tech.common.utils.timber.TimberInitializer
import timber.log.Timber

/**
 * BillingInitializer is a class that initializes the BillingManager instance as part of the application startup process.
 */
class BillingInitializer : Initializer<BillingManager> {
    override fun create(context: Context): BillingManager = BillingManager
        .initialize(
            applicationContext = context,
            premiumManager = RemoteBilling.premium
        ).apply {
            Timber.tag("Initializer").d("BillingManager initialized successfully!")
        }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf(
        TimberInitializer::class.java,
        PremiumInitializer::class.java
    )
}
