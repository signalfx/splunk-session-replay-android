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
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.view.PixelCopy
import android.view.Surface
import android.view.View
import androidx.annotation.RequiresApi
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.extensions.get
import com.splunk.android.instrumentation.recording.screenshot.cache.IntArrayCache
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

@RequiresApi(Build.VERSION_CODES.O)
internal open class ImageCopy26 : ImageCopy24() {

    final override fun copyWindow(view: View, windowDescription: Wireframe.Frame.Scene.Window, viewDescription: Wireframe.Frame.Scene.Window.View, bitmap: Bitmap) {
        val surface = try {
            view.parent?.get<Surface>("mSurface") // FIXME NoSuchFieldException on Android 16 (Wireframe > Views)
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "copyWindow", e)
            return
        }

        val inset = getSurfaceInset(view)
        srcRect.set(inset, inset, inset + view.width, inset + view.height)

        if (surface == null || !surface.isValid)
            return

        if (copyWindowPixels(surface, srcRect, bitmap))
            correctBitmapContentOffset(bitmap, viewDescription)
        else
            bitmap.eraseColor(Color.TRANSPARENT)
    }

    override fun copyWindowPixels(surface: Surface, srcRect: Rect, bitmap: Bitmap): Boolean {
        return try {
            PixelCopy.request(surface, srcRect, bitmap, onPixelCopyFinishedListener, handler)
            true
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun correctBitmapContentOffset(bitmap: Bitmap, viewDescription: Wireframe.Frame.Scene.Window.View) { // TODO Can be fixed by remove offset
        if (viewDescription.rect.top >= 0) // BottomSheetDialog with EditText - Negative value is caused by DecorView.mRootScrollY
            return

        val offset = -viewDescription.rect.top
        val pixels = IntArrayCache.obtain(bitmap.width)

        for (y in (0 until bitmap.height - offset).reversed()) {
            bitmap.getPixels(pixels, 0, bitmap.width, 0, y, bitmap.width, 1)
            bitmap.setPixels(pixels, 0, bitmap.width, 0, y + offset, bitmap.width, 1)
        }

        IntArrayCache.release(pixels)
    }

    private val onPixelCopyFinishedListener = PixelCopy.OnPixelCopyFinishedListener {}

    private companion object {
        const val TAG = "ImageCopy26"
    }
}
