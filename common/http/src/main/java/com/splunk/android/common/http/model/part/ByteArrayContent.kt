package com.splunk.android.common.http.model.part

import androidx.annotation.WorkerThread

class ByteArrayContent(
    override val dispositionName: String,
    override val dispositionFileName: String?,
    override val type: String,
    override val encoding: String? = null,
    val bytes: ByteArray
) : Content {

    @WorkerThread
    override fun getLength(): Long {
        return bytes.size.toLong()
    }

    override fun toString(): String {
        return "ByteArrayPart(dispositionName=$dispositionName, dispositionFileName=$dispositionFileName, type=$type, encoding=$encoding, bytesSize=${bytes.size})"
    }
}
