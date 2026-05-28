package com.splunk.android.instrumentation.recording.core

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

// Requires Proguard rules. Keep annotation can not be used because of broken visibility in library.
internal class SessionReplayInstaller : ContentProvider() {

    override fun onCreate(): Boolean {
        val application = context?.applicationContext as Application
        Initializer.setup(application)
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
