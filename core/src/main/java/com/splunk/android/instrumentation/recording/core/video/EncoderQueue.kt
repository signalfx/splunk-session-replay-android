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

import android.annotation.SuppressLint
import com.splunk.rum.common.encoder.Encoder
import com.splunk.rum.common.encoder.model.VideoFrame
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.extensions.map
import com.splunk.rum.common.utils.extensions.safeSubmit
import com.splunk.rum.common.utils.extensions.toJSONObject
import com.splunk.android.instrumentation.recording.core.data.DataChunk
import com.splunk.android.instrumentation.recording.core.job.model.RenderingData
import com.splunk.android.instrumentation.recording.core.storage.ISessionReplayStorage
import org.json.JSONArray
import java.io.File
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

@SuppressLint("DefaultLocale")
internal class EncoderQueue(
    private val storage: ISessionReplayStorage,
) {
    val listeners: MutableCollection<Listener> = CopyOnWriteArraySet()

    private val executors = Executors.newCachedThreadPool()
    private val encodingQueue: Queue<RenderingData> = LinkedList()

    fun scheduleRendering(data: RenderingData) {
        Logger.d(TAG, "scheduleRendering() called with: data = $data, encodingQueueCount = ${encodingQueue.size}")

        encodingQueue.add(data)
        if (encodingQueue.size == 1) {
            startNextTask()
        }
    }

    private fun startNextTask() {
        val data = encodingQueue.peek() ?: return

        executors.safeSubmit {
            var outputFile: File? = null
            runCatching {
                Logger.d(TAG, "encodeNext collecting data for data=$data")

                val dataChunk = data.loadDataChunkFromFile() ?: throw IllegalStateException("Missing dataChunk")
                val size = dataChunk.getVideoSize()
                val videoFrames = data.toVideoFrames()

                Logger.d(TAG, "encodeNext collecting data for data=$data, size=$size, bitrate=${dataChunk.bitrate}")

                outputFile = storage.createVideoFile(data.dataChunkId)

                Encoder().start(
                    width = size.width,
                    height = size.height,
                    frames = videoFrames,
                    outputFile = requireNotNull(outputFile),
                    bitrate = dataChunk.bitrate.toInt(),
                    frameRate = dataChunk.frameRate,
                ).getOrThrow()
            }.onSuccess {
                Logger.d(TAG, "encodeNext for data=$data, finished")
                onTaskSuccess(data)
            }.onFailure {
                Logger.d(TAG, "encodeNext for data=$data, failed with $it")
                outputFile?.delete()
                onTaskFailure(data)
            }
        }
    }

    private fun notifyListeners(data: RenderingData, isRendered: Boolean) {
        Logger.d(TAG, "notifyListeners() called with: data = $data, isRendered = $isRendered")

        listeners.forEach { it.onRenderingFinished(isRendered, data) }
    }

    private fun removeTaskAndCheckForNext() {
        encodingQueue.poll()
        startNextTask()
    }

    private fun onTaskSuccess(data: RenderingData) {
        storage.deleteAllVideoFrames(data.dataChunkId)
        notifyListeners(data, true)
        removeTaskAndCheckForNext()
    }

    private fun onTaskFailure(data: RenderingData) {
        notifyListeners(data, false)
        removeTaskAndCheckForNext()
    }

    private fun RenderingData.toVideoFrames(): List<VideoFrame> {
        val frames = storage.readVideoConfig(dataChunkId)?.let { decodeVideoConfigRawToFrames(it) }
        if (frames.isNullOrEmpty()) throw IllegalStateException("Missing video config")

        val images = storage.getVideoImageDir(dataChunkId).filterPresentInConfiguration(frames)
        if (images.isNullOrEmpty()) throw IllegalStateException("Missing video images")

        Logger.d(TAG, "toVideoFrames images: ${images.map { it.path }}")

        return frames.map { frame ->
            Logger.d(TAG, "toVideoFrames frame: ${frame.fileName}")
            VideoFrame(
                filePath = images.first { it.endsWith(frame.fileName) }.path,
                duration = frame.duration
            )
        }
    }

    /**
     * Deserialize [videoConfigRaw] from JSON to list of [VideoFrame]
     */
    private fun decodeVideoConfigRawToFrames(
        videoConfigRaw: String
    ): List<Frame> {
        return JSONArray(videoConfigRaw).map { array, index -> Frame.fromJSONObject(array.getJSONObject(index)) }
    }

    /**
     * @return Files in this folder which are actually present in given [videoSetup]
     */
    private fun File.filterPresentInConfiguration(videoSetup: List<Frame>): Array<out File>? {
        return listFiles { pathname ->
            val name = pathname.name
            videoSetup.any { name.endsWith(it.fileName) }
        }
    }

    private fun RenderingData.loadDataChunkFromFile(): DataChunk? {
        val dataChunkString = storage.readDataChunk(dataChunkId)
        return if (dataChunkString.isNullOrBlank()) {
            null
        } else {
            runCatching { DataChunk.fromJSONObject(dataChunkString.toJSONObject()) }.getOrNull()
        }
    }

    interface Listener {
        fun onRenderingFinished(success: Boolean, data: RenderingData)
    }

    private companion object {
        const val TAG = "EncoderQueue"
    }
}
