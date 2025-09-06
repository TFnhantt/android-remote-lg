package com.uni.remote.tech.common

import com.uni.remote.tech.common.premium.PremiumManager

object RemoteBilling

val RemoteBilling.premium: PremiumManager
    get() = PremiumManager.INSTANCE