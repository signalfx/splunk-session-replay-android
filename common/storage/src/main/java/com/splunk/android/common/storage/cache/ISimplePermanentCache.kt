package com.splunk.android.common.storage.cache

interface ISimplePermanentCache {
    fun readBytes(): ByteArray?
    fun writeBytes(bytes: ByteArray)
}
