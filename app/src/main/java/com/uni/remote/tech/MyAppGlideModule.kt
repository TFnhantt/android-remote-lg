package com.uni.remote.tech

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class MyAppGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)

        val diskCacheSizeBytes = 200 * 1024 * 1024L // 200 MB

        builder.setDiskCache(
            InternalCacheDiskCacheFactory(
                context,
                diskCacheSizeBytes
            )
        )
    }
}
