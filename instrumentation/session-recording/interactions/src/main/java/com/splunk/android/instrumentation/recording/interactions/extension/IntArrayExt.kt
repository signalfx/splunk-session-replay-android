package com.splunk.android.instrumentation.recording.interactions.extension

import org.json.JSONArray

internal fun IntArray.toJSONArray(): JSONArray {
    val array = JSONArray()

    for (value in this)
        array.put(value)

    return array
}
