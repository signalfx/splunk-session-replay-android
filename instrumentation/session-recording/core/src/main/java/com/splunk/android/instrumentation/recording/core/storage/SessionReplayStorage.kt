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

import android.content.Context
import android.graphics.Bitmap
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.storage.Storage
import com.splunk.android.common.storage.cache.FilePermanentCache
import com.splunk.android.common.storage.cache.FileSimplePermanentCache
import com.splunk.android.common.storage.extensions.createNewFileOnPath
import com.splunk.android.common.storage.extensions.noBackupFilesDirCompat
import com.splunk.android.common.storage.filemanager.EncryptedFileManager
import com.splunk.android.common.storage.filemanager.FileManagerFactory
import com.splunk.android.common.storage.preferences.Preferences
import com.splunk.android.common.utils.runOnBackgroundThread
import com.splunk.android.instrumentation.recording.core.storage.extension.MB
import com.splunk.android.instrumentation.recording.core.storage.extension.oldestChildDir
import com.splunk.android.instrumentation.recording.core.storage.extension.statFsFreeSpace
import com.splunk.android.instrumentation.recording.core.storage.policy.StoragePolicy
import java.io.File
import java.io.IOException

/**
 * Ideas:
 * - Remove the unnecessary folders with single files in them.
 * - Video image storage needs rework!
 * - If we rework the network layer we can make paths private, because they are now used only for the FILE multipart parts.
 *
 * SDK storage structure:
 *  session_recording/
 *    └─<STORAGE_VERSION>/
 *           └─dataChunks/
 *              └─<data_chunk_id>/
 *                ├──metrics.dat
 *                ├──dataChunk.dat
 *                ├──wireframe.dat
 *                └──video/
 *                  ├──<frame_number>.jpg
 *                  ├──video.mp4
 *                  └──metadata.dat
 */
