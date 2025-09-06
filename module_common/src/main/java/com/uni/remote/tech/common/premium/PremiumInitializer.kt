package com.uni.remote.tech.common.premium

import android.content.Context
import androidx.startup.Initializer
import com.chibatching.kotpref.initializer.KotprefInitializer
import timber.log.Timber

class PremiumInitializer : Initializer<IPremiumManager> {
    override fun create(context: Context): IPremiumManager {
        return PremiumManager.INSTANCE.apply {
            Timber.tag("Initializer").d("PremiumManager initialized successfully!")
        }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf(
        KotprefInitializer::class.java
    )
}