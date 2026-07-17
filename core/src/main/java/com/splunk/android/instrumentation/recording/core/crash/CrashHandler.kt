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

package com.splunk.android.instrumentation.recording.core.crash

import com.splunk.rum.common.logger.Logger
import com.splunk.android.instrumentation.recording.core.lifecycle.ILifecycleHandler

internal class CrashHandler(
    private val lifecycleHandler: ILifecycleHandler
) : ICrashHandler {
    private var originalUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    override fun register() {
        Logger.d(TAG, "register()")
        originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            uncaughtException(thread, throwable)
        }
    }

    override fun unregister() {
        Logger.d(TAG, "unregister()")
        originalUncaughtExceptionHandler?.let {
            Thread.setDefaultUncaughtExceptionHandler(it)
        }
    }

    private fun uncaughtException(thread: Thread, throwable: Throwable) {
        Logger.d(TAG, "uncaughtException()")
        lifecycleHandler.applicationCrash(throwable)
        originalUncaughtExceptionHandler?.uncaughtException(thread, throwable)
    }

    private companion object Companion {
        const val TAG = "CrashHandler"
    }
}
