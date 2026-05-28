package com.splunk.android.common.encoder.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.splunk.android.common.logger.Logger

internal object BitmapLoader {

    private const val TAG = "BitmapLoader"

    fun loadBitmap(filePath: String, width: Int, height: Int): Bitmap {
        val bitmap = decodeSampledBitmapFromFile(filePath, width, height)
        Logger.d(TAG, "loadBitmap() width: $width, height: $height, bitmapWidth: ${bitmap.width}, bitmapHeight: ${bitmap.height}")

        // TODO: Fix upscaling to be done during encoding.
        return if (bitmap.width != width || bitmap.height != height) {
            val scaled = Bitmap.createScaledBitmap(bitmap, width, height, true)
            bitmap.recycle()
            scaled
        } else {
            bitmap
        }
    }

    private fun decodeSampledBitmapFromFile(
        filePath: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inDither = true

        return BitmapFactory.decodeFile(filePath, options)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize > reqHeight &&
                halfWidth / inSampleSize > reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
