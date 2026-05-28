package com.splunk.android.instrumentation.recording.wireframe.util

import android.util.SparseArray

internal class PixelMatrixCache {

    private val cache = SparseArray<PixelMatrix>()

    fun get(width: Int, height: Int): PixelMatrix {
        val key = createKey(width, height)
        var item = cache.get(key)

        if (item == null) {
            item = PixelMatrix(width, height)
            cache.put(key, item)
        } else
            item.reset()

        return item
    }

    private fun createKey(width: Int, height: Int): Int {
        return height * 1000 + width
    }
}
