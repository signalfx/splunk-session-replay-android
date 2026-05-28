package com.splunk.android.debugger.util

import android.graphics.Bitmap
import android.util.SparseArray

internal class BitmapCache {

    private val cache = SparseArray<Bitmap>()

    fun get(width: Int, height: Int): Bitmap {
        val key = createKey(width, height)
        var item = cache.get(key)

        if (item == null) {
            item = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            cache.put(key, item)
        }

        return item
    }

    private fun createKey(width: Int, height: Int): Int {
        return height * 1000 + width
    }
}
