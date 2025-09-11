package com.uni.remote.tech.features.main.numpad

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.connectsdk.service.capability.KeyControl
import com.uni.remote.tech.base.BaseFragment
import com.uni.remote.tech.databinding.FragmentNumPadBinding
import com.uni.remote.tech.extensions.safeClickListener
import com.uni.remote.tech.extensions.vibrate
import com.uni.remote.tech.features.finddevice.FindDeviceActivity
import com.uni.remote.tech.features.main.MainViewModel
import com.uni.remote.tech.utils.AppPref

class NumPadFragment : BaseFragment<FragmentNumPadBinding, MainViewModel>() {
    override val viewModel: MainViewModel = MainViewModel()

    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentNumPadBinding.inflate(inflater)

    override fun init(savedInstanceState: Bundle?) {
        initListener()
    }

    fun initListener() {
        binding.tvZero.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()
                    ?.sendKeyCode(KeyControl.KeyCode.NUM_0, null)
            }
        }
        binding.tvOne.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()
                    ?.sendKeyCode(KeyControl.KeyCode.NUM_1, null)
            }
        }
        binding.tvTwo.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()
                    ?.sendKeyCode(KeyControl.KeyCode.NUM_2, null)
            }
        }
        binding.tvThree.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()
                    ?.sendKeyCode(KeyControl.KeyCode.NUM_3, null)
            }
        }
        binding.tvFour.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()
                    ?.sendKeyCode(KeyControl.KeyCode.NUM_4, null)
            }
        }
        binding.tvFive.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()
                    ?.sendKeyCode(KeyControl.KeyCode.NUM_5, null)
            }
        }
        binding.tvSix.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()
                    ?.sendKeyCode(KeyControl.KeyCode.NUM_6, null)
            }
        }
        binding.tvSeven.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()
                    ?.sendKeyCode(KeyControl.KeyCode.NUM_7, null)
            }
        }
        binding.tvEight.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()
                    ?.sendKeyCode(KeyControl.KeyCode.NUM_8, null)
            }
        }
        binding.tvNine.safeClickListener {
            checkConnected {
                viewModel.lgConnectManager.getKeyControl()
                    ?.sendKeyCode(KeyControl.KeyCode.NUM_9, null)
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