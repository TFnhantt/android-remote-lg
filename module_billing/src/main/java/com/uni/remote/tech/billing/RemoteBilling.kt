package com.uni.remote.tech.billing

import com.uni.remote.tech.billing.manager.BillingManager
import com.uni.remote.tech.common.RemoteBilling

val RemoteBilling.billing: BillingManager
    get() = BillingManager.getInstance()
