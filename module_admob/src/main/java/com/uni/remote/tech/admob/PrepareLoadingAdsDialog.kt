package com.uni.remote.tech.admob

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup

class PrepareLoadingAdsDialog(
    context: Context
) : Dialog(context, R.style.PrepareLoadingAdsDialogTheme) {
    init {
        setContentView(R.layout.dialog_preparing_ad)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }
}
