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

package com.splunk.android.instrumentation.recording.core.configuration

import android.media.MediaCodecInfo
import com.splunk.rum.common.encoder.Codec
import com.splunk.rum.common.job.IJobManager
import com.splunk.rum.common.job.JobIdStorage
import com.splunk.rum.common.utils.extensions.forEachFast
import com.splunk.android.instrumentation.recording.core.Constants
import com.splunk.android.instrumentation.recording.core.api.RecordingQuality
import com.splunk.android.instrumentation.recording.core.api.RenderingMode
import com.splunk.android.instrumentation.recording.core.storage.ISessionReplayStorage

internal class ConfigurationHandler(
    private val storage: ISessionReplayStorage,
    private val jobIdStorage: JobIdStorage,
    private val jobManager: IJobManager
) : IConfigurationHandler {

    private val codecInfo by lazy { Codec.findAvcEncoder(MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline) }

    override val listeners: MutableCollection<Listener> = HashSet()

    override var renderingMode: RenderingMode = RenderingMode.NATIVE
        set(value) {
            field = value
            listeners.forEachFast { it.onRenderingModeChanged(value) }
        }

    override var frameRate: Int = Constants.DEFAULT_FRAMERATE
        set(value) {
            if (allowedFrameRate(value)) {
                field = value
            }
        }

    override var recordingQuality: RecordingQuality = RecordingQuality.LOW
        set(value) {
            field = value
        }

    override fun recordingState(): RecordingState {
        var isStorageFull = storage.isStorageFull

        while (isStorageFull) {
            val oldestRecordId = storage.findOldestDataChunkId() ?: break

            for ((_, jobId) in jobIdStorage.getAllWithPrefix(oldestRecordId))
                jobManager.cancel(jobId)

            storage.deleteDataChunk(oldestRecordId)
            isStorageFull = storage.isStorageFull
        }

        return when {
            isStorageFull ->
                RecordingState.NotAllowed(
                    cause = RecordingState.NotAllowed.Cause.NOT_ENOUGH_STORAGE_SPACE
                )
            codecInfo == null && renderingMode != RenderingMode.WIREFRAME_ONLY ->
                RecordingState.NotAllowed(
                    cause = RecordingState.NotAllowed.Cause.MISSING_CODEC
                )
            else ->
                RecordingState.Allowed
        }
    }

    private fun allowedFrameRate(frameRate: Int?): Boolean {
        return frameRate == null ||
            (frameRate >= Constants.MIN_FRAMERATE && frameRate <= Constants.MAX_FRAMERATE)
    }
}
