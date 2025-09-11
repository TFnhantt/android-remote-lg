package com.uni.remote.tech.utils

import com.chibatching.kotpref.KotprefModel

object AppPref : KotprefModel() {
    var vibrationEnabled by booleanPref(false)
}