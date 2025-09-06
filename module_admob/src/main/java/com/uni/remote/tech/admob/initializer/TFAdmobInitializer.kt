package com.uni.remote.tech.admob.initializer

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.uni.remote.tech.admob.AdsManager
import com.uni.remote.tech.common.RemoteBilling
import com.uni.remote.tech.common.premium
import com.uni.remote.tech.common.premium.PremiumInitializer
import com.uni.remote.tech.common.utils.timber.TimberInitializer
import timber.log.Timber

class TFAdmobInitializer : Initializer<AdsManager> {
    override fun create(context: Context): AdsManager = AdsManager
        .initialize(
            application = context as Application,
            premiumManager = RemoteBilling.premium
        ).apply {
            Timber.tag("Initializer").d("AdsManager initialized successfully!")
        }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf(
        TimberInitializer::class.java,
        PremiumInitializer::class.java
    )
}
