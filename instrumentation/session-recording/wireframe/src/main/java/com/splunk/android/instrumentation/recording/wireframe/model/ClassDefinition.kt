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

package com.splunk.android.instrumentation.recording.wireframe.model

import android.os.Parcelable
import com.splunk.android.instrumentation.recording.wireframe.WireframeExtractor
import kotlinx.parcelize.Parcelize

/**
 * Class definition.
 *
 * @param isInternal Whether the class is probably internal implementation. Means, this probably is not public library.
 *
 * @see WireframeExtractor
 */
@Parcelize
data class ClassDefinition internal constructor(
    val className: String,
    val ancestors: List<String>,
    val isInternal: Boolean
) : Parcelable
