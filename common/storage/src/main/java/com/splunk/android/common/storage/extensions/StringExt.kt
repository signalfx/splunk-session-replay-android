package com.splunk.android.common.storage.extensions

import java.io.File

internal fun String.toFile(): File {
    return File(this)
}
