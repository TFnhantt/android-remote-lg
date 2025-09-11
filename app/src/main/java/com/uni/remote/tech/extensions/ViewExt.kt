package com.uni.remote.tech.extensions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresPermission
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment

/**
 * Visible an view.
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * Gone an view.
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * Invisible an view.
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * This function to click view for safe
 */
fun View.safeClickListener(safeClickListener: (view: View) -> Unit) {
    setOnClickListener { v ->
        isClickable = false
        safeClickListener(v)
        postDelayed({ isClickable = true }, 1000)
    }
}

/**
 * This function to padding root view from status bar & navigation bar
 */
fun View.applyInsetsVerticalPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val i =
            windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout())
        view.updatePadding(
            top = i.top,
            bottom = i.bottom
        )
        WindowInsetsCompat.CONSUMED
    }
}

/**
 * This function to padding root view from status bar
 */
fun View.applyInsetsTopPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val i =
            windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout())
        view.updatePadding(
            top = i.top,
        )
        WindowInsetsCompat.CONSUMED
    }
}

/**
 * This function to padding root view from navigation bar
 */
fun View.applyInsetsBottomPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val i =
            windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout())
        view.updatePadding(
            bottom = i.bottom,
        )
        WindowInsetsCompat.CONSUMED
    }
}

fun View.hideSoftKeyboard() {
    val inputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
}

@RequiresPermission(Manifest.permission.VIBRATE)
fun Context.vibrate(durationMillis: Long) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(durationMillis)
    }
}