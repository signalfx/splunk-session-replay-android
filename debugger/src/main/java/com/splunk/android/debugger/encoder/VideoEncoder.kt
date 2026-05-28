package com.splunk.android.debugger.encoder

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import androidx.annotation.WorkerThread
import java.io.File
import java.util.LinkedList

internal class VideoEncoder {

    private val sync = Object()

    private val frameQueue = LinkedList<Frame>()

    private var outputFile: File? = null
    private var mediaCodec: MediaCodec? = null
    private var mediaMuxer: MediaMuxer? = null
    private var surface: Surface? = null

    private var width = 0
    private var height = 0

    private var isEncodingStopped = false
    private var isAborted = false

    var listener: Listener? = null

    fun start(outputFile: File, width: Int, height: Int, frameRate: Int, bitrate: Int = width * height * 8, mimeType: String = "video/avc", keyFramesPerSecond: Int = 1) {
        if (isStarted())
            throw IllegalStateException("Encoding is already started")

        this.width = width
        this.height = height
        this.outputFile = outputFile

        val codecInfo = findEncoderCodec { it.supportedTypes.any { it.equals(mimeType, ignoreCase = true) } } ?: throw IllegalArgumentException("Unable to find an appropriate codec for '$mimeType'")

        val format = MediaFormat.createVideoFormat(mimeType, width, height)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, keyFramesPerSecond)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, CodecCapabilities.COLOR_FormatSurface)

        val mediaCodec = MediaCodec.createByCodecName(codecInfo.name)
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        surface = mediaCodec.createInputSurface()

        mediaCodec.start()

        mediaMuxer = MediaMuxer(outputFile.canonicalPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        this.mediaCodec = mediaCodec

        Encoder().start()
    }

    fun isStarted(): Boolean {
        return mediaCodec != null && mediaMuxer != null
    }

    fun stop() {
        if (!isStarted())
            throw IllegalStateException("Encoding is not started")

        isEncodingStopped = true
        synchronized(sync) { sync.notifyAll() }
    }

    fun abort() {
        if (!isStarted())
            throw IllegalStateException("Encoding is not started")

        isEncodingStopped = true
        isAborted = true

        synchronized(frameQueue) { frameQueue.clear() }
        synchronized(sync) { sync.notifyAll() }
    }

    fun addFrame(bitmap: Bitmap, timestamp: Long) {
        if (!isStarted())
            throw IllegalStateException("Encoding is not started")

        if (bitmap.width != width || bitmap.height != height)
            throw IllegalArgumentException("Bitmap size must be $width x $height")

        synchronized(frameQueue) { frameQueue += Frame(bitmap, timestamp) }
        synchronized(sync) { sync.notifyAll() }
    }

    private fun release() {
        mediaCodec?.stop()
        mediaCodec?.release()
        mediaCodec = null

        mediaMuxer?.stop()
        mediaMuxer?.release()
        mediaMuxer = null

        surface?.release()
        surface = null
    }

    private fun findEncoderCodec(predicate: (MediaCodecInfo) -> Boolean): MediaCodecInfo? {
        val infoList = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos

        for (codecInfo in infoList)
            if (codecInfo.isEncoder && predicate(codecInfo))
                return codecInfo

        return null
    }

    interface Listener {

        @WorkerThread
        fun onStarted() {
        }

        @WorkerThread
        fun onCompleted(file: File) {
        }

        @WorkerThread
        fun onAborted(file: File) {
        }
    }

    private inner class Encoder : Thread("VideoEncoder") {

        override fun run() {
            val mediaCodec = mediaCodec ?: return
            val mediaMuxer = mediaMuxer ?: return
            val surface = surface ?: return
            val outputFile = outputFile ?: return

            val bufferInfo = MediaCodec.BufferInfo()
            var firstFrameTimestamp = -1L
            var trackIndex = 0

            listener?.onStarted()

            while (true) {
                if (isEncodingStopped && frameQueue.size == 0)
                    break

                val frame = synchronized(frameQueue) { frameQueue.peek() }

                if (frame == null) {
                    synchronized(sync) { runCatching { sync.wait() } }
                    continue
                }

                if (firstFrameTimestamp == -1L)
                    firstFrameTimestamp = frame.timestamp

                val canvas = surface.lockCanvas(null)
                canvas.drawBitmap(frame.bitmap, 0f, 0f, null)
                surface.unlockCanvasAndPost(canvas)

                when (val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, BUFFER_TIMEOUT)) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val newFormat = mediaCodec.outputFormat
                        trackIndex = mediaMuxer.addTrack(newFormat)
                        mediaMuxer.start()
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        sleep(TRY_AGAIN_DELAY)
                    }
                    else -> {
                        bufferInfo.presentationTimeUs = (frame.timestamp - firstFrameTimestamp) * 1000L

                        val encodedData = mediaCodec.getOutputBuffer(outputBufferIndex) ?: continue
                        mediaMuxer.writeSampleData(trackIndex, encodedData, bufferInfo)

                        mediaCodec.releaseOutputBuffer(outputBufferIndex, false)

                        synchronized(frameQueue) { frameQueue.removeFirst() }
                    }
                }
            }

            release()

            if (isAborted) {
                outputFile.delete()
                listener?.onAborted(outputFile)
                return
            }

            listener?.onCompleted(outputFile)
        }
    }

    private data class Frame(
        val bitmap: Bitmap,
        val timestamp: Long
    )

    companion object {
        private const val BUFFER_TIMEOUT = 500000L
        private const val TRY_AGAIN_DELAY = 10L
    }
}
