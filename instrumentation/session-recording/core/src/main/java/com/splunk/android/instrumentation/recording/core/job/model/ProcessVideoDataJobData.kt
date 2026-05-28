package com.splunk.android.instrumentation.recording.core.job.model

import org.json.JSONObject

internal class ProcessVideoDataJobData(
    val dataChunkId: String
) {
    fun toJSONObject(): JSONObject {
        return JSONObject()
            .put(DATA_CHUNK_ID, dataChunkId)
    }

    companion object {
        private const val DATA_CHUNK_ID = "DATA_CHUNK_ID"
        fun fromJSONObject(jsonObject: JSONObject): ProcessVideoDataJobData {
            return ProcessVideoDataJobData(
                jsonObject.getString(DATA_CHUNK_ID),
            )
        }
    }
}
