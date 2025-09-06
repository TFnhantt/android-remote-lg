package com.uni.remote.tech.admob.extensions

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes

fun Context.loadAnimationFromRes(view: View, @AnimRes res: Int) {
    view.startAnimation(AnimationUtils.loadAnimation(this, res))
}