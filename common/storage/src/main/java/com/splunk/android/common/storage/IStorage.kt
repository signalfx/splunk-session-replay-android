package com.splunk.android.common.storage

import java.io.File

interface IStorage {

    fun readBytes(file: File): ByteArray?
    fun writeBytes(file: File, bytes: ByteArray): Boolean

    fun readText(file: File): String?
    fun writeText(file: File, text: String, append: Boolean = false): Boolean
}
