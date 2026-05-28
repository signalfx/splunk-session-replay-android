package com.splunk.android.common.storage.cache

import com.splunk.android.common.storage.filemanager.IFileManager
import java.io.File

class FileSimplePermanentCache(
    file: File,
    fileManager: IFileManager
) : FilePermanentCache(fileManager), ISimplePermanentCache {

    private val filePath: String = file.absolutePath

    override fun readBytes(): ByteArray? {
        return readBytes(filePath)
    }

    override fun writeBytes(bytes: ByteArray) {
        return writeBytes(filePath, bytes)
    }
}
