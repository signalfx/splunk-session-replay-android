package com.splunk.android.instrumentation.recording.interactions.model

internal data class ElementNodeInfo(
    val identity: String,
    val positionInList: Int?,
    val fragmentTag: String?
)
