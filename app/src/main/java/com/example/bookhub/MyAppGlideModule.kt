package com.example.bookhub

import android.R
import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.target.ViewTarget
import java.io.File


@GlideModule
class MyAppGlideModule : AppGlideModule() {
//    override fun applyOptions(context: Context, builder: GlideBuilder) {
//        super.applyOptions(context, builder)
//        // Use a disk cache strategy that persists across app launches
//        builder.setDiskCache(DiskLruCacheFactory(
//            File(context.cacheDir!!, "my_image_cache"),
//            DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE.toLong()))
//    }
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(
            DiskLruCacheFactory(
                { context.externalCacheDir!! },
                DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE.toLong()
            )
        )
    }
}
