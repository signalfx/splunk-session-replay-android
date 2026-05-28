package com.splunk.android.common.http.model.part

import androidx.annotation.WorkerThread
import java.io.OutputStream

abstract class AbstractContent(
    override val dispositionName: String,
    override val dispositionFileName: String?,
    override val type: String,
    override val encoding: String? = null
) : Content {

    @WorkerThread
    abstract fun copyInto(stream: OutputStream)
}
