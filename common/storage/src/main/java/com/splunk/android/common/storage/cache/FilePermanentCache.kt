package com.splunk.android.common.storage.cache

import com.splunk.android.common.logger.Logger
import com.splunk.android.common.storage.extensions.createNewFileOnPath
import com.splunk.android.common.storage.extensions.toFile
import com.splunk.android.common.storage.filemanager.IFileManager

open class FilePermanentCache(
    private val fileManager: IFileManager
) : IPermanentCache {

    override fun readBytes(key: String): ByteArray? {
        val file = key.toFile()

        if (!file.exists()) {
            return null
        }

        return try {
            fileManager.readBytes(file)
        } catch (e: Exception) {
            Logger.w(TAG, "loadBytes(): Failed to load bytes from a file due to ${e.message}!")
            null
        }
    }

    override fun writeBytes(key: String, bytes: ByteArray) {
        val file = key.toFile()

        try {
            file.createNewFileOnPath()
            fileManager.writeBytes(file, bytes)
        } catch (e: Exception) {
            Logger.w(TAG, "saveBytes(): Failed to save bytes due to ${e.message}!")
        }
    }

    private companion object {
        const val TAG = "FilePermanentCache"
    }
}
