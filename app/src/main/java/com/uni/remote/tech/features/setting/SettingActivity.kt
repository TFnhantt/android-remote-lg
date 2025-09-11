package com.uni.remote.tech.features.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ShareCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.nativead.NativeAd
import com.uni.remote.tech.R
import com.uni.remote.tech.base.BaseActivity
import com.uni.remote.tech.common.extension.flow.collectIn
import com.uni.remote.tech.databinding.ActivitySettingBinding
import com.uni.remote.tech.extensions.linkToChPlay
import com.uni.remote.tech.extensions.openWebPage
import com.uni.remote.tech.extensions.safeClickListener
import com.uni.remote.tech.features.main.MainViewModel
import com.uni.remote.tech.features.subscription.SubscriptionActivity
import com.uni.remote.tech.utils.AppPref
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding, MainViewModel>() {

    override val viewModel: MainViewModel by viewModels()

    private var nativeAd: NativeAd? = null

    private val settingAdapter by lazy {
        SettingAdapter(
            context = this,
            onToggleChange = { item, isChecked ->
                if (item == SettingItem.VIBRATION) {
                    AppPref.vibrationEnabled = isChecked
                }
            },
            onItemClick = ::handleItemClick
        )
    }

    override fun init(savedInstanceState: Bundle?) {
        binding.apply {
            rclSetting.layoutManager = LinearLayoutManager(this@SettingActivity)
            rclSetting.adapter = settingAdapter
            imvBack.safeClickListener { onBackPressedDispatcher.onBackPressed() }
        }

        // Observe VM
        viewModel.hasPurchased.collectIn(this) { isSubscribed ->
            binding.nativeView.visibility = if (isSubscribed) View.GONE else View.VISIBLE
            val updatedItems = SettingItem.entries.toMutableList().toMutableList().apply {
                if (isSubscribed) remove(SettingItem.PREMIUM)
            }
            settingAdapter.submitList(updatedItems)
        }

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
    }

    private fun handleItemClick(item: SettingItem) {
        when (item) {
            SettingItem.PREMIUM -> startActivity(Intent(this, SubscriptionActivity::class.java))
            SettingItem.RATE -> linkToChPlay()
            SettingItem.TERM -> openWebPage(getString(R.string.term_of_use_url))
            SettingItem.PRIVACY -> openWebPage(getString(R.string.privacy_policy_url))
            SettingItem.SHARE -> shareApp()
            else -> {}
        }
    }

    private fun shareApp() {
        ShareCompat.IntentBuilder(this)
            .setType("text/plain")
            .setChooserTitle("Share APP")
            .setText("http://play.google.com/store/apps/details?id=$packageName")
            .startChooser()
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAd?.destroy()
    }

    override fun setupViewBinding(inflater: LayoutInflater) =
        ActivitySettingBinding.inflate(inflater)
}