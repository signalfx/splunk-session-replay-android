package com.splunk.android.common.storage.filemanager

import java.io.File

class PlainFileManager : IFileManager {

    override fun readBytes(file: File): ByteArray {
        return file.readBytes()
    }

    override fun writeBytes(file: File, bytes: ByteArray) {
        file.writeBytes(bytes)
    }
}
