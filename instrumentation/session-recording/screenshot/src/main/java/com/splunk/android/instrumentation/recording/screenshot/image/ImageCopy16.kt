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
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import androidx.annotation.RequiresApi
import com.splunk.android.common.utils.extensions.barrier
import com.splunk.android.common.utils.runOnUiThread
import com.splunk.android.common.utils.runOnUiThreadSync
import com.splunk.android.instrumentation.recording.screenshot.cache.CanvasCache
import com.splunk.android.instrumentation.recording.screenshot.extension.forEach
import com.splunk.android.instrumentation.recording.wireframe.extension.findViewByInstance
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

@RequiresApi(Build.VERSION_CODES.KITKAT)
internal class ImageCopy16 : ImageCopy {

    private val paint = Paint()

    init {
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
    }

    override fun copyWindow(view: View, windowDescription: Wireframe.Frame.Scene.Window, viewDescription: Wireframe.Frame.Scene.Window.View, bitmap: Bitmap) {
        bitmap.eraseColor(Color.TRANSPARENT)

        val canvas = CanvasCache.obtain()
        canvas.setBitmap(bitmap)

        val isLaidOut: Boolean

        runOnUiThreadSync {
            isLaidOut = view.isLaidOut

            if (isLaidOut) {
                view.background?.draw(canvas)

                val saveCount = canvas.save()
                canvas.translate(-view.scrollX.toFloat(), -view.scrollY.toFloat())
                view.draw(canvas)
                canvas.restoreToCount(saveCount)
            }
        }

        if (isLaidOut)
            barrier(0) { barrier ->
                view.forEach {
                    if (it is TextureView) {
                        val textureViewDescription = windowDescription.findViewByInstance(it)

                        if (textureViewDescription != null && textureViewDescription.isSensitive != true) {
                            barrier.increase()

                            runOnUiThread {
                                val textureBitmap = it.bitmap ?: return@runOnUiThread
                                canvas.drawBitmap(textureBitmap, null, textureViewDescription.rect, paint)
                                barrier.decrease()
                            }
                        }
                    }
                }
            }

        CanvasCache.release(canvas)
    }

    // TODO Maybe somehow? https://source.android.com/devices/graphics, https://github.com/google/grafika, https://stackoverflow.com/questions/27486164/how-to-take-snapshot-of-surfaceview/, https://stackoverflow.com/questions/27817577/android-take-screenshot-of-surface-view-shows-black-screen/27824250#27824250
    override fun copySurface(view: SurfaceView, bitmap: Bitmap) {
        bitmap.eraseColor(Color.BLACK)
    }
}
