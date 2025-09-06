package com.uni.remote.tech.admob.nativeAd

import com.google.android.gms.ads.nativead.NativeAd

fun <T> sufferList(
    items: List<T>,
    ads: List<NativeAd>,
    startAdIndex: Int = 2,
    offset: Int = 2,
    maxAdToShow: Int = 5 // Set 0 to maximum
): List<ItemOrAd<T>> {
    check(startAdIndex >= 0) { "startAdIndex cannot be less than 0" }
    check(offset > 0) { "offset cannot be less than or equal to 0" }
    check(maxAdToShow >= 0) { "offset cannot be less than 0" }

    val itemsMapped = items.map { ItemOrAd.Item(it) }
    val adsMapped = ads.map { ItemOrAd.Ad(it) }

    val newList: MutableList<ItemOrAd<T>> = ArrayList(itemsMapped)
    var numAds = 0

    if (adsMapped.isNotEmpty() && itemsMapped.isNotEmpty()) {
        var y = 0
        var i = if (itemsMapped.count() < startAdIndex) items.count() else startAdIndex
        while (i <= newList.size) {
            if (numAds < maxAdToShow && maxAdToShow != 0) {
                newList.add(i, adsMapped[y])
                numAds++
            } else if (maxAdToShow == 0) {
                newList.add(i, adsMapped[y])
                numAds++
            }
            if (y == ads.size - 1) {
                y = 0
            } else {
                y++
            }
            i += (offset + 1)
        }
    }

    return newList.toList()
}
