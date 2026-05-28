package com.splunk.android.common.storage.filemanager

import java.io.File

interface IFileManager {
    fun readBytes(file: File): ByteArray
    fun writeBytes(file: File, bytes: ByteArray)
}
