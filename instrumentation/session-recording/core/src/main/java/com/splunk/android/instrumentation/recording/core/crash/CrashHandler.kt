package com.splunk.android.instrumentation.recording.core.crash

import com.splunk.android.common.logger.Logger
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
