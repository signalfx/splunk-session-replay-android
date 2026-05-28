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

package com.splunk.android.debugger.extension

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

internal fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

internal fun Context.createFileInDownloadDirectory(fileName: String): OutputStream? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val extension = fileName.substring(fileName.lastIndexOf('.'))
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

        val contentValues = ContentValues()
        contentValues[MediaStore.MediaColumns.DISPLAY_NAME] = fileName
        contentValues[MediaStore.MediaColumns.MIME_TYPE] = mimeType
        contentValues[MediaStore.MediaColumns.RELATIVE_PATH] = Environment.DIRECTORY_DOWNLOADS

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) contentResolver.openOutputStream(uri) else null
    } else {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        FileOutputStream(file)
    }
}

internal val Context.applicationName: String
    get() = applicationInfo.let { it.nonLocalizedLabel?.toString() ?: getString(it.labelRes) }
