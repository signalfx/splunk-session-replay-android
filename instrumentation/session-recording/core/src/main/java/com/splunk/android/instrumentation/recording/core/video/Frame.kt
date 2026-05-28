package com.splunk.android.instrumentation.recording.core.video

import org.json.JSONObject

internal class Frame(
    var fileName: String,
    var duration: Long,
    var generalTime: Long,
) {

    constructor(
        batchFrameNumber: Int,
        duration: Long,
        generalTime: Long,
    ) : this("$batchFrameNumber.jpg", duration, generalTime)

    fun toJSONObject(): JSONObject {
        return JSONObject()
            .put("fileName", fileName)
            .put("duration", duration)
            .put("generalTime", generalTime)
    }

    companion object {
        fun fromJSONObject(jsonObject: JSONObject) = Frame(
            jsonObject.getString("fileName"),
            jsonObject.getLong("duration"),
            jsonObject.getLong("generalTime"),
        )
    }
}

internal fun List<Frame>.addLimitFrames(startTimestamp: Long, closeTimestamp: Long): List<Frame> {
    return buildList {
        addAll(this@addLimitFrames)
        if (this.isNotEmpty()) {
            val lastFrame = this.last()
            add(
                Frame(
                    fileName = lastFrame.fileName,
                    duration = closeTimestamp - lastFrame.generalTime,
                    generalTime = closeTimestamp,
                )
            )
            val firstFrame = this.first()
            if (firstFrame.generalTime != startTimestamp) {
                add(
                    0, Frame(
                        fileName = firstFrame.fileName,
                        duration = firstFrame.generalTime - startTimestamp,
                        generalTime = startTimestamp,
                    )
                )
            }
        }
    }
}
