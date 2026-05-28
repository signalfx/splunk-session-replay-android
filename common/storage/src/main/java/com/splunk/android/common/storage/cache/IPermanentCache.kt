package com.splunk.android.common.storage.cache

interface IPermanentCache {
    fun readBytes(key: String): ByteArray?
    fun writeBytes(key: String, bytes: ByteArray)
}
