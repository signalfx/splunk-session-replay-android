package com.splunk.android.instrumentation.recording.core.api

sealed interface Status {

    /**
     * Whether this is [Recording] status.
     */
    val isRecording: Boolean
        get() = this is Recording

    /**
     * SDK is recording of an user's activity.
     */
    object Recording : Status

    /**
     * SDK is not recording of an user's activity.
     *
     * @param cause Reason of not recording.
     */
    data class NotRecording internal constructor(val cause: Cause) : Status {

        enum class Cause {

            /**
             * Recording have not been started.
             *
             * @see Smartlook.start
             */
            NOT_STARTED,

            /**
             * Recording was stopped.
             */
            STOPPED,

            /**
             * The device's Android sdk is below supported minimum.
             */
            BELOW_MIN_SDK_VERSION,

            /**
             * The device's storage is too low to start recording.
             */
            STORAGE_LIMIT_REACHED,

            /**
             *  It was impossible to start the recording because the internal
             *  database could not be open, or another internal error occurred.
             */
            INTERNAL_ERROR
        }
    }
}
