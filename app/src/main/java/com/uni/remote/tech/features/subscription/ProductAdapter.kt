package com.uni.remote.tech.features.subscription

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uni.remote.tech.R
import com.uni.remote.tech.billing.extensions.biggestPrice
import com.uni.remote.tech.billing.extensions.biggestSubscriptionOfferDetailsToken
import com.uni.remote.tech.billing.model.IAPProduct
import com.uni.remote.tech.billing.model.IAPProductPeriods
import com.uni.remote.tech.billing.model.periods
import com.uni.remote.tech.databinding.ItemPurchaseProductFreeTrialBinding
import com.uni.remote.tech.databinding.ItemPurchaseProductNormalBinding
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class ProductAdapter(
    private val context: Context,
    private val listener: (IAPProduct, Int) -> Unit,
) : ListAdapter<ProductWithSelection, ProductAdapter.VH>(DataDifferentiator) {
    private val layoutInflater = LayoutInflater.from(context)

    abstract class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: ProductWithSelection, position: Int)
        abstract fun bindSelectedState(isSelected: Boolean)
    }

    private inner class ProductNormalVH(
        private val binding: ItemPurchaseProductNormalBinding,
    ) : VH(binding.root) {

        override fun bind(item: ProductWithSelection, position: Int) {
            val (product, isSelected) = item

            binding.run {
                root.setOnClickListener { listener(product, position) }

                if (product.isOneTime) {
                    product.productDetails?.oneTimePurchaseOfferDetails?.let {
                        val price = try {
                            getPrice(
                                it.priceCurrencyCode,
                                it.priceAmountMicros.toDouble()
                            )
                        } catch (e: Exception) {
                            ""
                        }
                        tvProductName.text =
                            context.resources.getString(R.string.one_time_name)
                        tvProductDes.text =
                            context.resources.getString(R.string.one_time_description)
                    }
                } else {
                    product.productDetails?.let {
                        val subscriptionOfferDetails = it.biggestSubscriptionOfferDetailsToken()
                        subscriptionOfferDetails?.let { offerDetails ->
                            val pricePhase = offerDetails.biggestPrice()

                            pricePhase?.let { nonNullPricePhase ->
                                val price = try {
                                    getPrice(
                                        nonNullPricePhase.priceCurrencyCode,
                                        nonNullPricePhase.priceAmountMicros.toDouble()
                                    )
                                } catch (e: Exception) {
                                    ""
                                }
                                tvProductName.text = context.resources.getString(
                                    R.string.single_product_normal,
                                    price,
                                    getTimePeriodPage(product)
                                )
                                tvProductDes.text =
                                    context.resources.getString(R.string.auto_renew_cancel_anytime)
                            }
                        }
                    }
                }

                bindSelectedState(isSelected)
            }
        }

        override fun bindSelectedState(isSelected: Boolean) {
            binding.run {
                root.isSelected = isSelected
                tvProductName.isSelected = isSelected
                tvProductDes.isSelected = isSelected
            }
        }
    }

    private inner class ProductTrialVH(
        private val binding: ItemPurchaseProductFreeTrialBinding,
    ) : VH(binding.root) {

        override fun bind(item: ProductWithSelection, position: Int) {
            val (product, isSelected) = item

            binding.run {
                root.setOnClickListener { listener(product, position) }

                product.productDetails?.let {
                    val subscriptionOfferDetails = it.biggestSubscriptionOfferDetailsToken()
                    subscriptionOfferDetails?.let { offerDetails ->
                        val pricePhase = offerDetails.biggestPrice()

                        pricePhase?.let { nonNullPricePhase ->
                            val price = try {
                                getPrice(
                                    nonNullPricePhase.priceCurrencyCode,
                                    nonNullPricePhase.priceAmountMicros.toDouble()
                                )
                            } catch (e: Exception) {
                                ""
                            }
                            tvProductName.text = context.getString(
                                R.string.free_trial,
                                product.freeTrialDays
                            )
                            tvProductDes.text =
                                context.resources.getString(
                                    R.string.free_trial_description,
                                    price,
                                    getTimePeriodPage(product)
                                )
                        }
                    }
                }
                bindSelectedState(isSelected)
            }
        }

        override fun bindSelectedState(isSelected: Boolean) {
            binding.run {
                root.isSelected = isSelected
                tvProductName.isSelected = isSelected
                tvProductDes.isSelected = isSelected
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        IAPProduct.PRODUCT_NORMAL_VIEW_TYPE -> {
            ProductNormalVH(
                ItemPurchaseProductNormalBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
        }

        IAPProduct.PRODUCT_TRIAL_VIEW_TYPE -> {
            ProductTrialVH(
                ItemPurchaseProductFreeTrialBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
        }

        else -> error("Unknown view type: $viewType")
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(item = getItem(position), position = position)

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (payloads[0] == true) {
                val item = getItem(position)
                holder.bindSelectedState(item.second)
            }
        }
    }

    override fun getItemViewType(position: Int) = getItem(position).first.viewType

    private fun getTimePeriodPage(product: IAPProduct): String {
        return when (product.periods()) {
            IAPProductPeriods.Weekly -> context.getString(R.string.week)
            IAPProductPeriods.Monthly -> context.getString(R.string.month)
            IAPProductPeriods.Yearly -> context.getString(R.string.year)
            else -> ""
        }
    }

    private fun getNamePeriodPage(product: IAPProduct): String {
        return when (product.periods()) {
            IAPProductPeriods.Weekly -> context.getString(R.string.weekly)
            IAPProductPeriods.Monthly -> context.getString(R.string.monthly)
            IAPProductPeriods.Yearly -> context.getString(R.string.yearly)
            else -> ""
        }
    }

    private fun getPrice(
        currencyCode: String,
        priceAmountMicros: Double,
    ): String {
        val price: Double = priceAmountMicros / 1000000.0
        val currencyFormat: NumberFormat =
            NumberFormat.getCurrencyInstance(Locale.getDefault())
        currencyFormat.currency = Currency.getInstance(currencyCode)
        currencyFormat.minimumFractionDigits = 0

        return currencyFormat.format(price)
    }

    object DataDifferentiator : DiffUtil.ItemCallback<ProductWithSelection>() {
        override fun areItemsTheSame(oldItem: ProductWithSelection, newItem: ProductWithSelection) =
            oldItem.first.productId == newItem.first.productId

        override fun areContentsTheSame(
            oldItem: ProductWithSelection,
            newItem: ProductWithSelection
        ) = oldItem == newItem

        override fun getChangePayload(
            oldItem: ProductWithSelection,
            newItem: ProductWithSelection
        ) = oldItem.second != newItem.second
    }
}