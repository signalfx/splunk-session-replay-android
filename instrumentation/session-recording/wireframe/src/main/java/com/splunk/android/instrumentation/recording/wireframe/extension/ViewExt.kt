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

package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import com.splunk.rum.common.utils.extensions.get
import com.splunk.rum.common.utils.runOnAndroidAtLeast
import com.splunk.android.instrumentation.recording.wireframe.R

var View.isInvisibleForWireframe: Boolean
    get() = getTag(R.id.sr_tag_invisible_wireframe) == true
    set(value) = setTag(R.id.sr_tag_invisible_wireframe, value)

internal var View.isSensitiveOverride: Boolean?
    get() = getTag(R.id.sr_tag_is_sensitive) as? Boolean
    set(value) = setTag(R.id.sr_tag_is_sensitive, value)

internal val View.zCompat: Float
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.LOLLIPOP) { z } ?: 0f

internal val View.elevationCompat: Float
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.LOLLIPOP) { elevation } ?: 0f

internal val View.foregroundCompat: Drawable?
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.M) { foreground } ?: (this as? FrameLayout)?.foreground

internal fun View.canScroll(): Boolean {
    return canScrollVertically(1) || canScrollVertically(-1) || canScrollHorizontally(1) || canScrollHorizontally(-1)
}

internal enum class LayoutDirection(val constant: Int) {
    LTR(0), // View.LAYOUT_DIRECTION_LTR
    RTL(1) // View.LAYOUT_DIRECTION_RTL
}

internal val View.layoutDirectionCompat: LayoutDirection
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            when (layoutDirection) {
                View.LAYOUT_DIRECTION_RTL -> LayoutDirection.RTL
                else -> LayoutDirection.LTR
            }
        } else
            LayoutDirection.LTR
    }

private const val PFLAG_DRAWN = 0x00000020 // View.PFLAG_DRAWN
private var isUnableToGetPrivateFlags = false

internal val View.isDrawn: Boolean
    get() {
        if (isUnableToGetPrivateFlags)
            return true

        var isDrawn = getTag(R.id.sr_tag_is_drawn) as? Boolean

        if (isDrawn != true) {
            val flags = runCatching { get<Int>("mPrivateFlags") }.getOrNull()

            if (flags == null) {
                isUnableToGetPrivateFlags = true
                return true
            }

            isDrawn = (flags and PFLAG_DRAWN) > 0

            if (isDrawn)
                setTag(R.id.sr_tag_is_drawn, isDrawn)
        }

        return isDrawn
    }
