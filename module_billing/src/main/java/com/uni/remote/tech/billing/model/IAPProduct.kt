package com.uni.remote.tech.billing.model

import androidx.annotation.IntDef
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.uni.remote.tech.billing.extensions.biggestPrice
import com.uni.remote.tech.billing.extensions.biggestSubscriptionOfferDetailsToken
import kotlin.time.Duration
import kotlin.time.DurationUnit

enum class IAPProductType {
    InApp,
    Subscription
}

enum class IAPProductPeriods {
    Weekly,
    Monthly,
    Yearly
}

data class IAPProduct(
    val productType: IAPProductType,
    val productId: String,
    val consumable: Boolean = false,
    var productDetails: ProductDetails? = null
) {
    init {
        if (productType == IAPProductType.Subscription && consumable) {
            throw IllegalArgumentException("IAPProduct with type Subscription cannot be consumable")
        }
    }

    val isConsumable: Boolean
        get() = consumable && productType == IAPProductType.InApp

    val isOneTime: Boolean
        get() = !consumable && productType == IAPProductType.InApp

    val hasFreeTrial: Boolean
        get() {
            return if (productDetails?.productType == BillingClient.ProductType.SUBS) {
                productDetails
                    ?.subscriptionOfferDetails
                    ?.firstOrNull()
                    ?.pricingPhases
                    ?.pricingPhaseList
                    ?.firstOrNull { it.priceAmountMicros == 0L } != null
            } else {
                false
            }
        }

    val freeTrialDays: Int
        get() {
            return if (hasFreeTrial) {
                productDetails?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.let {
                    Duration.parseIsoStringOrNull(it)?.toInt(DurationUnit.DAYS) ?: 0
                } ?: 0
            } else {
                0
            }
        }

    @IAPProductViewType
    val viewType: Int
        get() = if (hasFreeTrial) PRODUCT_TRIAL_VIEW_TYPE else PRODUCT_NORMAL_VIEW_TYPE

    companion object {
        const val PRODUCT_NORMAL_VIEW_TYPE = 1
        const val PRODUCT_TRIAL_VIEW_TYPE = 2

        @IntDef(
            value = [
                PRODUCT_NORMAL_VIEW_TYPE,
                PRODUCT_TRIAL_VIEW_TYPE
            ]
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class IAPProductViewType
    }
}

fun IAPProductType.contentType(): String =
    if (this == IAPProductType.Subscription) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP

fun IAPProduct.priceAmountMicros(): Long? = if (productType == IAPProductType.InApp) {
    productDetails?.oneTimePurchaseOfferDetails?.priceAmountMicros
} else {
    productDetails?.biggestSubscriptionOfferDetailsToken()?.biggestPrice()?.priceAmountMicros
}

fun IAPProduct.priceCurrencyCode(): String? = if (productType == IAPProductType.InApp) {
    productDetails?.oneTimePurchaseOfferDetails?.priceCurrencyCode
} else {
    productDetails?.biggestSubscriptionOfferDetailsToken()?.biggestPrice()?.priceCurrencyCode
}

fun IAPProduct.periods(): IAPProductPeriods? = if (productType == IAPProductType.Subscription) {
    productDetails
        ?.biggestSubscriptionOfferDetailsToken()
        ?.biggestPrice()
        ?.billingPeriod
        ?.let {
            if (it.endsWith("W")) {
                IAPProductPeriods.Weekly
            } else if (it.endsWith("M")) {
                IAPProductPeriods.Monthly
            } else if (it.endsWith("Y")) {
                IAPProductPeriods.Yearly
            } else {
                null
            }
        }
} else {
    null
}
