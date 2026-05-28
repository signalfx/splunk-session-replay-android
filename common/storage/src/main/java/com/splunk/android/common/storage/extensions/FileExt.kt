package com.splunk.android.common.storage.extensions

import java.io.File

fun File.createNewFileOnPath(): File {
    if (!exists()) {
        parentFile?.mkdirs()
        createNewFile()
    }
    return this
}
