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
