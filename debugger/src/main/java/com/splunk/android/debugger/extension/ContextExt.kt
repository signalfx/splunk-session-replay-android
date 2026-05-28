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
