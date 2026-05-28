package com.splunk.android.instrumentation.recording.core.metadata

import android.os.Build

internal object MetadataHandler : IMetadataHandler {
    override fun androidSdk(): Int = Build.VERSION.SDK_INT
}
