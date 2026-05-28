package com.splunk.android.instrumentation.recording.core.api

enum class RenderingMode(internal val code: String) {

    /**
     * Recording screen image (what a user sees). Sensitive parts of screen, for example credit card number, can be hidden.
     *
     */
    NATIVE("native"),

    /**
     * Wireframe representation of screen data.
     */
    WIREFRAME_ONLY("wireframeOnly")
}
