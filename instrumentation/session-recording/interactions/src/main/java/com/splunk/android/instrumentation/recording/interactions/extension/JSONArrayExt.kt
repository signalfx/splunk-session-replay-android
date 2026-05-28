package com.splunk.android.instrumentation.recording.interactions.extension

import org.json.JSONArray

internal fun JSONArray.toIntArray(): IntArray {
    return IntArray(length()) { getInt(it) }
}
