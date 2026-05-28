/*
Copyright 2026 Splunk Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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
