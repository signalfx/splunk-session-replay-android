package com.splunk.android.instrumentation.recording.core.data

internal enum class RenderingDataSource(internal val code: String) {
    NATIVE("NATIVE"),

    WIREFRAME("WIREFRAME");

    internal companion object {

        @JvmStatic
        fun fromString(code: String): RenderingDataSource {
            return when (code) {
                NATIVE.code -> NATIVE
                WIREFRAME.code -> WIREFRAME
                else -> NATIVE
            }
        }
    }
}

internal fun List<RenderingDataSource>.containsNative(): Boolean =
    any { it == RenderingDataSource.NATIVE }

internal fun List<RenderingDataSource>.containsWireframe(): Boolean =
    any { it == RenderingDataSource.WIREFRAME }
