package com.uni.remote.tech.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.openWebPage(url: String, noActivityFound: () -> Unit) {
    try {
        var newUrl = url
        if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
            newUrl = "http://$newUrl"
        }
//        Timber.d("Open Web Page url = $newUrl")
        val webpage = Uri.parse(newUrl)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            noActivityFound()
        }
    } catch (_: Exception) {
        noActivityFound()
    }
}
