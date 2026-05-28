package com.splunk.android.instrumentation.recording.core.exporter

import com.splunk.android.common.http.extension.write
import com.splunk.android.common.http.model.part.ByteArrayContent
import com.splunk.android.common.http.model.part.Content
import com.splunk.android.common.http.model.part.FileContent
import com.splunk.android.common.http.model.part.StringContent
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.extensions.forEachFast
import com.splunk.android.common.utils.extensions.toISO8601String
import com.splunk.android.common.utils.extensions.toJSONObject
import com.splunk.android.instrumentation.recording.core.Initializer
import com.splunk.android.instrumentation.recording.core.api.Metadata
import com.splunk.android.instrumentation.recording.core.data.DataChunk
import com.splunk.android.instrumentation.recording.core.data.containsNative
import com.splunk.android.instrumentation.recording.core.data.containsWireframe
import com.splunk.android.instrumentation.recording.core.storage.ISessionReplayStorage
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.UUID

internal class DataChunkExporter(
    private val storage: ISessionReplayStorage,
) : IDataChunkExporter {
    override fun export(dataChunkId: String) {
        val dataString = storage.readDataChunk(dataChunkId)

        val dataChunk = if (dataString.isNullOrBlank()) {
            storage.deleteDataChunk(dataChunkId)
            return
        } else {
            runCatching { DataChunk.fromJSONObject(dataString.toJSONObject()) }.getOrElse {
                storage.deleteDataChunk(dataChunkId)
                return
            }
        }

        val parts = mutableListOf<Content>(
            createMetadataPart(dataChunk),
            createEventPart(dataChunk)
        )

        if (dataChunk.renderingDataSources.containsNative()) {
            parts.add(createVideoPart(dataChunkId))
        }
        if (dataChunk.renderingDataSources.containsWireframe()) {
            createWireframePart(dataChunkId)?.let { parts.add(it) }
        }

        val boundary = UUID.randomUUID().toString()
        val stream = ByteArrayOutputStream()

        try {
            stream.write(parts, boundary)
        } catch (e: FileNotFoundException) {
            Logger.d(TAG, "export() failed to write parts to stream", e)
            return
        }

        Initializer.instance.dataListeners.forEachFast {
            it.onData(
                data = stream.toByteArray(),
                metadata = Metadata(
                    startUnixMs = dataChunk.timeStart,
                    endUnixMs = dataChunk.timeEnd,
                    userActivity = dataChunk.userActivity
                )
            )
        }
        stream.close()
        storage.deleteDataChunk(dataChunkId)
    }

    private fun createVideoPart(
        dataChunkId: String
    ): FileContent {
        return FileContent(
            dispositionName = "video",
            dispositionFileName = "video.mp4",
            type = "video/mp4",
            file = storage.getVideoFile(dataChunkId)
        )
    }

    private fun createWireframePart(
        dataChunkId: String
    ): ByteArrayContent? {
        val wireframe = storage.readWireframe(dataChunkId) ?: return null
        return ByteArrayContent(
            dispositionName = "wireframe",
            dispositionFileName = "wireframe.dat",
            type = "application/json",
            encoding = "gzip",
            bytes = wireframe
        )
    }

    private fun createEventPart(dataChunk: DataChunk): StringContent {
        val eventsDataJson = JSONObject()
            .put("interactions", dataChunk.interactions)
            .put("schemeVersion", dataChunk.scheme)

        return StringContent(
            dispositionName = "events",
            dispositionFileName = null,
            type = "application/json",
            string = eventsDataJson.toString()
        )
    }

    private fun createMetadataPart(dataChunk: DataChunk): StringContent {
        val metadataJson = JSONObject()
            .put("timeStart", dataChunk.timeStart.toISO8601String())
            .put("timeClose", dataChunk.timeEnd.toISO8601String())
            .put("applicationFrame", dataChunk.applicationFrame?.toJSONObject())
            .put("schemeVersion", dataChunk.scheme)

        return StringContent(
            dispositionName = "metadata",
            dispositionFileName = null,
            type = "application/json",
            string = metadataJson.toString()
        )
    }

    private companion object {
        const val TAG = "DataChunkExporter"
    }
}
