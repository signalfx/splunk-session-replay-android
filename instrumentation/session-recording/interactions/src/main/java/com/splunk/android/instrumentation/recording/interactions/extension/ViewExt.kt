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
