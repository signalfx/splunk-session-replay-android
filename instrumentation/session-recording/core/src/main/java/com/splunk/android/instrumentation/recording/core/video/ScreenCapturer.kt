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
import android.app.Activity
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.extensions.compress
import com.splunk.rum.common.utils.extensions.safeSchedule
import com.splunk.rum.common.utils.extensions.safeSubmit
import com.splunk.rum.common.utils.extensions.toJSONArray
import com.splunk.rum.common.utils.runOnUiThread
import com.splunk.rum.common.utils.thread.NamedThreadFactory
import com.splunk.android.instrumentation.recording.capturer.FrameCapturer
import com.splunk.android.instrumentation.recording.core.Constants.MAX_RECORD_LENGTH
import com.splunk.android.instrumentation.recording.core.Initializer
import com.splunk.android.instrumentation.recording.core.api.RenderingMode
import com.splunk.android.instrumentation.recording.core.configuration.IConfigurationHandler
import com.splunk.android.instrumentation.recording.core.configuration.Listener
import com.splunk.android.instrumentation.recording.core.configuration.toBitRate
import com.splunk.android.instrumentation.recording.core.data.ApplicationFrame
import com.splunk.android.instrumentation.recording.core.data.DataChunk
import com.splunk.android.instrumentation.recording.core.data.VideoSize
import com.splunk.android.instrumentation.recording.core.extensions.runWhenActivityIsMeasuredAndAttachedToWindow
import com.splunk.android.instrumentation.recording.core.extensions.toFrameCapturerMode
import com.splunk.android.instrumentation.recording.core.extensions.toRenderingDataOption
import com.splunk.android.instrumentation.recording.core.lifecycle.LifecycleCallback
import com.splunk.android.instrumentation.recording.core.storage.ISessionReplayStorage
import com.splunk.android.instrumentation.recording.interactions.Interactions
import com.splunk.android.instrumentation.recording.interactions.extension.sampled
import com.splunk.android.instrumentation.recording.interactions.extension.toJSONArray
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.screenshot.model.Screenshot
import com.splunk.android.instrumentation.recording.screenshot.stats.ScreenshotStats
import com.splunk.android.instrumentation.recording.wireframe.extension.create
import com.splunk.android.instrumentation.recording.wireframe.extension.toJSONObject
import com.splunk.android.instrumentation.recording.wireframe.extension.waitToFinish
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("NewApi")
internal class ScreenCapturer(
    private val configurationHandler: IConfigurationHandler,
    private val storage: ISessionReplayStorage,
    private val activeDataChunkHandler: ActiveDataChunkHandler,
    private val closedDataChunkHandler: ClosedDataChunkHandler
) : IScreenCapturer {
    private val capturerExecutor = Executors.newSingleThreadExecutor(NamedThreadFactory("capturerExecutor"))
    private val storageExecutor = Executors.newSingleThreadExecutor(NamedThreadFactory("storageExecutor"))
    private val timeoutExecutor = Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("timeoutExecutor"))
    private var timeout: ScheduledFuture<*>? = null

    private val recordingShouldRun = AtomicBoolean(false)
    private val activityProcessed = AtomicBoolean(false)

    private var weakActivity: WeakReference<Activity>? = null

    private var activeDataChunkInstance: DataChunk? = null

    private val currentFrames = ArrayList<Frame>()
    private val framesStorage = HashMap<String, List<Frame>>()
    private val frameIndex = AtomicInteger(0)

    private val frameCapturerListener: FrameCapturer.Listener = object : FrameCapturer.Listener {
        override fun onNewScreenshot(screenshot: Screenshot, stats: ScreenshotStats) {
            Logger.d(TAG, "onNewScreenshot()")
            capturerExecutor.safeSubmit {
                activeDataChunkInstance?.let { activeDataChunk ->
                    val activeDataChunkFrame = activeDataChunk.applicationFrame
                    if (activeDataChunkFrame != null && (activeDataChunkFrame.width != screenshot.bitmap.width || activeDataChunkFrame.height != screenshot.bitmap.height)) {
                        setupNewDataChunk()
                    } else {
                        createAndAddVideoFrameToDataChunk(screenshot, activeDataChunk)
                    }
                }
            }
        }
    }

    private val configurationHandlerListener = object : Listener {
        private var lastRenderingMode: RenderingMode? = null

        override fun onRenderingModeChanged(mode: RenderingMode) {
            Logger.d(TAG, "onRenderingModeChanged() mode = $mode")

            val wasNull = lastRenderingMode == null

            if (mode != lastRenderingMode) {
                lastRenderingMode = mode

                runOnUiThread { FrameCapturer.mode = mode.toFrameCapturerMode() }

                if (recordingShouldRun.get() && !wasNull) {
                    Logger.d(TAG, "onRenderingModeChanged() mode = $mode, setting up new data chunk")

                    setupNewDataChunk()
                }
            }
        }
    }

    override fun registerLifecycleCallback(): LifecycleCallback = object : LifecycleCallback() {

        override fun onSetup() {
            Logger.d(TAG, "onSetup() called")
            recordingShouldRun.set(false)
        }

        override fun onActivityStarted(activity: Activity) {
            Logger.d(TAG, "onActivityStarted() called with: activity = $activity")

            activityProcessed.set(false)
            tryToProcessNewActivity(activity)
        }

        override fun onApplicationClosedWithSettle() {
            Logger.d(TAG, "onApplicationSettle() called")
            stopRecording(synchronized = false)
        }

        override fun onApplicationCrash(cause: Throwable) {
            Logger.d(TAG, "onApplicationCrash() START called with: cause = $cause")
            stopRecording(synchronized = true).get()
            Logger.d(TAG, "onApplicationCrash() END called with: cause = $cause")
        }
    }

    override fun start() {
        Logger.d(TAG, "start()")
        recordingShouldRun.set(true)

        weakActivity?.get()?.let {
            if (activeDataChunkInstance == null) tryToProcessNewActivity(it)
        } ?: run { Logger.d(TAG, "start() called before activity is available") }

        configurationHandler.listeners += configurationHandlerListener
    }

    override fun stop() {
        Logger.d(TAG, "stop()")
        configurationHandler.listeners -= configurationHandlerListener

        activityProcessed.set(false)
        recordingShouldRun.set(false)

        stopRecording(synchronized = false)
    }

    override fun newDataChunk() {
        if (recordingShouldRun.get() && activeDataChunkInstance == null) setupNewDataChunk()
    }

    /**
     * Called on every new activity/irregular setup/start recording.
     */
    private fun tryToProcessNewActivity(activity: Activity) {
        Logger.d(TAG, "tryToProcessNewActivity() called with: activity = $activity")

        weakActivity = WeakReference(activity)

        if (!recordingShouldRun.get() || activityProcessed.get()) return

        activityProcessed.set(true)

        Logger.d(TAG, "processNewActivity() called with: activity = $activity")

        activity.runWhenActivityIsMeasuredAndAttachedToWindow {
            Logger.d(TAG, "processNewActivity() activity is attached to a window and measured")

            runOnUiThread { FrameCapturer.mode = configurationHandler.renderingMode.toFrameCapturerMode() }

            startRecording()

            closedDataChunkHandler.processClosedDataChunks(Initializer.setupTimestamp)
        }
    }

    private fun startRecording() {
        Logger.d(TAG, "startRecording()")
        FrameCapturer.listeners += frameCapturerListener
        setupNewDataChunk()
    }

    private fun setupNewDataChunk() {
        Logger.d(TAG, "setupNewDataChunk()")
        capturerExecutor.safeSubmit {

            // Schedule maximum length of the data chunk -> needed because of adaptive frameRate.
            timeout?.cancel(true)
            timeout = timeoutExecutor.safeSchedule(MAX_RECORD_LENGTH) {
                Logger.d(TAG, "setupNewDataChunk() timeout")
                setupNewDataChunk()
            }

            frameIndex.set(0)

            // Store data chunk which is in progress if it exists and setup new one, or setup new one directly.
            activeDataChunkInstance?.let {
                Logger.d(TAG, "setupNewDataChunk() storing current data chunk, dataChunkId = ${it.id}")
                storeDataChunk(it)
            } ?: run {
                activeDataChunkInstance = DataChunk.create(
                    startTimestamp = System.currentTimeMillis(),
                    bitrate = configurationHandler.recordingQuality.toBitRate(),
                    frameRate = configurationHandler.frameRate,
                    renderingDataSources = configurationHandler.renderingMode.toRenderingDataOption()
                )
            }

            // Adding first frame to new data chunk -> needed because of adaptive frameRate.
            FrameCapturer.frameHolder.getLastScreenshot()?.let { screenshot ->
                activeDataChunkInstance?.let {
                    Logger.d(TAG, "setupNewDataChunk() adding first frame to new data chunk, dataChunkId = ${it.id}")
                    createAndAddVideoFrameToDataChunk(screenshot, it)
                }
            }
        }
    }

    private fun stopRecording(synchronized: Boolean): Future<*> {
        Logger.d(TAG, "stopRecording()")
        FrameCapturer.listeners -= frameCapturerListener

        return capturerExecutor.safeSubmit {
            timeout?.cancel(true)
            val activeDataChunk = activeDataChunkInstance
            if (activeDataChunk != null) {
                val storedDataChunkFuture = storeDataChunk(activeDataChunk, synchronized)
                if (synchronized) storedDataChunkFuture.get()
                activeDataChunkInstance = null
            } else {
                Logger.d(TAG, "stopRecording() no active dataChunk!")
            }
        }
    }

    private fun storeDataChunk(dataChunk: DataChunk, synchronized: Boolean = false): Future<*> {
        Logger.d(TAG, "storeDataChunk() dataChunkId = ${dataChunk.id}")
        val closeTimestamp = System.currentTimeMillis()
        activeDataChunkInstance = DataChunk.create(
            startTimestamp = closeTimestamp,
            bitrate = configurationHandler.recordingQuality.toBitRate(),
            frameRate = configurationHandler.frameRate,
            renderingDataSources = configurationHandler.renderingMode.toRenderingDataOption(),
        )
        val frames = getFrames(ArrayList(currentFrames), dataChunk.id)
        currentFrames.clear()
        framesStorage.remove(dataChunk.id)

        return storageExecutor.safeSubmit {
            val interactions = Interactions.interactionsHolder.getInteractionsCopy().sampled(
                startTime = dataChunk.timeStart,
                endTime = closeTimestamp
            )

            dataChunk.interactions = interactions.toJSONArray(dataChunk.timeStart)
            dataChunk.userActivity = interactions.mapNotNull { if (it !is Interaction.Touch.Pointer) it.timestamp else null }
            dataChunk.close(closingTimestamp = closeTimestamp)

            val wireframeFrames = FrameCapturer.frameHolder.getWireframeFramesCopy()

            val wireframe = Wireframe.create(
                frames = wireframeFrames,
                startTime = dataChunk.timeStart,
                endTime = closeTimestamp
            )
            wireframe.waitToFinish()

            // We need to take video size from the wireframe if there is no screenshot.
            if (dataChunk.getVideoSize() == VideoSize.EMPTY) {
                wireframe.frames.firstOrNull()?.let {
                    val rect = it.scenes.first().rect
                    dataChunk.setDimensions(ApplicationFrame(rect.width(), rect.height()))
                }
            }

            storage.writeVideoConfig(
                dataChunk.id,
                frames.addLimitFrames(dataChunk.timeStart, closeTimestamp).toJSONArray { array, item -> array.put(item.toJSONObject()) }.toString()
            )
            storage.writeDataChunk(dataChunk.id, dataChunk.toJSONObject().toString())
            storage.writeWireframe(dataChunk.id, wireframe.toJSONObject().compress())

            Logger.d(TAG, "storeDataChunk() dataChunkId = ${dataChunk.id}, send for processing")
            if (!synchronized) activeDataChunkHandler.processDataChunk(dataChunk)
        }
    }

    private fun getFrames(framesToBeSaved: List<Frame>, key: String): List<Frame> {
        val storedFrames = framesStorage[key]
        return if (storedFrames != null) {
            with<MutableList<Frame>, MutableList<Frame>>(mutableListOf()) {
                addAll(storedFrames)
                addAll(framesToBeSaved.filter { frame -> storedFrames.any { storedFrame -> storedFrame != frame } })
                framesStorage[key] = this
                this
            }
        } else {
            framesStorage[key] = framesToBeSaved
            framesToBeSaved
        }
    }

    private fun createAndAddVideoFrameToDataChunk(screenshot: Screenshot, dataChunk: DataChunk) {
        val frameNumber = frameIndex.get()
        addVideoFrame(screenshot, frameNumber, dataChunk)
        frameIndex.incrementAndGet()
        storage.writeVideoFrame(dataChunk.id, frameNumber, screenshot.bitmap)
        dataChunk.setDimensions(ApplicationFrame(screenshot.bitmap.width, screenshot.bitmap.height))
    }

    private fun addVideoFrame(screenshot: Screenshot, frameIndex: Int, dataChunk: DataChunk) {
        Logger.d(TAG, "addVideoFrame() index = $frameIndex, dataChunkId = ${dataChunk.id}")

        // Normalize screenshot time, it cannot be sooner than dataChunk start time.
        // If it is the case than we took over the last available screenshot.
        val time = when {
            screenshot.time < dataChunk.timeStart -> dataChunk.timeStart
            else -> screenshot.time
        }

        return with(currentFrames) {
            if (isEmpty()) {
                add(Frame(frameIndex, time - dataChunk.timeStart, time))
            } else {
                val lastFrame = last()
                add(Frame(frameIndex, time - lastFrame.generalTime, time))
            }
        }
    }

    private companion object {
        const val TAG = "ScreenCapturer"
    }
}
