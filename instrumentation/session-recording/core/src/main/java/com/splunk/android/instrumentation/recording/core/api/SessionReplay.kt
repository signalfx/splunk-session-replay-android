package com.splunk.android.instrumentation.recording.core.api

import com.splunk.android.instrumentation.recording.core.Initializer
import com.splunk.android.instrumentation.recording.core.api.handler.CoreApiHandler
import com.splunk.android.instrumentation.recording.core.api.handler.PreferencesApiHandler
import com.splunk.android.instrumentation.recording.core.api.handler.SensitivityApiHandler
import com.splunk.android.instrumentation.recording.core.api.handler.StateApiHandler

class SessionReplay internal constructor(
    private val coreApi: CoreApiHandler,
    preferencesApi: PreferencesApiHandler,
    stateApi: StateApiHandler,
    sensitivityApi: SensitivityApiHandler
) {

    companion object {

        /**
         * Returns instance of the SessionReplay.
         */
        @get:JvmStatic
        val instance: SessionReplay
            get() = Initializer.instance
    }

    val dataListeners: MutableCollection<DataListener> = coreApi.dataListeners

    /**
     * Preferred configuration. The entered values represent only the preferred configuration. The resulting state may be different according to your
     * account settings.
     *
     * @see state
     */
    val preferences: Preferences = Preferences(preferencesApi)

    /**
     * The current SDK state. Each value is combination of default one and [preferences].
     */
    val state: State = State(stateApi)

    /**
     * Sensitivity configuration defines which part of screen will not be visible. Used only when [State.renderingMode] is [RenderingMode.NATIVE].
     */
    val sensitivity: Sensitivity = Sensitivity(sensitivityApi)

    /**
     * Recording mask configuration defines which part of screen will not be visible. Used only when [State.renderingMode] is [RenderingMode.NATIVE].
     */
    var recordingMask: RecordingMask?
        get() = coreApi.recordingMask
        set(value) {
            coreApi.recordingMask = value
        }

    /**
     * Starts recording of a user activity.
     */
    fun start() {
        coreApi.start()
    }

    /**
     * Stops recording of a user activity.
     */
    fun stop() {
        coreApi.stop()
    }

    /**
     * Forces new data chunk to be created and the current one if there is any to be processed.
     */
    fun newDataChunk() {
        coreApi.newDataChunk()
    }

    /**
     * Stops recording and resets [preferences] into the default state.
     */
    fun reset() {
        coreApi.reset()
    }
}
