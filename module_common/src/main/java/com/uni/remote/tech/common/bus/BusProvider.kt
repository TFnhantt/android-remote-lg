package com.uni.remote.tech.common.bus

interface BusProvider {
    fun register(obj: Any)
    fun unregister(obj: Any)
    fun post(event: Any)
}