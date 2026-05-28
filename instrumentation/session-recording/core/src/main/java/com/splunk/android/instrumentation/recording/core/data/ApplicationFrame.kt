package com.splunk.android.instrumentation.recording.core.data

import org.json.JSONObject

internal data class ApplicationFrame(
    val width: Int,
    val height: Int,
) {
    fun toJSONObject(): JSONObject {
        return JSONObject()
            .put("w", width)
            .put("h", height)
    }

    companion object {
        fun fromJSONObject(json: JSONObject): ApplicationFrame {
            return ApplicationFrame(
                width = json.getInt("w"),
                height = json.getInt("h")
            )
        }
    }
}
