package com.splunk.android.common.http.model.part

import androidx.annotation.WorkerThread

sealed interface Content {

    val dispositionName: String
    val dispositionFileName: String?
    val type: String
    val encoding: String?

    @WorkerThread
    fun getLength(): Long
}
