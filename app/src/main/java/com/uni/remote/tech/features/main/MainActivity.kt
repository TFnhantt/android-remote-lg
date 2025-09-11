package com.uni.remote.tech.features.main

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.connectsdk.core.AppInfo
import com.connectsdk.service.capability.Launcher
import com.connectsdk.service.capability.MediaControl
import com.connectsdk.service.capability.VolumeControl
import com.connectsdk.service.command.ServiceCommandError
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.uni.remote.tech.R
import com.uni.remote.tech.base.BaseActivity
import com.uni.remote.tech.common.extension.flow.collectIn
import com.uni.remote.tech.databinding.ActivityMainBinding
import com.uni.remote.tech.extensions.gone
import com.uni.remote.tech.extensions.safeClickListener
import com.uni.remote.tech.extensions.vibrate
import com.uni.remote.tech.extensions.visible
import com.uni.remote.tech.features.finddevice.FindDeviceActivity
import com.uni.remote.tech.features.main.adapter.AppsAdapter
import com.uni.remote.tech.features.main.adapter.MainViewPagerAdapter
import com.uni.remote.tech.features.setting.SettingActivity
import com.uni.remote.tech.features.subscription.SubscriptionActivity
import com.uni.remote.tech.lgsocket.DeviceState
import com.uni.remote.tech.utils.AppPref
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override val viewModel: MainViewModel by viewModels()
    private var muteState = false
    private var playState = false
    private val appsAdapter by lazy {
        AppsAdapter(
            context = this,
            itemClick = ::onAppItemClick
        )
    }

    private val findDeviceActResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    viewModel.lgConnectManager.mTV?.friendlyName?.let {
                        binding.tvStatusDevice.text = getString(
                            R.string.device_name,
                            it
                        )
                    }
                } catch (_: Exception) {
                    binding.tvStatusDevice.text = getString(R.string.no_device)
                }
            }
        }

    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        binding.vpLayout.adapter = MainViewPagerAdapter(this)
        binding.vpLayout.isUserInputEnabled = false

        val layoutManager = GridLayoutManager(this, 2, RecyclerView.HORIZONTAL, false)
        binding.rvApp.layoutManager = layoutManager
        binding.rvApp.adapter = appsAdapter

        // Sync TabLayout and ViewPager2
        TabLayoutMediator(
            binding.tlNav,
            binding.vpLayout
        ) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> tab.setIcon(R.drawable.ic_dpad)
                1 -> tab.setIcon(R.drawable.ic_trackpad)
                2 -> tab.setIcon(R.drawable.ic_numpad)
            }
        }.attach()

        viewModel.lgConnectManager.getMediaControl()
            ?.subscribePlayState(object : MediaControl.PlayStateListener {
                override fun onError(error: ServiceCommandError?) {}

                override fun onSuccess(state: MediaControl.PlayStateStatus?) {
                    playState = state == MediaControl.PlayStateStatus.Playing
                }
            })
    }

    private fun initListener() {
        binding.imvPremium.safeClickListener {
            startActivity(Intent(this@MainActivity, SubscriptionActivity::class.java))
        }

        binding.imvSetting.safeClickListener {
            startActivity(Intent(this@MainActivity, SettingActivity::class.java))
        }

        binding.tvStatusDevice.safeClickListener {
            showInterstitialAd {
                checkConnected { showDisconnectDialog() }
            }
        }

        binding.imvPower.safeClickListener {
            checkConnected { showPowerOffDialog() }
        }

        binding.imvHome.safeClickListener {
            checkConnected { viewModel.lgConnectManager.getKeyControl()?.home(null) }
        }

        binding.imvVolUp.safeClickListener {
            checkConnected { viewModel.lgConnectManager.getVolumeControl()?.volumeUp(null) }
        }

        binding.imvVolDown.safeClickListener {
            checkConnected { viewModel.lgConnectManager.getVolumeControl()?.volumeDown(null) }
        }

        binding.imvBack.safeClickListener {
            checkConnected { viewModel.lgConnectManager.getKeyControl()?.back(null) }
        }

        binding.imvMute.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getVolumeControl()?.apply {
                    subscribeMute(object : VolumeControl.MuteListener {
                        override fun onError(error: ServiceCommandError?) {}

                        override fun onSuccess(state: Boolean?) {
                            muteState = state ?: false
                        }
                    })
                    setMute(!muteState, null)
                }
            }
        }

        binding.imvPrevious.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getMediaControl()?.rewind(null)
            }
        }

        binding.imvPlay.safeClickListener {
            checkConnected {
                if (playState) {
                    viewModel.lgConnectManager.getMediaControl()?.pause(null)
                } else {
                    viewModel.lgConnectManager.getMediaControl()?.play(null)
                }
            }
        }

        binding.imvNext.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getMediaControl()?.fastForward(null)
            }
        }

        binding.imvCHUp.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getTVControl()?.channelUp(null)
            }
        }

        binding.imvCHDown.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getTVControl()?.channelDown(null)
            }
        }
    }

    private fun bindViewModel() {
        viewModel.hasPurchased.collectIn(this) { isPurchased ->
            binding.imvPremium.isVisible = !isPurchased
            binding.flBannerAds.isVisible = !isPurchased

            if (!isPurchased) {
                loadBannerAd(
                    adview = binding.flBannerAds,
                    adId = getString(R.string.ads_banner_id)
                )
            }
        }

        viewModel.lgConnectManager.stateLiveData.observe(this) {
            when (it) {
                is DeviceState.DeviceDisconnect -> {
                    lifecycleScope.launch {
                        binding.rvApp.gone()
                        appsAdapter.submitList(null)
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.device_disconnected),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    viewModel.lgConnectManager.apply {
                        mTV?.disconnect()
                        isConnected = false
                        removeListener()
                        mTV = null
                    }
                }

                is DeviceState.DeviceReady -> {
                    lifecycleScope.launch {
                        binding.rvApp.visible()
                    }

                    viewModel.lgConnectManager.getLauncher()
                        ?.getAppList(object : Launcher.AppListListener {
                            override fun onError(error: ServiceCommandError?) {
                            }

                            override fun onSuccess(list: MutableList<AppInfo>?) {
                                lifecycleScope.launch {
                                    appsAdapter.submitList(list)
                                }
                            }
                        })
                }

                else -> {}
            }
        }
    }

    private fun onAppItemClick(appInfo: AppInfo) {
        checkConnected {
            viewModel.lgConnectManager.getLauncher()?.launchApp(appInfo.id, null)
        }
    }

    private fun checkConnected(complete: () -> Unit) {
        if (!viewModel.lgConnectManager.isConnected) {
            findDeviceActResultLauncher.launch(Intent(this, FindDeviceActivity::class.java))
            return
        }

        if (AppPref.vibrationEnabled) {
            vibrate(100)
        }

        complete.invoke()
    }

    private fun showPowerOffDialog() {
        AlertDialog.Builder(this@MainActivity)
            .setTitle(getString(R.string.power_off))
            .setMessage(getString(R.string.are_you_sure_turn_off_device))
            .setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                viewModel.lgConnectManager.getPowerControl()?.powerOff(null)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showDisconnectDialog() {
        AlertDialog.Builder(this)
            .setTitle("Disconnect Device")
            .setMessage("Are you sure you want to disconnect from the current device?")
            .setNegativeButton(
                "Cancel"
            ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            .setPositiveButton(
                "Sure"
            ) { dialog: DialogInterface, which: Int ->
                viewModel.lgConnectManager.apply {
                    mTV?.disconnect()
                    isConnected = false
                    removeListener()
                    mTV = null
                }
                dialog.dismiss()
            }
            .show()
    }


    override fun setupViewBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(inflater)
}