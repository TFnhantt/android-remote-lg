package com.uni.remote.tech.billing.listener

import com.uni.remote.tech.billing.model.IAPProduct

interface BillingClientListener {
    fun onConnected(isConnected: Boolean, responseCode: Int)

    fun onQueryProductDetailComplete(products: List<IAPProduct>)

    fun onLaunchPurchaseComplete(isSuccess: Boolean)
}
