package com.uni.remote.tech.common.bus

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class BusProviderImpl : BusProvider {
    private val mainScope = MainScope()
    private val bus = EventBus()

    override fun register(obj: Any) {
        bus.register(obj)
    }

    override fun unregister(obj: Any) {
        try {
            bus.unregister(obj)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun post(event: Any) {
        mainScope.launch { bus.post(event) }
    }
}