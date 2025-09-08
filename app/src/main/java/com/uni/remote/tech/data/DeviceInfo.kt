package com.fox.lgremote.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceInfo(
    val ipAddress: String?,
    val friendlyName: String?,
    val modelName: String?,
    val manufacturer: String?,
    val modelNumber: String?
) : Parcelable
