package com.splunk.android.sr.testapp.util

import android.content.Context
import com.splunk.android.common.storage.cache.FileSimplePermanentCache
import com.splunk.android.common.storage.extensions.noBackupFilesDirCompat
import com.splunk.android.common.storage.filemanager.FileManagerFactory
import com.splunk.android.common.storage.preferences.Preferences
import com.splunk.android.common.utils.Lock
import com.splunk.android.common.utils.runOnBackgroundThread
import java.io.File

class AppPreferences private constructor() {

    private var preferences: Preferences? = null
    private val lock = Lock()

    var isContentSensitive: Boolean?
        get() = requirePreferences().getBoolean(KEY_IS_CONTENT_SENSITIVE)
        set(value) {
            if (value != null)
                requirePreferences().putBoolean(KEY_IS_CONTENT_SENSITIVE, value)
            else
                requirePreferences().remove(KEY_IS_CONTENT_SENSITIVE)
        }

    fun load(context: Context) {
        if (preferences == null) {
            lock.lock()

            runOnBackgroundThread {
                preferences = Preferences(FileSimplePermanentCache(File(context.noBackupFilesDirCompat, FILE), FileManagerFactory.createPlainFileManager()))
                lock.unlock()
            }
        }
    }

    private fun requirePreferences(): Preferences {
        lock.waitToUnlock()
        return preferences ?: throw IllegalStateException("Preferences are not loaded")
    }

    companion object {

        private const val FILE = "preferences/app.dat"

        private const val KEY_IS_CONTENT_SENSITIVE = "isContentSensitive"

        private var instance: AppPreferences? = null

        fun getInstance(): AppPreferences {
            return instance ?: AppPreferences().also { instance = it }
        }
    }
}
