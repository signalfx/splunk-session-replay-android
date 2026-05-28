package com.splunk.android.instrumentation.recording.screenshot.cache

import android.graphics.Canvas
import java.util.LinkedList

internal object CanvasCache {

    private const val RELEASE_THRESHOLD = 10000L

    private val holders = LinkedList<CanvasHolder>()

    fun obtain(): Canvas {
        synchronized(holders) {
            for (holder in holders)
                if (holder.isUsable())
                    return holder.lock()

            val holder = CanvasHolder()
            holders += holder

            return holder.lock()
        }
    }

    fun release(canvas: Canvas) {
        val releaseThreshold = System.currentTimeMillis() - RELEASE_THRESHOLD

        synchronized(holders) {
            val iterator = holders.iterator()

            while (iterator.hasNext()) {
                val holder = iterator.next()

                if (canvas in holder)
                    holder.unlock()
                else if (holder.isOlder(releaseThreshold))
                    iterator.remove()
            }
        }
    }

    private class CanvasHolder {

        private val canvas = Canvas()

        private var lastUsageTimestamp = 0L
        private var isLocked = false

        fun lock(): Canvas {
            lastUsageTimestamp = System.currentTimeMillis()
            isLocked = true

            return canvas
        }

        fun unlock() {
            isLocked = false
        }

        fun isUsable(): Boolean {
            return !isLocked
        }

        fun isOlder(timestamp: Long): Boolean {
            return !isLocked && lastUsageTimestamp < timestamp
        }

        operator fun contains(canvas: Canvas): Boolean {
            return this.canvas === canvas
        }

        override fun toString(): String {
            return "CanvasHolder(isLocked: $isLocked)"
        }
    }
}
