package com.uni.remote.tech.lgsocket

import com.connectsdk.service.DeviceService

sealed class DeviceState {
    object DeviceReady : DeviceState()
    object DeviceDisconnect : DeviceState()
    data class ParingRequire(val paringState: DeviceService.PairingType?) :DeviceState()
    object CapabilityUpdated : DeviceState()
    object ConnectionFailed : DeviceState()
}