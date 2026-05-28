package com.splunk.android.common.storage.preferences

import com.splunk.android.common.storage.cache.ISimplePermanentCache
import org.jetbrains.annotations.TestOnly

internal class TestSimplePermanentCache : ISimplePermanentCache {

    private var bytesMockFile = ByteArray(0)

    @TestOnly
    override fun readBytes(): ByteArray {
        return bytesMockFile
    }

    @TestOnly
    override fun writeBytes(bytes: ByteArray) {
        bytesMockFile = bytes
    }
}
