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
