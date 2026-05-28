package com.splunk.android.instrumentation.recording.wireframe.extension

import android.os.Build
import android.view.ViewGroup
import com.splunk.android.common.utils.runOnAndroidAtLeast

internal val ViewGroup.clipToPaddingCompat: Boolean
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.LOLLIPOP) { clipToPadding } ?: true