internal class SessionReplayStorage(
    context: Context,
    isEncrypted: Boolean
) : ISessionReplayStorage {

    private val preferencesFileManager =
        FileManagerFactory.createConditionedEncryptedFileManager("SessionRecording-Preferences$VERSION", isEncrypted)
    private val preferences: Preferences

    private val encryptedStorage =
        Storage(
            FilePermanentCache(
                FileManagerFactory.createConditionedEncryptedFileManager(
                    "SessionRecording-Storage$VERSION",
                    isEncrypted
                )
            )
        )

    private val rootDir = File(context.noBackupFilesDirCompat, "session_recording")
    private val sessionRecordingVersionDir = File(rootDir, "${VERSION}${if (preferencesFileManager is EncryptedFileManager) "e" else ""}")
    private val preferencesFile = File(sessionRecordingVersionDir, "preferences/preferences.dat")
    private val dataChunksDir = File(sessionRecordingVersionDir, "dataChunks")

    init {
        preferences = Preferences(FileSimplePermanentCache(preferencesFile, preferencesFileManager))

        sessionRecordingVersionDir.mkdirs()
        dataChunksDir.mkdirs()

        cleanUpStorage()
    }

    override val freeSpace: Long
        get() {
            val freeSpace = rootDir.statFsFreeSpace
            Logger.v(TAG, "freeSpace: $freeSpace")
            return freeSpace
        }

    override val rootDirPath: String
        get() {
            val path = rootDir.path
            Logger.v(TAG, "consistentDirPath: $path")
            return path
        }

    override val isStorageFull: Boolean
        get() {
            val isFull = !StoragePolicy(dataChunksDir, 1000.MB, 0.2f, 50.MB).check(freeSpace)
            Logger.v(TAG, "isStorageFull: $isFull")
            return isFull
        }

    //region Data Chunk

    override fun writeDataChunk(dataChunkId: String, dataChunkJson: String): Boolean {
        val dataChunkFile: File = dataChunkFile(dataChunkId)
        val success = encryptedStorage.writeText(dataChunkFile, dataChunkJson)
        Logger.d(TAG, "writeDataChunk(): dataChunkId = $dataChunkId, success = $success")

        return success
    }

    override fun readDataChunk(dataChunkId: String): String? {
        val dataChunkFile: File = dataChunkFile(dataChunkId)
        val dataChunk = encryptedStorage.readText(dataChunkFile)
        Logger.d(TAG, "readDataChunk(): dataChunkId = $dataChunkId, isNullOrBlank = ${dataChunk.isNullOrBlank()}")

        return dataChunk
    }

    override fun deleteDataChunk(dataChunkId: String): Boolean {
        val dataChunkDir: File = dataChunkDir(dataChunkId)
        val success = dataChunkDir.deleteRecursively()
        Logger.d(TAG, "deleteDataChunk(): dataChunkId = $dataChunkId, success = $success")

        return success
    }

    //endregion

    //region Session

    override fun hasData(dataChunkId: String): Boolean {
        return dataChunkDir(dataChunkId).listFiles()?.isNotEmpty() ?: false
    }

    override fun getDataChunks(): List<StoredDataChunk> {
        val ids = dataChunksDir.listFiles()?.map { StoredDataChunk(fileName = it.name, modified = it.lastModified()) } ?: emptyList()
        return ids
    }

    override fun findOldestDataChunkId(): String? {
        return dataChunksDir.oldestChildDir()?.name
    }

    //region Wireframe

    override fun writeWireframe(dataChunkId: String, wireframe: ByteArray): Boolean {
        val wireframeFile: File = wireframeFile(dataChunkId)
        val success = encryptedStorage.writeBytes(wireframeFile, wireframe)
        Logger.d(TAG, "writeWireframe(): dataChunkId = $dataChunkId, success = $success")

        return success
    }

    override fun isWireframeFileAvailable(dataChunkId: String): Boolean {
        val wireframeFile: File = wireframeFile(dataChunkId)
        val isAvailable = wireframeFile.exists()
        Logger.d(TAG, "isWireframeFileAvailable(): isAvailable = $isAvailable")

        return isAvailable
    }

    override fun readWireframe(dataChunkId: String): ByteArray? {
        val wireframeFile = wireframeFile(dataChunkId)
        val bytes = encryptedStorage.readBytes(wireframeFile)
        Logger.d(TAG, "getWireframeBytes(): dataChunkId = $dataChunkId")

        return bytes
    }

    //endregion

    //region Session video images

    override fun getVideoImageDir(dataChunkId: String): File {
        return videoImageDir(dataChunkId)
    }

    //endregion

    //region Video configuration

    override fun writeVideoConfig(dataChunkId: String, config: String): Boolean {
        val videoConfigurationFile: File = videoConfigFile(dataChunkId)
        val success = encryptedStorage.writeText(videoConfigurationFile, config)
        Logger.d(TAG, "writeVideoConfig(): dataChunkId = $dataChunkId, success = $success")

        return success
    }

    override fun readVideoConfig(dataChunkId: String): String? {
        val videoConfigFile = videoConfigFile(dataChunkId)
        val videoConfig = encryptedStorage.readText(videoConfigFile)
        Logger.d(TAG, "readVideoConfig(): dataChunkId = $dataChunkId, isNullOrBlank = ${videoConfig.isNullOrBlank()}")

        return videoConfig
    }

    //endregion

    //region Video frames

    /*TODO encrypt when there is time to come up with solution for decrypting - code below
     * videoImageFile.createNewFileOnPath()
     * val stream = ByteArrayOutputStream()
     * frame.compress(format, quality, stream)
     * val encryptedBytes = stream.toByteArray().encrypt(storageSecretKey)
     * FileOutputStream(videoImageFile).write(encryptedBytes)
     */
    override fun writeVideoFrame(dataChunkId: String, frameIndex: Int, frame: Bitmap): Boolean {
        val videoImageFile = videoImageFile(dataChunkId, frameIndex)

        val success = try {
            val stream = videoImageFile.createNewFileOnPath().outputStream().buffered()
            frame.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()
            true
        } catch (e: IOException) {
            false
        } catch (e: IllegalStateException) { // recycled bitmap
            false
        }

        Logger.v(
            TAG,
            "writeVideoFrame(): dataChunkId = $dataChunkId," + " frameIndex = $frameIndex, success = $success, width: ${frame.width}, height: ${frame.height}"
        )

        return success
    }

    override fun deleteAllVideoFrames(dataChunkId: String): Boolean {
        val videoImageDir = videoImageDir(dataChunkId)
        val videoImageFiles = videoImageDir.listFiles()
        var success = true

        videoImageFiles?.forEach {
            if (it.name.endsWith(".jpg")) {
                try {
                    it.delete()
                } catch (e: Exception) {
                    Logger.d(TAG, "deleteAllVideoFrames(): dataChunkId = $dataChunkId - failed with Exception ${e.message}")
                    success = false
                }
            }
        }

        Logger.d(TAG, "deleteAllVideoFrames(): dataChunkId = $dataChunkId, success = $success")

        return success
    }

    //endregion

    //region Video file

    override fun createVideoFile(dataChunkId: String): File {
        return getVideoFile(dataChunkId).createNewFileOnPath()
    }

    override fun getVideoFile(dataChunkId: String): File {
        return videoFile(dataChunkId)
    }

    override fun isVideoFileAvailable(dataChunkId: String): Boolean {
        val videoFile = videoFile(dataChunkId)
        val isAvailable = videoFile.exists()
        Logger.d(TAG, "isVideoFileAvailable(): dataChunkId = $dataChunkId, isAvailable = $isAvailable")

        return isAvailable
    }

    //endregion

    fun cleanUpStorage(): Boolean {
        val files = ArrayList<File>()
        files += rootDir.listFiles()

        val filesToDelete = ArrayList<File>()

        for (file in files)
            if (file.exists() && file != sessionRecordingVersionDir)
                filesToDelete += file

        return if (filesToDelete.isNotEmpty()) {
            runOnBackgroundThread {
                for (file in filesToDelete) {
                    val success = file.deleteRecursively()
                    Logger.w(TAG, "deleteOldDirectories(): file = $file, success = $success")
                }
            }

            false
        } else
            true
    }

    fun commitPreferences() {
        preferences.commit()
    }

    /**
     * Data Chunks folder
     */
    private fun dataChunkDir(dataChunkId: String) = File(dataChunksDir, dataChunkId)
    private fun dataChunkFile(dataChunkId: String) = File(dataChunkDir(dataChunkId), "dataChunk.txt")

    /**
     * Video images folder. Specific data chunk video folders.
     */
    private fun videoImageDir(dataChunkId: String) = File(dataChunkDir(dataChunkId), "video")

    /**
     * Video images and video files.
     */
    private fun videoImageFile(dataChunkId: String, frameNumber: Int) = File(videoImageDir(dataChunkId), "$frameNumber.jpg")
    private fun videoFile(dataChunkId: String) = File(videoImageDir(dataChunkId), "video.mp4")
    private fun videoConfigFile(dataChunkId: String) = File(videoImageDir(dataChunkId), "metadata.txt")

    /**
     * Wireframe file stored inside a data chunk video dir.
     */
    private fun wireframeFile(dataChunkId: String) = File(dataChunkDir(dataChunkId), "wireframe.txt")

    companion object {
        private const val TAG = "SessionRecordingStorage"

        /**
         * If storage model changes this version needs to be changed. This will ensure data consistency.
         * The storage will wipe all the legacy data (older version than this one).
         */
        private const val VERSION = 6
    }
}
