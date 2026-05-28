package com.splunk.android.instrumentation.recording.core.storage

import android.graphics.Bitmap
import java.io.File

internal interface ISessionReplayStorage {
    val freeSpace: Long
    val rootDirPath: String
    val isStorageFull: Boolean

    fun writeDataChunk(dataChunkId: String, dataChunkJson: String): Boolean
    fun readDataChunk(dataChunkId: String): String?
    fun deleteDataChunk(dataChunkId: String): Boolean

    fun getDataChunks(): List<StoredDataChunk>

    fun hasData(dataChunkId: String): Boolean

    fun findOldestDataChunkId(): String?

    fun writeWireframe(dataChunkId: String, wireframe: ByteArray): Boolean
    fun readWireframe(dataChunkId: String): ByteArray?
    fun isWireframeFileAvailable(dataChunkId: String): Boolean

    fun getVideoImageDir(dataChunkId: String): File
    fun writeVideoConfig(dataChunkId: String, config: String): Boolean
    fun readVideoConfig(dataChunkId: String): String?
    fun writeVideoFrame(dataChunkId: String, frameIndex: Int, frame: Bitmap): Boolean
    fun deleteAllVideoFrames(dataChunkId: String): Boolean
    fun createVideoFile(dataChunkId: String): File
    fun getVideoFile(dataChunkId: String): File
    fun isVideoFileAvailable(dataChunkId: String): Boolean
}
