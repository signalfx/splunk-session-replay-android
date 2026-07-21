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
