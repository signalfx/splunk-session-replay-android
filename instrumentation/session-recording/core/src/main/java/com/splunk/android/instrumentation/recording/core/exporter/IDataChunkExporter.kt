package com.splunk.android.instrumentation.recording.core.exporter

interface IDataChunkExporter {
    fun export(dataChunkId: String)
}
