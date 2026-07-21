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

package com.splunk.android.instrumentation.recording.screenshot.extension

import android.view.SurfaceView
import com.splunk.rum.common.utils.extensions.get
import com.splunk.rum.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.screenshot.R

// MARK Requires Proguard rules

private val flutterSurfaceViewClass = "io.flutter.embedding.android.FlutterSurfaceView".toClass()

internal val SurfaceView.isDrawnOnTop: Boolean
    get() {
        var value = getTag(R.id.sl_tag_drawn_on_top) as? Boolean

        if (value != null)
            return value

        value = when (this::class.java) {
            flutterSurfaceViewClass ->
                runCatching { get<Boolean>("renderTransparently") }.getOrNull() ?: false
            else ->
                false // FIXME Investigate setZOrderOnTop
        }

        setTag(R.id.sl_tag_drawn_on_top, value)
        return value
    }
