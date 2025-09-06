package com.uni.remote.tech.common.utils.inAppReview

interface InAppReviewListener {
    fun onReviewSuccess()
    fun onReviewFailure()
    fun onRequestReviewFailed()
}