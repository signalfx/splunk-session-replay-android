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

import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.extensions.safeSubmit
import com.splunk.rum.common.utils.extensions.toJSONObject
import com.splunk.android.instrumentation.recording.core.data.DataChunk
import com.splunk.android.instrumentation.recording.core.data.containsNative
import com.splunk.android.instrumentation.recording.core.data.containsWireframe
import com.splunk.android.instrumentation.recording.core.exporter.IDataChunkExporter
import com.splunk.android.instrumentation.recording.core.job.model.RenderingData
import com.splunk.android.instrumentation.recording.core.storage.ISessionReplayStorage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal class ClosedDataChunkHandler(
    private val storage: ISessionReplayStorage,
    private val encoderQueue: EncoderQueue,
    private val dataChunkExporter: IDataChunkExporter
) {

    private val executor = Executors.newCachedThreadPool()
    private var loadedClosedDataChunks = AtomicBoolean(false)

    fun processClosedDataChunks(currentSetupTimestamp: Long?) {
        if (currentSetupTimestamp == null) return
        executor.safeSubmit {
            Logger.d(TAG, "processClosedDataChunks(): called with: currentSetupTimestamp = $currentSetupTimestamp")

            if (!loadedClosedDataChunks.getAndSet(true)) {
                loadClosedDataChunks(currentSetupTimestamp)
            } else {
                Logger.d(TAG, "processClosedDataChunks(): already called! Not doing anything.")
            }
        }
    }

    private fun renderVideo(data: RenderingData) {
        Logger.d(TAG, "renderVideo(): called with: data = $data")
        encoderQueue.scheduleRendering(data)
    }

    private fun loadClosedDataChunks(currentSetupTimestamp: Long) {
        Logger.d(TAG, "loadClosedDataChunks() called")

        storage.getDataChunks().filter { it.modified < currentSetupTimestamp }.forEach { closedDataChunk ->
            processClosedDataChunk(closedDataChunk.fileName)
        }
    }

    private fun processClosedDataChunk(dataChunkId: String) {
        Logger.d(TAG, "processClosedDataChunk() called with: dataChunkId = $dataChunkId")

        if (storage.hasData(dataChunkId)) {
            Logger.d(TAG, "processClosedDataChunk() processing dataChunkId with id = $dataChunkId")
            val data = RenderingData(dataChunkId)
            when (videoPartStatus(dataChunkId)) {
                VideoPartStatus.MissingNativeVideo -> renderVideo(data)
                VideoPartStatus.MissingWireframe -> storage.deleteDataChunk(dataChunkId)
                VideoPartStatus.Ready -> processDataChunk(data)
            }
        } else {
            storage.deleteDataChunk(dataChunkId)
        }
    }

    /**
     * [BaseData.RenderingData] is ready.
     */
    private fun processDataChunk(
        data: RenderingData,
    ) {
        Logger.d(TAG, "processDataChunk() called with: data = $data")

        dataChunkExporter.export(data.dataChunkId)
    }

    private fun videoPartStatus(dataChunkId: String): VideoPartStatus {
        val dataChunkString = storage.readDataChunk(dataChunkId)
        val dataChunk = if (dataChunkString.isNullOrBlank()) {
            return VideoPartStatus.MissingWireframe
        } else {
            runCatching { DataChunk.fromJSONObject(dataChunkString.toJSONObject()) }.getOrElse {
                return VideoPartStatus.MissingWireframe
            }
        }

        if (dataChunk.renderingDataSources.containsWireframe() && !storage.isWireframeFileAvailable(dataChunkId)) {
            return VideoPartStatus.MissingWireframe
        }

        if (dataChunk.renderingDataSources.containsNative() && !storage.isVideoFileAvailable(dataChunkId)) {
            return VideoPartStatus.MissingNativeVideo
        }

        return VideoPartStatus.Ready
    }

    internal sealed interface VideoPartStatus {
        object Ready : VideoPartStatus
        object MissingNativeVideo : VideoPartStatus
        object MissingWireframe : VideoPartStatus
    }

    private companion object {
        const val TAG = "ClosedDataChunkHandler"
    }
}
