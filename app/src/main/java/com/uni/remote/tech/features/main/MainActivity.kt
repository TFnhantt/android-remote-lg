package com.uni.remote.tech.features.main

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.uni.remote.tech.R
import com.uni.remote.tech.base.BaseActivity
import com.uni.remote.tech.common.extension.flow.collectIn
import com.uni.remote.tech.databinding.ActivityMainBinding
import com.uni.remote.tech.features.main.adapter.MainViewPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override val viewModel: MainViewModel by viewModels()

    private var currentTab = 0


    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        binding.vpLayout.adapter = MainViewPagerAdapter(this)
        binding.vpLayout.isUserInputEnabled = false

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
    }

    private fun initListener() {

    }

    private fun bindViewModel() {
        viewModel.hasPurchased.collectIn(this) {

            if (!it) {

            }
        }
    }

    override fun setupViewBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(inflater)
}