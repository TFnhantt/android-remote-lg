package com.uni.remote.tech.utils

import android.util.Log
import com.chibatching.kotpref.KotprefModel
import com.connectsdk.device.ConnectableDevice

object AppPref : KotprefModel() {
    var vibrationEnabled by booleanPref(false)
    var deviceID by stringPref("")
    var isConnecting by booleanPref(false)

    public fun saveDevice(connectableDevice: ConnectableDevice) {
        deviceID = connectableDevice.id
        isConnecting = true
    }

    public fun disconnectDevice() {
        deviceID = ""
        isConnecting = false
    }
}