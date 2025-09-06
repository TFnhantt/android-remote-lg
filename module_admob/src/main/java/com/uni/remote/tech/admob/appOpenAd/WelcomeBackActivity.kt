package com.uni.remote.tech.admob.appOpenAd

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.uni.remote.tech.admob.AdsManager
import com.uni.remote.tech.admob.R
import com.uni.remote.tech.admob.databinding.ActivityWelcomeBinding
import com.uni.remote.tech.admob.extensions.loadAnimationFromRes

class WelcomeBackActivity : AppCompatActivity() {
    private val adsManager = AdsManager.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private val showAdRunnable = Runnable {
        adsManager.appOpenAdUtils.showAdIfAvailable(
            this,
            object : OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    finish()
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Drop down icon view
        loadAnimationFromRes(view = binding.sivLogo, R.anim.drop_down_animation)

        // Disable back pressed
        onBackPressedDispatcher.addCallback(this) { }
    }

    override fun onStart() {
        super.onStart()

        try {
            handler.postDelayed(showAdRunnable, 1000)
        } catch (e: Exception) {
            finish()
        }
    }

    override fun onStop() {
        super.onStop()

        handler.removeCallbacks(showAdRunnable)
    }
}
