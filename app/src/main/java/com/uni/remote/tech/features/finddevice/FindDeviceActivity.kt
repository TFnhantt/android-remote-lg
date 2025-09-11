package com.uni.remote.tech.features.finddevice

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.connectsdk.service.DeviceService
import com.google.android.gms.ads.nativead.NativeAd
import com.uni.remote.tech.R
import com.uni.remote.tech.base.BaseActivity
import com.uni.remote.tech.common.extension.flow.collectIn
import com.uni.remote.tech.databinding.ActivityFindDeviceBinding
import com.uni.remote.tech.extensions.hideSoftKeyboard
import com.uni.remote.tech.lgsocket.DeviceState
import kotlin.getValue

class FindDeviceActivity : BaseActivity<ActivityFindDeviceBinding, FindDeviceViewModel>() {
    override val viewModel: FindDeviceViewModel by viewModels()
    private var nativeAd: NativeAd? = null
    private lateinit var pairingAlertDialog: AlertDialog
    private lateinit var pairingCodeDialog: AlertDialog
    private var input: EditText? = null
    private val deviceAdapter by lazy {
        DeviceAdapter { device -> viewModel.connectToDevice(device) }
    }
    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        observeViewModel()
    }

    private fun initView() {
        binding.rclDevices.adapter = deviceAdapter

        pairingAlertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.pairing_code))
            .setMessage(getString(R.string.please_enter_pairing_code_on_tv))
            .setPositiveButton(getString(R.string.ok), null)
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create()

        input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        pairingCodeDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.enter_pairing_code_on_tv))
            .setView(input)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                viewModel.lgConnectManager.mTV?.let {
                    val value = input?.text.toString().trim()
                    it.sendPairingKey(value)
                    input?.hideSoftKeyboard()
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                input?.hideSoftKeyboard()
            }
            .create()
    }

    private fun initListener() {
        binding.imvBack.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        viewModel.hasPurchased.collectIn(this) {
            binding.nativeView.isVisible = !it
            if (!it) {
                loadSingleNativeAd(
                    adId = getString(R.string.ads_native_id),
                    onAdLoaded = { native ->
                        nativeAd?.destroy()
                        nativeAd = native
                        binding.nativeView.populate(native)
                    }
                )
            }
        }

        viewModel.devices.observe(this) {
            deviceAdapter.submitList(it)
        }
        viewModel.deviceState.observe(this) {
            when (it) {
                is DeviceState.DeviceReady -> {
                    pairingAlertDialog.dismiss()
                    pairingCodeDialog.dismiss()
                    input?.hideSoftKeyboard()
                    Toast.makeText(this, getString(R.string.connect_success), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is DeviceState.ConnectionFailed -> {
                    Toast.makeText(this, getString(R.string.there_was_an_error), Toast.LENGTH_SHORT).show()
                }
                is DeviceState.ParingRequire -> {
                    when (it.paringState) {
                        DeviceService.PairingType.FIRST_SCREEN -> pairingAlertDialog.show()
                        DeviceService.PairingType.PIN_CODE,
                        DeviceService.PairingType.MIXED -> pairingCodeDialog.show()
                        else -> {}
                    }
                }
                is DeviceState.DeviceDisconnect -> {
//                    Toast.makeText(this, getString(R.string.device_disconnected), Toast.LENGTH_SHORT).show()
                    input?.setText("")
                }
                else -> {}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAd?.destroy()
    }
    override fun setupViewBinding(inflater: LayoutInflater) =
        ActivityFindDeviceBinding.inflate(inflater)
}