package com.uni.remote.tech.common.utils.timber

import android.content.Context
import androidx.startup.Initializer
import com.uni.remote.tech.common.BuildConfig
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugLogTree())
        }
        Timber.tag("Initializer").d("Timber initialized successfully!")
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}