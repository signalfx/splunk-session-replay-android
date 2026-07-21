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
import com.splunk.android.instrumentation.recording.core.data.DataChunk
import com.splunk.android.instrumentation.recording.core.data.containsNative
import com.splunk.android.instrumentation.recording.core.exporter.IDataChunkExporter
import com.splunk.android.instrumentation.recording.core.job.model.RenderingData
import com.splunk.android.instrumentation.recording.core.storage.ISessionReplayStorage
import java.util.concurrent.Executors

internal class ActiveDataChunkHandler(
    private val storage: ISessionReplayStorage,
    private val encoderQueue: EncoderQueue,
    private val dataChunkExporter: IDataChunkExporter
) {
    private val executor = Executors.newCachedThreadPool()

    init {
        encoderQueue.listeners += object : EncoderQueue.Listener {
            override fun onRenderingFinished(success: Boolean, data: RenderingData) {
                executor.safeSubmit {
                    onVideoRendered(success, data)
                }
            }
        }
    }

    /**
     * [ActiveDataChunkHandler] assumes that everything is ready for the data chunk to be processed.
     */
    fun processDataChunk(dataChunk: DataChunk) {
        Logger.d(TAG, "processDataChunk(): called with: dataChunk = $dataChunk")

        val data = RenderingData(dataChunk.id)

        when {
            dataChunk.renderingDataSources.containsNative() -> renderVideo(data)
            else -> executor.safeSubmit { processDataChunk(data) }
        }
    }

    private fun renderVideo(data: RenderingData) {
        Logger.d(TAG, "renderVideo(): called with: data = $data")
        encoderQueue.scheduleRendering(data)
    }

    private fun onVideoRendered(success: Boolean, data: RenderingData) {
        Logger.d(TAG, "onVideoRendered() called with: success = $success, dataChunkId = ${data.dataChunkId}")

        if (success) {
            processDataChunk(data)
        } else {
            Logger.d(TAG, "onVideoRendered() deleting data chunk: success = $success, dataChunkId = ${data.dataChunkId}")
            storage.deleteDataChunk(data.dataChunkId)
        }
    }

    private fun processDataChunk(data: RenderingData) {
        Logger.d(TAG, "processDataChunk() called with: data = $data")

        dataChunkExporter.export(data.dataChunkId)
    }

    private companion object {
        const val TAG = "ActiveDataChunkHandler"
    }
}
