package com.splunk.android.instrumentation.recording.screenshot.image

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.view.PixelCopy
import android.view.PixelCopy.OnPixelCopyFinishedListener
import android.view.Surface
import androidx.annotation.RequiresApi
import com.splunk.android.common.utils.Lock

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
