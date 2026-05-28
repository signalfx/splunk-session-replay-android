package com.splunk.android.common.storage

import com.splunk.android.common.logger.Logger
import com.splunk.android.common.storage.cache.IPermanentCache
import java.io.File

class Storage(private val permanentCache: IPermanentCache) : IStorage {

    override fun readBytes(file: File): ByteArray? {
        val bytes = try {
            permanentCache.readBytes(file.absolutePath)
        } catch (e: Exception) {
            Logger.d(TAG, "readBytes(): file = ${file.name} - failed with Exception: ${e.message}")
            return null
        }

        Logger.d(TAG, "readBytes(): file = ${file.name}")
        return bytes
    }

    override fun writeBytes(file: File, bytes: ByteArray): Boolean {
        try {
            permanentCache.writeBytes(file.absolutePath, bytes)
        } catch (e: Exception) {
            Logger.d(TAG, "writeBytes(): file = ${file.name}, bytes = ${bytes.size} - failed with Exception: ${e.message}")
            return false
        }

        Logger.d(TAG, "writeBytes(): file = ${file.name}, bytes = ${bytes.size}")
        return true
    }

    override fun readText(file: File): String? {
        return readBytes(file)?.toString(Charsets.UTF_8)
    }

    override fun writeText(file: File, text: String, append: Boolean): Boolean {
        val bytes = if (append) {
            val actualText = readText(file) ?: ""
            (actualText + text).toByteArray()
        } else
            text.toByteArray()

        return writeBytes(file, bytes)
    }

    private companion object {
        const val TAG = "Storage"
    }
}
