package com.splunk.android.common.encoder.model

data class VideoFrame(
    val filePath: String,
    val duration: Long,
    val orientation: Orientation? = null
) {

    enum class Orientation(val angle: Int) {
        PORTRAIT(0), LANDSCAPE(90)
    }
}
