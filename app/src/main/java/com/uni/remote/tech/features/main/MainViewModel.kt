package com.uni.remote.tech.features.main

import com.connectsdk.device.ConnectableDevice
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.discovery.DiscoveryManagerListener
import com.connectsdk.service.command.ServiceCommandError
import dagger.hilt.android.lifecycle.HiltViewModel
import com.uni.remote.tech.base.BaseViewModel
import com.uni.remote.tech.lgsocket.LGConnectManager
import com.uni.remote.tech.utils.AppPref
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : BaseViewModel(), DiscoveryManagerListener {
    val lgConnectManager = LGConnectManager.getInstance()
    private val discoveryManager = DiscoveryManager.getInstance()

    fun reconnectDevice() {
        lgConnectManager.removeListener()

        discoveryManager.apply {
            addListener(this@MainViewModel)
            registerDefaultDeviceTypes()
            pairingLevel = DiscoveryManager.PairingLevel.ON
        }.start()
    }

    /** DiscoveryManagerListener */
    override fun onDeviceAdded(manager: DiscoveryManager?, device: ConnectableDevice?) {
        device?.let {
            if (device.getId().equals(AppPref.deviceID)) {
                lgConnectManager.isConnected = true
                lgConnectManager.mTV = device
                lgConnectManager.registerListener()
                device.connect()
            }
        }
    }

    override fun onDeviceUpdated(manager: DiscoveryManager?, device: ConnectableDevice?) {}
    override fun onDeviceRemoved(manager: DiscoveryManager?, device: ConnectableDevice?) {}
    override fun onDiscoveryFailed(manager: DiscoveryManager?, error: ServiceCommandError?) {}
}