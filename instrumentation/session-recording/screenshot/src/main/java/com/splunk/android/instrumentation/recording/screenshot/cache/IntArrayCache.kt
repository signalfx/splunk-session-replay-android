package com.splunk.android.instrumentation.recording.screenshot.cache

import java.util.LinkedList

internal object IntArrayCache {

    private const val RELEASE_THRESHOLD = 10000L

    private val holders = LinkedList<IntArrayHolder>()

    fun obtain(size: Int): IntArray {
        synchronized(holders) {
            for (holder in holders)
                if (holder.isUsable(size))
                    return holder.lock()

            val holder = IntArrayHolder(size)
            holders += holder

            return holder.lock()
        }
    }

    fun release(value: IntArray) {
        val releaseThreshold = System.currentTimeMillis() - RELEASE_THRESHOLD

        synchronized(holders) {
            val iterator = holders.iterator()

            while (iterator.hasNext()) {
                val holder = iterator.next()

                if (value in holder)
                    holder.unlock()
                else if (holder.isOlder(releaseThreshold))
                    iterator.remove()
            }
        }
    }

    private class IntArrayHolder(size: Int) {

        private val value = IntArray(size)

        private var lastUsageTimestamp = 0L
        private var isLocked = false

        fun lock(): IntArray {
            lastUsageTimestamp = System.currentTimeMillis()
            isLocked = true

            return value
        }

        fun unlock() {
            isLocked = false
        }

        fun isUsable(size: Int): Boolean {
            return !isLocked && value.size == size
        }

        fun isOlder(timestamp: Long): Boolean {
            return !isLocked && lastUsageTimestamp < timestamp
        }

        operator fun contains(value: IntArray): Boolean {
            return this.value === value
        }

        override fun toString(): String {
            return "IntArrayHolder(size: ${value.size}, isLocked: $isLocked)"
        }
    }
}
