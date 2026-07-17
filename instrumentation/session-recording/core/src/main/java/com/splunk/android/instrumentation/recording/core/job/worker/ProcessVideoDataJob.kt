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

package com.splunk.android.instrumentation.recording.core.job.worker

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.extensions.safeSubmit
import com.splunk.rum.common.utils.extensions.toJSONObject
import com.splunk.android.instrumentation.recording.core.data.DataChunk
import com.splunk.android.instrumentation.recording.core.data.containsNative
import com.splunk.android.instrumentation.recording.core.data.containsWireframe
import com.splunk.android.instrumentation.recording.core.dependencyInjection.DependencyInjectionTree
import com.splunk.android.instrumentation.recording.core.exporter.IDataChunkExporter
import com.splunk.android.instrumentation.recording.core.job.model.ProcessVideoDataJobData
import com.splunk.android.instrumentation.recording.core.job.model.RenderingData
import com.splunk.android.instrumentation.recording.core.video.EncoderQueue
import java.util.concurrent.Executors

@SuppressLint("NewApi")
internal class ProcessVideoDataJob : JobService() {

    companion object {

        private const val TAG = "ProcessVideoDataJob"
        private const val DATA_SERIALIZE_KEY = "DATA"
        const val JOB_NUMBER_LIMIT = 80L

        fun createJobInfoBuilder(
            context: Context,
            jobId: Int,
            jobData: ProcessVideoDataJobData
        ): JobInfo.Builder = JobInfo.Builder(
            jobId,
            ComponentName(context, ProcessVideoDataJob::class.java)
        )
            .setExtras(PersistableBundle().apply { putString(DATA_SERIALIZE_KEY, jobData.toJSONObject().toString()) })
            .setRequiresCharging(false)
    }

    private val taskQueueHandler by lazy { DependencyInjectionTree.encoderQueue }
    private val storage by lazy { DependencyInjectionTree.storage }
    private val dataChunkExporter: IDataChunkExporter by lazy { DependencyInjectionTree.dataChunkExporter }
    private var params: JobParameters? = null
    private val executors = Executors.newCachedThreadPool()
    private val listener = object : EncoderQueue.Listener {
        override fun onRenderingFinished(success: Boolean, data: RenderingData) {
            executors.safeSubmit {
                onVideoRendered(success, data)
            }
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Logger.d(TAG, "onStartJob()")
        this.params = params
        executors.safeSubmit { process(params) }
        return true
    }

    private fun process(params: JobParameters?) {
        params?.extras?.getString(DATA_SERIALIZE_KEY)?.let {
            val processVideoDataJobData = ProcessVideoDataJobData.fromJSONObject(it.toJSONObject())
            Logger.d(TAG, "process(): called with: processVideoDataJobData = $processVideoDataJobData")

            val dataChunkString = storage.readDataChunk(processVideoDataJobData.dataChunkId)
            val dataChunk = if (dataChunkString.isNullOrBlank()) {
                null
            } else {
                runCatching { DataChunk.fromJSONObject(dataChunkString.toJSONObject()) }.getOrNull()
            }

            dataChunk?.let {
                when {
                    it.renderingDataSources.containsNative() -> {
                        renderVideo(
                            RenderingData(
                                dataChunkId = processVideoDataJobData.dataChunkId
                            )
                        )
                    }

                    it.renderingDataSources.containsWireframe() -> {
                        val data = RenderingData(
                            dataChunkId = processVideoDataJobData.dataChunkId,
                        )
                        processDataChunk(data)
                    }

                    else -> Unit
                }
            }
        } ?: jobFinished(params, false)
    }

    private fun renderVideo(data: RenderingData) {
        Logger.d(TAG, "renderVideo(): called with: data = $data")
        taskQueueHandler.listeners += listener
        taskQueueHandler.scheduleRendering(data)
    }

    private fun onVideoRendered(success: Boolean, data: RenderingData) {
        params?.extras?.getString(DATA_SERIALIZE_KEY)?.let {
            val processVideoDataJobData = ProcessVideoDataJobData.fromJSONObject(it.toJSONObject())

            if (processVideoDataJobData.dataChunkId == data.dataChunkId) {
                taskQueueHandler.listeners -= listener

                Logger.d(TAG, "onVideoRendered() called with: success = $success, dataChunkId = ${data.dataChunkId}")

                if (success) {
                    processDataChunk(data)
                } else {
                    Logger.d(TAG, "onVideoRendered() deleting data chunk: success = $success, dataChunkId = ${data.dataChunkId}")

                    storage.deleteDataChunk(data.dataChunkId)
                }
            }
        }

        jobFinished(params, false)
    }

    private fun processDataChunk(data: RenderingData) {
        Logger.d(TAG, "processDataChunk() called with: data = $data")

        dataChunkExporter.export(data.dataChunkId)
    }
}
