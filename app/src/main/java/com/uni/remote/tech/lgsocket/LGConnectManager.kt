package com.uni.remote.tech.lgsocket

import com.connectsdk.device.ConnectableDevice
import com.connectsdk.device.ConnectableDeviceListener
import com.connectsdk.service.DeviceService
import com.connectsdk.service.capability.KeyControl
import com.connectsdk.service.capability.Launcher
import com.connectsdk.service.capability.MediaControl
import com.connectsdk.service.capability.MouseControl
import com.connectsdk.service.capability.PowerControl
import com.connectsdk.service.capability.TVControl
import com.connectsdk.service.capability.VolumeControl
import com.connectsdk.service.command.ServiceCommandError
import com.uni.remote.tech.utils.VolatileLiveData
import timber.log.Timber

class LGConnectManager {

    var mTV: ConnectableDevice? = null
    var isConnected = false

    var stateLiveData = VolatileLiveData<DeviceState>()

    private var listener = object : ConnectableDeviceListener {
        override fun onDeviceReady(device: ConnectableDevice?) {
            Timber.d("onDeviceReady")
            stateLiveData.setValue(DeviceState.DeviceReady)
        }

        override fun onDeviceDisconnected(device: ConnectableDevice?) {
            Timber.d("onDeviceDisconnected")
            stateLiveData.setValue(DeviceState.DeviceDisconnect)
        }

        override fun onPairingRequired(
            device: ConnectableDevice?,
            service: DeviceService?,
            pairingType: DeviceService.PairingType?
        ) {
            Timber.d("onPairingRequired")
            stateLiveData.setValue(DeviceState.ParingRequire(pairingType))
        }

        override fun onCapabilityUpdated(
            device: ConnectableDevice?,
            added: MutableList<String>?,
            removed: MutableList<String>?
        ) {
            Timber.d("onCapabilityUpdated")
            stateLiveData.setValue(DeviceState.CapabilityUpdated)
        }

        override fun onConnectionFailed(device: ConnectableDevice?, error: ServiceCommandError?) {
            Timber.d("onConnectionFailed")
            stateLiveData.setValue(DeviceState.ConnectionFailed)
        }

    }

    fun registerListener() {
        mTV?.addListener(listener)
    }

    fun removeListener() {
        mTV?.removeListener(listener)
    }

    fun getKeyControl(): KeyControl? {
        return mTV?.getCapability(KeyControl::class.java)
    }

    fun getTVControl(): TVControl? {
        return mTV?.getCapability(TVControl::class.java)
    }

    fun getPowerControl(): PowerControl? {
        return mTV?.getCapability(PowerControl::class.java)
    }

    fun getVolumeControl(): VolumeControl? {
        return mTV?.getCapability(VolumeControl::class.java)
    }

    fun getMediaControl(): MediaControl? {
        return mTV?.getCapability(MediaControl::class.java)
    }

    fun getMouseControl(): MouseControl? {
        return mTV?.getCapability(MouseControl::class.java)
    }

    fun getLauncher(): Launcher? {
        return mTV?.getCapability(Launcher::class.java)
    }

    companion object {
        private val shared = LGConnectManager()

        fun getInstance(): LGConnectManager {
            return shared
        }
    }
}