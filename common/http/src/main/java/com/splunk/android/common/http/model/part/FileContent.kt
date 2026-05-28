package com.splunk.android.common.http.model.part

import androidx.annotation.WorkerThread
import java.io.File

data class FileContent(
    override val dispositionName: String,
    override val dispositionFileName: String?,
    override val type: String,
    override val encoding: String? = null,
    val file: File,
) : Content {

    @WorkerThread
    override fun getLength(): Long {
        return file.length()
    }
}
