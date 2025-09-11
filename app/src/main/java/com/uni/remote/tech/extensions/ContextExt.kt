package com.uni.remote.tech.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber

fun Context.linkToChPlay(idApp: String = packageName) {
    try {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$idApp"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$idApp")
        )

        if (marketIntent.resolveActivity(packageManager) != null) {
            startActivity(marketIntent)
        } else if (webIntent.resolveActivity(packageManager) != null) {
            startActivity(webIntent)
        }
    } catch (_: Exception) {
    }
}

fun Context.openWebPage(url: String, noActivityFound: () -> Unit = {}) {
    try {
        var newUrl = url
        if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
            newUrl = "http://$newUrl"
        }
        Timber.d("Open Web Page url = $newUrl")
        val webpage = Uri.parse(newUrl)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            noActivityFound()
        }
    } catch (e: Exception) {
        noActivityFound()
    }
}
