package com.splunk.android.instrumentation.recording.screenshot.cache

import android.graphics.Bitmap
import android.graphics.Color
import com.splunk.android.instrumentation.recording.screenshot.extension.removeFirst
import java.util.LinkedList

internal object BitmapCache {

    private const val RELEASE_THRESHOLD = 10000L

    private val holders = LinkedList<BitmapHolder>()

    @Synchronized
    fun obtain(width: Int, height: Int): Bitmap {
        for (holder in holders)
            if (holder.isUsable(width, height)) {
                val bitmap = holder.lock()
                releaseOldUnusedBitmaps()
                return bitmap
            }

        releaseOldUnusedBitmaps()

        val holder = createBitmapHolder(width, height)
        holders += holder

        return holder.lock()
    }

    @Synchronized
    fun release(bitmap: Bitmap) {
        releaseOldUnusedBitmaps()
        holders.find { bitmap in it }?.unlock()
    }

    @Synchronized
    private fun releaseOldUnusedBitmaps() {
        val releaseThreshold = System.currentTimeMillis() - RELEASE_THRESHOLD
        val iterator = holders.iterator()

        while (iterator.hasNext()) {
            val holder = iterator.next()

            if (holder.isUnusedAndOlder(releaseThreshold)) {
                holder.release()
                iterator.remove()
            }
        }
    }

    @Synchronized
    private fun createBitmapHolder(width: Int, height: Int): BitmapHolder {
        return try {
            BitmapHolder(width, height)
        } catch (e: OutOfMemoryError) {
            if (!releaseOldestUnusedBitmap())
                throw e

            createBitmapHolder(width, height)
        }
    }

    @Synchronized
    private fun releaseOldestUnusedBitmap(): Boolean {
        val holder = holders.asSequence()
            .filter { !it.isLocked }
            .minByOrNull { it.lastUsageTimestamp }
            ?: return false

        holders.removeFirst { it === holder }
        holder.release()

        return true
    }

    private class BitmapHolder(width: Int, height: Int) {

        private val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        var lastUsageTimestamp = 0L
            private set

        @Volatile
        var isLocked = false
            private set

        fun lock(): Bitmap {
            bitmap.eraseColor(Color.TRANSPARENT)
            isLocked = true

            return bitmap
        }

        fun unlock() {
            lastUsageTimestamp = System.currentTimeMillis()
            isLocked = false
        }

        fun isUsable(width: Int, height: Int): Boolean {
            return !isLocked && width == bitmap.width && height == bitmap.height && !bitmap.isRecycled
        }

        fun isUnusedAndOlder(timestamp: Long): Boolean {
            return !isLocked && lastUsageTimestamp < timestamp
        }

        fun release() {
            bitmap.recycle()
        }

        operator fun contains(bitmap: Bitmap): Boolean {
            return this.bitmap === bitmap
        }

        override fun toString(): String {
            return "BitmapHolder(width: ${bitmap.width}, height: ${bitmap.height}, isLocked: $isLocked)"
        }
    }
}
