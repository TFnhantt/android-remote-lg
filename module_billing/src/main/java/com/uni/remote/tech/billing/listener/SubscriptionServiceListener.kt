package com.uni.remote.tech.billing.listener

import com.uni.remote.tech.billing.model.PurchaseInfo

interface SubscriptionServiceListener {
    fun onSubscriptionRestored(purchaseInfo: PurchaseInfo)

    fun onSubscriptionPurchased(purchaseInfo: PurchaseInfo)

    fun onSubscriptionPurchasePending(purchaseInfo: PurchaseInfo)
}
