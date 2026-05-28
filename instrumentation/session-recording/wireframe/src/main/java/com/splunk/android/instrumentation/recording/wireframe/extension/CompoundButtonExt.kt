package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.CompoundButton
import com.splunk.android.common.utils.extensions.get
import com.splunk.android.common.utils.runOnAndroidAtLeast

internal val CompoundButton.buttonDrawableCompat: Drawable?
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.M) { buttonDrawable } ?: get("mButtonDrawable")
