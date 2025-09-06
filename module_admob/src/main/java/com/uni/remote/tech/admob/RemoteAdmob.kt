package com.uni.remote.tech.admob

import com.uni.remote.tech.common.RemoteBilling

val RemoteBilling.admob: AdsManager
    get() = AdsManager.getInstance()
