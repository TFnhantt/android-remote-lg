package com.uni.remote.tech.features.main.dpad

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.uni.remote.tech.base.BaseFragment
import com.uni.remote.tech.databinding.FragmentDPadBinding
import com.uni.remote.tech.extensions.safeClickListener
import com.uni.remote.tech.extensions.vibrate
import com.uni.remote.tech.features.finddevice.FindDeviceActivity
import com.uni.remote.tech.features.main.MainViewModel
import com.uni.remote.tech.utils.AppPref

class DPadFragment : BaseFragment<FragmentDPadBinding, MainViewModel>() {
    override val viewModel: MainViewModel = MainViewModel()

    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDPadBinding.inflate(inflater)

    override fun init(savedInstanceState: Bundle?) {
        initListener()
    }

    fun initListener() {
        binding.imvDown.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()?.down(null)
            }
        }
        binding.imvUP.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()?.up(null)
            }
        }
        binding.imvLeft.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()?.left(null)
            }
        }
        binding.imvRight.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()?.right(null)
            }
        }
        binding.tvOK.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()?.ok(null)
            }
        }
    }

    private fun checkConnected(complete: () -> Unit) {
        if (!viewModel.lgConnectManager.isConnected) {
            startActivity(Intent(requireContext(), FindDeviceActivity::class.java))
            return
        }

        if (AppPref.vibrationEnabled) {
            requireContext().vibrate(100)
        }

        complete.invoke()
    }
}