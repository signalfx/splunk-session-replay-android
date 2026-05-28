package com.splunk.android.common.http.model.part

import androidx.annotation.WorkerThread

data class StringContent(
    override val dispositionName: String,
    override val dispositionFileName: String?,
    override val type: String,
    val string: String
) : Content {

    override val encoding: String? = null

    @WorkerThread
    override fun getLength(): Long {
        return string.length.toLong()
    }
}
