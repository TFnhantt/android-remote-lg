package com.uni.remote.tech.features.finddevice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.discovery.DiscoveryManagerListener
import com.connectsdk.service.DeviceService
import com.connectsdk.service.command.ServiceCommandError
import com.uni.remote.tech.base.BaseViewModel
import com.uni.remote.tech.lgsocket.DeviceState
import com.uni.remote.tech.lgsocket.LGConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FindDeviceViewModel @Inject constructor() : BaseViewModel(), DiscoveryManagerListener {

    val lgConnectManager = LGConnectManager.getInstance()
    private val discoveryManager = DiscoveryManager.getInstance()

    private val _devices = MutableLiveData<List<ConnectableDevice>>(emptyList())
    val devices: LiveData<List<ConnectableDevice>> = _devices

    private val _deviceState = MutableLiveData<DeviceState>()
    val deviceState: LiveData<DeviceState> = _deviceState

    private val connectableDevices = mutableSetOf<ConnectableDevice>()
    var isClickedToConnect = false
    var isPinCoding = false

    init {
        discoveryDevices()
    }

    private fun discoveryDevices() {
        discoveryManager.apply {
            addListener(this@FindDeviceViewModel)
            registerDefaultDeviceTypes()
            pairingLevel = DiscoveryManager.PairingLevel.ON
        }.start()
    }

    fun refreshDiscovery() {
        connectableDevices.clear()
        _devices.value = emptyList()
        viewModelScope.launch {
            runCatching {
                discoveryManager.stop()
                discoveryManager.removeListener(this@FindDeviceViewModel)
            }
            delay(500L)
            runCatching {
                discoveryManager.apply {
                    addListener(this@FindDeviceViewModel)
                    registerDefaultDeviceTypes()
                    pairingLevel = DiscoveryManager.PairingLevel.ON
                    start()
                }
            }
        }
    }

    fun connectToDevice(device: ConnectableDevice) {
        if (!device.isConnectable) {
            _deviceState.value = DeviceState.ConnectionFailed
            return
        }
        if (lgConnectManager.isConnected) {
            lgConnectManager.mTV?.disconnect()
            lgConnectManager.isConnected = false
        }
        lgConnectManager.removeListener()
        viewModelScope.launch {
            delay(100L)
            lgConnectManager.mTV = device
            lgConnectManager.registerListener()
            lgConnectManager.mTV?.apply {
                isClickedToConnect = true
                setPairingType(DeviceService.PairingType.PIN_CODE)
                connect()
            }
        }
    }

    /** DiscoveryManagerListener */
    override fun onDeviceAdded(manager: DiscoveryManager?, device: ConnectableDevice?) {
        device?.let {
            if (it.serviceId.lowercase(Locale.ROOT).contains("webos") && it.isConnectable) {
                connectableDevices.find { d -> d.ipAddress == it.ipAddress }?.let { d ->
                    connectableDevices.remove(d)
                }
                connectableDevices.add(it)
                _devices.postValue(connectableDevices.toList())
            }
        }
    }

    override fun onDeviceUpdated(manager: DiscoveryManager?, device: ConnectableDevice?) {}
    override fun onDeviceRemoved(manager: DiscoveryManager?, device: ConnectableDevice?) {}
    override fun onDiscoveryFailed(manager: DiscoveryManager?, error: ServiceCommandError?) {}
}
