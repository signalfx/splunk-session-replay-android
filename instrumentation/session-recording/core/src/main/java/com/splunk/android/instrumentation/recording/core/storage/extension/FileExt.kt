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
