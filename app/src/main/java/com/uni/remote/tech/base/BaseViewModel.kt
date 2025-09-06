package com.uni.remote.tech.base

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.uni.remote.tech.admob.admob
import com.uni.remote.tech.common.RemoteBilling
import com.uni.remote.tech.common.premium
import javax.inject.Inject

@HiltViewModel
open class BaseViewModel
@Inject
constructor() : ViewModel() {
    val hasPurchased by lazy {
        RemoteBilling.premium.getSubscribedStateFlow()
    }

    val isPurchased: Boolean
        get() = RemoteBilling.premium.isSubscribed()

    val nativeAdsFlow by lazy {
        RemoteBilling.admob.nativeAdUtils.nativeAdsFlow
    }
}
