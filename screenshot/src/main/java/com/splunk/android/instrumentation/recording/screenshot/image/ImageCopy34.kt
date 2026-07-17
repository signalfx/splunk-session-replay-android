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

package com.splunk.android.instrumentation.recording.screenshot.image

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.view.PixelCopy
import android.view.PixelCopy.OnPixelCopyFinishedListener
import android.view.Surface
import androidx.annotation.RequiresApi
import com.splunk.rum.common.utils.Lock

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
internal open class ImageCopy34 : ImageCopy26() {

    private val lock = Lock()

    override fun copyWindowPixels(surface: Surface, srcRect: Rect, bitmap: Bitmap): Boolean {
        lock.lock()

        val isSuccess = try {
            PixelCopy.request(surface, srcRect, bitmap, onPixelCopyFinishedListener, handler)
            true
        } catch (_: IllegalArgumentException) { // Surface can not be locked and "IllegalArgumentException: Surface isn't valid, source.isValid() == false" can be thrown
            false
        }

        lock.waitToUnlock()
        return isSuccess
    }

    private val onPixelCopyFinishedListener = OnPixelCopyFinishedListener {
        lock.unlock()
    }
}
