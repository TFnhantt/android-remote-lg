package com.uni.remote.tech.extensions

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

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
