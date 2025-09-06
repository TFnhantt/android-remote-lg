package com.uni.remote.tech.common.utils.inAppReview

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import timber.log.Timber

fun Activity.showInAppReview(listener: InAppReviewListener? = null) {
    val manager = ReviewManagerFactory.create(this)
    manager.requestReviewFlow().addOnCompleteListener { request ->
        if (request.isSuccessful) {
            val reviewInfo = request.result
            manager.launchReviewFlow(this, reviewInfo)
                .addOnSuccessListener {
                    Timber.d("In-app review successfully")
                    listener?.onReviewSuccess()
                }
                .addOnFailureListener {
                    Timber.e("In-app review request failed, reason=$it")
                    listener?.onReviewFailure()
                }
        } else {
            Timber.d("In-app review request failed, reason=${request.exception}")
            listener?.onRequestReviewFailed()
        }
    }
}
