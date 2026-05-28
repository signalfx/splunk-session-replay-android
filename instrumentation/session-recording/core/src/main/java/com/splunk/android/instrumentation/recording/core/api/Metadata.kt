package com.splunk.android.instrumentation.recording.core.api

import org.json.JSONArray
import org.json.JSONObject

data class Metadata(
    val startUnixMs: Long,
    val endUnixMs: Long,
    val userActivity: List<Long>?
) {

    fun toJSONObject(): JSONObject {
        return JSONObject()
            .put("startUnixMs", startUnixMs)
            .put("endUnixMs", endUnixMs)
            .put("userActivity", JSONArray(userActivity))
            .put("source", "mobile")
    }
}
