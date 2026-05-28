package com.splunk.android.common.storage.extensions

import android.content.Context
import android.os.Build
import com.splunk.android.common.utils.runOnAndroidAtLeast
import java.io.File

val Context.noBackupFilesDirCompat: File
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.LOLLIPOP) { noBackupFilesDir } ?: filesDir
