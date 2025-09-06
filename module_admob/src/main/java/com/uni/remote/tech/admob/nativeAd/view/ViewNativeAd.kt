package com.uni.remote.tech.admob.nativeAd.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.children
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.SkeletonConfig
import com.faltenreich.skeletonlayout.SkeletonLayout
import com.faltenreich.skeletonlayout.createSkeleton
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.uni.remote.tech.admob.R
import com.uni.remote.tech.admob.databinding.NativeAdViewBinding
import com.uni.remote.tech.admob.nativeAd.NativeAdCalculator
import timber.log.Timber

class ViewNativeAd : FrameLayout {
    private var binding: NativeAdViewBinding? = null
    private var nativeAdViewLayout: View? = null
    private var skeleton: Skeleton? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        binding = NativeAdViewBinding.inflate(LayoutInflater.from(context))
        addView(binding?.root)

        // Load attributes
        val styledAttributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ViewNativeAd,
            defStyle,
            0
        )
        val maskColorColor = styledAttributes.getColor(
            R.styleable.ViewNativeAd_attribute_mask_color,
            resources.getColor(R.color.colorViewNativeAdMaskColor, context.theme)
        )
        val shimmerColorColor = styledAttributes.getColor(
            R.styleable.ViewNativeAd_attribute_shimmer_color,
            resources.getColor(R.color.colorViewNativeAdShimmerColor, context.theme)
        )
        val layoutNativeAdRes = styledAttributes.getResourceId(
            R.styleable.ViewNativeAd_attribute_native_ad_layout,
            R.layout.layout_native_ad_medium
        )
        styledAttributes.recycle()

        binding?.run {
            layoutStub.layoutResource = layoutNativeAdRes
            nativeAdViewLayout = layoutStub.inflate()
            skeleton = nativeAdViewLayout?.createSkeleton(
                config = SkeletonConfig(
                    maskColor = maskColorColor,
                    maskCornerRadius = 8f,
                    showShimmer = true,
                    shimmerColor = shimmerColorColor,
                    shimmerDurationInMillis = 1500L,
                    shimmerDirection = SkeletonLayout.DEFAULT_SHIMMER_DIRECTION,
                    shimmerAngle = 20
                )
            )
            skeleton?.showSkeleton()
        }
    }

    fun populate(nativeAd: NativeAd) {
        if (nativeAdViewLayout is NativeAdView) {
            NativeAdCalculator.populateNativeAdView(nativeAd, nativeAdViewLayout as NativeAdView)
            skeleton?.showOriginal()
        } else if (nativeAdViewLayout is ViewGroup) {
            val viewGroup = nativeAdViewLayout as ViewGroup
            for (child in viewGroup.children) {
                if (child is NativeAdView) {
                    NativeAdCalculator.populateNativeAdView(nativeAd, child)
                    skeleton?.showOriginal()
                    break
                }
            }
        } else {
            Timber.e("Native Ad View Layout not contain NativeAdView.")
        }
    }

    fun updateNativeAdStyle(
        backgroundColorHex: String,
        titleColorHex: String,
        descriptionColorHex: String
    ) {
        val backgroundColor = Color.parseColor(backgroundColorHex)
        val titleColor = Color.parseColor(titleColorHex)
        val descriptionColor = Color.parseColor(descriptionColorHex)

        if (nativeAdViewLayout is NativeAdView) {
            val nativeAdView = nativeAdViewLayout as NativeAdView
            nativeAdView.setBackgroundColor(backgroundColor)
            nativeAdView.findViewById<TextView>(R.id.ad_headline)?.setTextColor(titleColor)
            nativeAdView.findViewById<TextView>(R.id.ad_body)?.setTextColor(descriptionColor)
        } else if (nativeAdViewLayout is ViewGroup) {
            val viewGroup = nativeAdViewLayout as ViewGroup
            for (child in viewGroup.children) {
                if (child is NativeAdView) {
                    child.setBackgroundColor(backgroundColor)
                    child.findViewById<TextView>(R.id.ad_headline)?.setTextColor(titleColor)
                    child.findViewById<TextView>(R.id.ad_body)?.setTextColor(descriptionColor)
                    break
                }
            }
        }
    }
}
