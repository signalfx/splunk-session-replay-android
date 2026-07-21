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

package com.splunk.android.instrumentation.recording.core

import com.splunk.android.instrumentation.recording.core.api.Status
import com.splunk.android.instrumentation.recording.core.configuration.IConfigurationHandler
import com.splunk.android.instrumentation.recording.core.configuration.toStatus
import com.splunk.android.instrumentation.recording.core.crash.ICrashHandler
import com.splunk.android.instrumentation.recording.core.lifecycle.ILifecycleHandler
import com.splunk.android.instrumentation.recording.core.metadata.IMetadataHandler
import com.splunk.android.instrumentation.recording.core.video.IScreenCapturer
import java.util.concurrent.atomic.AtomicBoolean

internal class Core(
    private val screenCapturer: IScreenCapturer,
    private val lifecycleHandler: ILifecycleHandler,
    private val metadataHandler: IMetadataHandler,
    private val configurationHandler: IConfigurationHandler,
    private val crashHandler: ICrashHandler,
) {

    private val started: AtomicBoolean = AtomicBoolean(false)
    private val startedAtLeastOnce: AtomicBoolean = AtomicBoolean(false)

    /**
     * Current [Status] of the SDK recording. Before [start] is called. The status is
     * [Status.NotRecording.Cause.NOT_STARTED]
     *
     * 1) [Status.Recording] -> [start] was called and SDK is actually recording
     * 2) [Status.NotRecording] -> SDK is not recording, if the user called the [stop].
     * the [Status.NotRecording.Cause] will be [Status.NotRecording.Cause.STOPPED]
     */
    val status: Status
        get() {
            if (isBelowAndroidSDKMinVersion()) {
                return Status.NotRecording(cause = Status.NotRecording.Cause.BELOW_MIN_SDK_VERSION)
            }

            if (!startedAtLeastOnce.get()) {
                return Status.NotRecording(cause = Status.NotRecording.Cause.NOT_STARTED)
            }

            if (!started.get()) {
                return Status.NotRecording(cause = Status.NotRecording.Cause.STOPPED)
            }

            return configurationHandler.recordingState().toStatus()
        }

    fun start() {
        if (isBelowAndroidSDKMinVersion()) {
            return
        }

        startedAtLeastOnce.set(true)
        if (!started.get()) {
            started.set(true)
            lifecycleHandler.startRecording()
            screenCapturer.start()
            crashHandler.register()
        } else {
            // TODO SR
        }
    }

    fun stop() {
        if (started.get()) {
            started.set(false)
            lifecycleHandler.stopRecording()
            screenCapturer.stop()
            crashHandler.unregister()
        } else {
            // TODO SR
        }
    }

    fun newDataChunk() {
        if (started.get()) {
            screenCapturer.newDataChunk()
        } else {
            // TODO SR
        }
    }

    fun reset() {
        // Stop recording.
        if (started.get()) {
            started.set(false)
            lifecycleHandler.stopRecording()
            screenCapturer.stop()
        }

        // Reset state.
        startedAtLeastOnce.set(false)
    }

    private fun isBelowAndroidSDKMinVersion(): Boolean {
        val currentAndroidSdk = metadataHandler.androidSdk()
        return currentAndroidSdk < Constants.MIN_ANDROID_SDK
    }
}
