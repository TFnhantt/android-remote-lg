package com.uni.remote.tech.features.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uni.remote.tech.features.main.dpad.DPadFragment
import com.uni.remote.tech.features.main.numpad.NumPadFragment
import com.uni.remote.tech.features.main.trackpad.TrackPadFragment

class MainViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DPadFragment()
            1 -> TrackPadFragment()
            2 -> NumPadFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}