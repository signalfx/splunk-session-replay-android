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

package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.vico

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor

/* MARK
 *  - Compatible with com.patrykandpatrick.vico:core:2.0.0-beta.1, com.patrykandpatrick.vico:views:2.0.0-beta.1
 */
internal open class ChartViewDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.patrykandpatrick.vico.views.common.ChartView".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.CANVAS
    }
}
