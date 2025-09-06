package com.uni.remote.tech.billing.listener

import com.uni.remote.tech.billing.model.PurchaseInfo

interface PurchaseServiceListener {
    fun onProductPurchased(purchaseInfo: PurchaseInfo)

    fun onProductRestored(purchaseInfo: PurchaseInfo)

    fun onProductPurchasePending(purchaseInfo: PurchaseInfo)
}
