package com.splunk.android.instrumentation.recording.core.data

import com.splunk.android.common.id.NanoId
import com.splunk.android.common.utils.extensions.map
import com.splunk.android.common.utils.extensions.toJSONArray
import com.splunk.android.instrumentation.recording.core.Constants
import com.splunk.android.instrumentation.recording.core.video.VideoFrameProcessingUtil
import org.json.JSONArray
import org.json.JSONObject

internal class DataChunk private constructor(
    val id: String,

    var interactions: JSONArray? = null,
    var userActivity: List<Long>? = null,

    val renderingDataSources: List<RenderingDataSource> = emptyList(),

    // Timestamps
    val timeStart: Long = 0L,
    var timeEnd: Long = 0L,

    // Video & screen dimensions
    var videoWidth: Int = 0,
    var videoHeight: Int = 0,

    var applicationFrame: ApplicationFrame? = null,

    // Video settings
    val bitrate: Long = 0L,
    val frameRate: Int = 0,

    val scheme: String = "1.0.0"
) {

    fun close(closingTimestamp: Long) {
        this.timeEnd = closingTimestamp
    }

    fun setDimensions(frame: ApplicationFrame) {
        if (getVideoSize() == VideoSize.EMPTY) {
            val videoSize = VideoFrameProcessingUtil.calculateVideoSize(frame, Constants.DEFAULT_MAX_VIDEO_HEIGHT)
            // Screen
            this.applicationFrame = frame

            // Video
            videoWidth = videoSize.width
            videoHeight = videoSize.height
        }
    }

    fun getVideoSize(): VideoSize = VideoSize(videoWidth, videoHeight)

    companion object {

        fun create(
            startTimestamp: Long,
            bitrate: Long,
            frameRate: Int,
            renderingDataSources: List<RenderingDataSource>
        ): DataChunk {
            return DataChunk(
                id = NanoId.generate(),
                timeStart = startTimestamp,
                bitrate = bitrate,
                frameRate = frameRate,
                renderingDataSources = renderingDataSources
            )
        }

        fun fromJSONObject(json: JSONObject) = DataChunk(
            id = json.getString("id"),
            interactions = json.getJSONArray("interactions"),
            userActivity = json.getJSONArray("userActivity").map { array, index -> array.getLong(index) },
            renderingDataSources = json.getJSONArray("renderingDataSources")
                .map { array, index -> RenderingDataSource.fromString(array.getString(index)) },
            timeStart = json.getLong("timeStart"),
            timeEnd = json.getLong("timeEnd"),
            applicationFrame = ApplicationFrame.fromJSONObject(json.getJSONObject("applicationFrame")),
            videoWidth = json.getInt("videoWidth"),
            videoHeight = json.getInt("videoHeight"),
            bitrate = json.getLong("bitrate"),
            frameRate = json.getInt("frameRate"),
            scheme = json.getString("scheme")
        )
    }

    fun toJSONObject(): JSONObject {
        return JSONObject()
            .put("timeStart", timeStart)
            .put("timeEnd", timeEnd)
            .put("applicationFrame", applicationFrame?.toJSONObject())
            .put("id", id)
            .put("videoWidth", videoWidth)
            .put("videoHeight", videoHeight)
            .put("bitrate", bitrate)
            .put("frameRate", frameRate)
            .put("interactions", interactions)
            .put("userActivity", JSONArray(userActivity))
            .put("renderingDataSources", renderingDataSources.toJSONArray { array, item -> array.put(item.code) })
            .put("scheme", scheme)
    }
}
