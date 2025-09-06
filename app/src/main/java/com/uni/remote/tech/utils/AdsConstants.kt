package com.uni.remote.tech.utils

import android.content.res.Resources
import com.uni.remote.tech.MainApplication
import com.uni.remote.tech.R
import com.uni.remote.tech.admob.constants.AbstractAdsConstants

object AdsConstants : AbstractAdsConstants() {
    private val resources: Resources
        get() = MainApplication.instance.resources

    override val ADMOB_NATIVE_ID = resources.getString(R.string.ads_native_id)
    override val ADMOB_INTERSTITIAL_ID = resources.getString(R.string.ads_interstitial_id)
    override val ADMOB_BACKWARD_INTERSTITIAL_ID =
        resources.getString(R.string.ads_interstitial_backward_id)
    override val ADMOB_BANNER_ID = ""
    override val ADMOB_REWARDED_ID = ""
    override val ADMOB_APP_OPEN_ID = resources.getString(R.string.ads_open_ads_id)
}