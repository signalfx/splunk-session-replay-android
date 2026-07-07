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

package com.splunk.android.instrumentation.recording.interactions.extension

import android.content.Context
import android.os.Build
import android.view.Display
import com.splunk.android.common.utils.extensions.windowManager
import com.splunk.android.common.utils.runOnAndroidAtLeast

@get:Suppress("DEPRECATION")
internal val Context.displayCompat: Display
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.R) { display } ?: windowManager.defaultDisplay

internal val Context.isAssociatedWithDisplayCompat: Boolean
    get() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            try {
                // ContextImpl.getDisplay() calls ContextImpl.isAssociatedWithDisplay() and throws exception when false
                display
            } catch (_: UnsupportedOperationException) {
                return false
            }

        return true
    }
