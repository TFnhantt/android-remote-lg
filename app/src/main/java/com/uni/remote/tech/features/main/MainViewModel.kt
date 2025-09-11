package com.uni.remote.tech.features.main

import dagger.hilt.android.lifecycle.HiltViewModel
import com.uni.remote.tech.base.BaseViewModel
import com.uni.remote.tech.lgsocket.LGConnectManager
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : BaseViewModel() {
    val lgConnectManager = LGConnectManager.getInstance()
}