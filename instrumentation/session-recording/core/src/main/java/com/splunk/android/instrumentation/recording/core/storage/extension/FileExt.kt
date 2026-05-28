package com.splunk.android.instrumentation.recording.core.storage.extension

import android.os.StatFs
import java.io.File

internal fun File.oldestChildDir(): File? {
    val childDirs = listFiles()
    if (childDirs.isNullOrEmpty()) return null

    var oldestDir: File? = null
    var oldestModificationTime = Long.MAX_VALUE

    for (file in childDirs) {
        if (file.isDirectory) {
            val lastModified = file.lastModified()
            if (lastModified < oldestModificationTime) {
                oldestModificationTime = lastModified
                oldestDir = file
            }
        }
    }

    return oldestDir
}

internal val File.statFsFreeSpace: Long
    get() {
        if (!exists())
            mkdirs()

        return StatFs(path).run { availableBlocksCompat * blockSizeCompat }
    }
