package com.splunk.android.instrumentation.recording.core.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.Display

internal val Activity.displayCompat: Display?
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.R) { display } ?: windowManager.defaultDisplay

/**
 * When you need to be sure [Activity] was measured (all views have correct "size") and attached to
 * [Window], call your code wrapped in this callback.
 */
internal fun Activity.runWhenActivityIsMeasuredAndAttachedToWindow(toRun: (activity: Activity) -> Unit) {
    this.window.decorView.post {
        toRun.invoke(this)
    }
}

/**
 * Perform [block] when Android version is at least [versionCode].
 *
 * @param versionCode Android SDK version code from [Build.VERSION_CODES]
 */
@SuppressLint("AnnotateVersionCheck")
internal inline fun <T> runOnAndroidAtLeast(versionCode: Int, crossinline block: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= versionCode) {
        block()
    } else {
        null
    }
}
