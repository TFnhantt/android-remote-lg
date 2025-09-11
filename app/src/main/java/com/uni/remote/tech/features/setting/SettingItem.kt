package com.uni.remote.tech.features.setting

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.uni.remote.tech.R

enum class SettingItem(
    @StringRes val title: Int,
    @DrawableRes val iconResId: Int,
    val isToggle: Boolean
) {
    PREMIUM(R.string.upgrade_to_premium, R.drawable.ic_premium, false),
    VIBRATION(R.string.vibration, R.drawable.ic_vibration, true),
//    LANGUAGE(R.string.language, R.drawable.ic_language, false),
    TERM(R.string.term_of_use, R.drawable.ic_term, false),
    PRIVACY(R.string.privacy_policy, R.drawable.ic_policy, false),
    RATE(R.string.rate_app, R.drawable.ic_rate, false),
    SHARE(R.string.share, R.drawable.ic_share, false)
}
