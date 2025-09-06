package com.uni.remote.tech.admob.base

import com.google.android.gms.ads.AdRequest
import com.uni.remote.tech.common.premium.IPremiumManager

open class BaseAdUtils internal constructor(
    private val premiumManager: IPremiumManager
) {
    internal val defaultAdRequest: AdRequest
        get() = AdRequest.Builder().build()

    internal val appAllowShowAd: Boolean
        get() = !premiumManager.isSubscribed()
}
