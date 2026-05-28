package com.splunk.android.common.utils.extensions

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

fun ByteArray.compress(): ByteArray {
    val outputStream = ByteArrayOutputStream(size)

    val deflaterStream = DeflaterOutputStream(outputStream, Deflater(Deflater.BEST_COMPRESSION))
    deflaterStream.write(this)
    deflaterStream.close()

    return outputStream.toByteArray()
}

fun ByteArray.decompress(): ByteArray? {
    return runCatching { InflaterInputStream(ByteArrayInputStream(this)).readBytes() }.getOrNull()
}
