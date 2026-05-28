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

import android.graphics.Rect
import android.view.View
import com.splunk.android.instrumentation.recording.interactions.R
import com.splunk.android.instrumentation.recording.interactions.compose.TargetElementHolder

var View.isInvisibleForInteractions: Boolean
    get() = getTag(R.id.sl_tag_invisible_interactions) == true
    set(value) = setTag(R.id.sl_tag_invisible_interactions, value)

internal val View.globalVisibleRect: Rect
    get() {
        val rect = Rect()
        getGlobalVisibleRect(rect)
        return rect
    }

internal var View.composeTargetElementHolder: TargetElementHolder?
    get() = getTag(R.id.sl_tag_motion_event_target_element) as? TargetElementHolder
    set(value) = setTag(R.id.sl_tag_motion_event_target_element, value)
