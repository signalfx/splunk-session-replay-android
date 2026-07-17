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
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.extensions.get
import com.splunk.rum.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.screenshot.cache.BitmapCache
import com.splunk.android.instrumentation.recording.screenshot.cache.CanvasCache
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import kotlin.math.ceil

@RequiresApi(Build.VERSION_CODES.N)
internal open class ImageCopy24 : ImageCopy {

    protected val handler: Handler

    protected val srcRect = Rect()
    private val dstRect = Rect()

    init {
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()

        handler = Handler(handlerThread.looper)
    }

    override fun copyWindow(view: View, windowDescription: Wireframe.Frame.Scene.Window, viewDescription: Wireframe.Frame.Scene.Window.View, bitmap: Bitmap) {
        val surface = try {
            view.parent?.get<Surface>("mSurface")
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "copyWindow", e)
            return
        }

        if (surface == null || !surface.isValid)
            return

        val inset = getSurfaceInset(view)

        if (inset == 0) {
            if (!copyWindowPixels(surface, srcRect, bitmap))
                bitmap.eraseColor(Color.TRANSPARENT)
        } else {
            val tempBitmap = BitmapCache.obtain(view.width + inset * 2, view.height + inset * 2)
            val isSuccess = copyWindowPixels(surface, srcRect, tempBitmap)

            if (isSuccess) {
                srcRect.set(inset, inset, inset + view.width, inset + view.height)
                dstRect.set(0, 0, view.width, view.height)

                val canvas = CanvasCache.obtain()
                canvas.setBitmap(bitmap)
                canvas.drawBitmap(tempBitmap, srcRect, dstRect, null)
                CanvasCache.release(canvas)
            }

            BitmapCache.release(tempBitmap)
        }
    }

    /**
     * Copy [surface] pixels into [bitmap].
     *
     * Surface can not be locked and "IllegalArgumentException: Surface isn't valid, source.isValid() == false" can be thrown.
     * Implementation must catch IllegalArgumentException.
     *
     * @return whether the pixel copy was success.
     */
    protected open fun copyWindowPixels(surface: Surface, srcRect: Rect, bitmap: Bitmap): Boolean {
        return try {
            PixelCopy.request(surface, bitmap, onPixelCopyFinishedListener, handler)
            true
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    override fun copySurface(view: SurfaceView, bitmap: Bitmap) {
        if (!view.holder.surface.isValid)
            return

        try {
            PixelCopy.request(view, bitmap, onPixelCopyFinishedListener, handler)
        } catch (_: IllegalArgumentException) {
        }
    }

    protected fun getSurfaceInset(view: View): Int { // WindowManager.setSurfaceInsets
        return when (view::class.java) {
            POPUP_DECOR_VIEW_CLASS -> { // PopupWindow.preparePopup - Can be read from background view, but because of interactions, it have to be parsed from String
                val layoutParams = view.layoutParams as? WindowManager.LayoutParams ?: return 0
                val layoutParamsString = layoutParams.toString() // ... surfaceInsets=Rect(84, 84 - 84, 84) (manual) ...
                val surfaceInsetsIndex = layoutParamsString.indexOf("surfaceInsets=")

                if (surfaceInsetsIndex == -1)
                    return 0

                val commaIndex = layoutParamsString.indexOf(',', surfaceInsetsIndex)

                if (commaIndex == -1)
                    return 0

                val insetString = layoutParamsString.substring(surfaceInsetsIndex + 19, commaIndex)

                insetString.toIntOrNull() ?: 0
            }
            else -> {
                ceil(view.z * 2).toInt()
            }
        }
    }

    private val onPixelCopyFinishedListener = PixelCopy.OnPixelCopyFinishedListener {}

    private companion object {
        const val TAG = "ImageCopy24"

        val POPUP_DECOR_VIEW_CLASS = "android.widget.PopupWindow\$PopupDecorView".toClass()
    }
}
