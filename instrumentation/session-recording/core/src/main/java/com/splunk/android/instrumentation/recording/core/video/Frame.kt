/*
Copyright 2026 Splunk Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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
