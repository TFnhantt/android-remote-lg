package com.uni.remote.tech.common.premium

import com.chibatching.kotpref.KotprefModel

internal object PremiumPrefs : KotprefModel() {
    var isSubscribed by booleanPref()
    var isUnlockByCode by booleanPref()
}