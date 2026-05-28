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
