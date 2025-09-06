package com.uni.remote.tech.features.launcher

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.uni.remote.tech.admob.AdsManager
import com.uni.remote.tech.admob.admob
import com.uni.remote.tech.admob.appOpenAd.OnShowAdCompleteListener
import com.uni.remote.tech.base.BaseActivity
import com.uni.remote.tech.common.RemoteBilling
import com.uni.remote.tech.databinding.ActivityLauncherBinding
import com.uni.remote.tech.features.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LauncherActivity : BaseActivity<ActivityLauncherBinding, LauncherViewModel>() {
    override val viewModel: LauncherViewModel by viewModels()

    private var secondsRemaining: Long = 0L

    override fun init(savedInstanceState: Bundle?) {
        initView()
    }

    private fun initView() {
        gatherConsentIfNeed {
            if (viewModel.isPurchased) {
                openMainActivity()
            } else {
                createTimer()
            }
        }
    }

    private fun gatherConsentIfNeed(callback: () -> Unit) {
        if (viewModel.isPurchased) {
            callback()
        } else {
            RemoteBilling.admob.gatherConsent(this, object : AdsManager.OnGatherConsentListener {
                override fun onCompletion(error: String?) {
                    callback()
                }

            })
        }
    }

    private fun createTimer() {
        val countDownTimer: CountDownTimer =
            object : CountDownTimer(COUNTER_TIME_MILLISECONDS, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1
                    if (RemoteBilling.admob.appOpenAdUtils.isAdAvailable()) {
                        cancel()
                        RemoteBilling.admob.appOpenAdUtils.showAdIfAvailable(
                            this@LauncherActivity,
                            object : OnShowAdCompleteListener {
                                override fun onShowAdComplete() {
                                    openMainActivity()
                                }
                            })
                    }
                }

                override fun onFinish() {
                    secondsRemaining = 0
                    openMainActivity()
                }
            }
        countDownTimer.start()
    }

    private fun openMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun setupViewBinding(inflater: LayoutInflater) =
        ActivityLauncherBinding.inflate(inflater)

    companion object {
        private const val COUNTER_TIME_MILLISECONDS = 10000L
    }
}