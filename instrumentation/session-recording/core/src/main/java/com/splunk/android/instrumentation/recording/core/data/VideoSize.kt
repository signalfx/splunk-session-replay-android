package com.splunk.android.instrumentation.recording.core.data

import org.json.JSONObject

internal data class VideoSize(var width: Int, var height: Int) {

    fun toJSONObject(): JSONObject {
        return JSONObject()
            .put("width", width)
            .put("height", height)
    }

    companion object {
        val EMPTY = VideoSize(0, 0)

        fun fromJSONObject(jsonObject: JSONObject): VideoSize {
            return VideoSize(
                jsonObject.getInt("width"),
                jsonObject.getInt("height")
            )
        }
    }
}
