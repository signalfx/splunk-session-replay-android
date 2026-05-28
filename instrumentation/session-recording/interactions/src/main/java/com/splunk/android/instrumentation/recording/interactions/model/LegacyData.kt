package com.splunk.android.instrumentation.recording.interactions.model

import android.graphics.Rect
import android.view.View
import com.splunk.android.common.utils.extensions.activity
import com.splunk.android.common.utils.extensions.ciscoId
import com.splunk.android.common.utils.legacy.getViewIdentifier
import com.splunk.android.instrumentation.recording.interactions.extension.globalVisibleRect

data class LegacyData internal constructor(
    val activityName: String,
    val viewFrame: Rect,
    val viewId: String,
    val viewName: String
) {

    internal companion object {

        fun create(rootView: View, targetView: View?): LegacyData {
            return LegacyData(
                activityName = targetView?.activity?.javaClass?.simpleName ?: "",
                viewFrame = targetView?.globalVisibleRect ?: rootView.globalVisibleRect,
                viewId = targetView?.ciscoId ?: targetView?.getViewIdentifier() ?: "-",
                viewName = (targetView ?: rootView).javaClass.simpleName
            )
        }
    }
}
