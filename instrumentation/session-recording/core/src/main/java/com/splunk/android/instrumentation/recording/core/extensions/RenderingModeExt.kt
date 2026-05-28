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

import com.splunk.android.instrumentation.recording.capturer.FrameCapturer
import com.splunk.android.instrumentation.recording.core.api.RenderingMode
import com.splunk.android.instrumentation.recording.core.data.RenderingDataSource

internal fun RenderingMode.toFrameCapturerMode(): FrameCapturer.Mode {
    return when (this) {
        RenderingMode.NATIVE -> FrameCapturer.Mode.WIREFRAME_SCREENSHOT
        RenderingMode.WIREFRAME_ONLY -> FrameCapturer.Mode.WIREFRAME
    }
}

internal fun RenderingMode.toRenderingDataOption(): List<RenderingDataSource> {
    return when (this) {
        RenderingMode.NATIVE -> listOf(
            RenderingDataSource.WIREFRAME,
            RenderingDataSource.NATIVE
        )
        RenderingMode.WIREFRAME_ONLY -> listOf(
            RenderingDataSource.WIREFRAME
        )
    }
}
