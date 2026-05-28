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

import android.view.View
import android.view.ViewGroup
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.screenshot.R

private val popupWindowDecorViewClass = "android.widget.PopupWindow\$PopupDecorView".toClass()
private val popupWindowBackgroundViewClass = "android.widget.PopupWindow\$PopupBackgroundView".toClass()
private val decorViewClass = "com.android.internal.policy.DecorView".toClass()
private val decorViewClass21 = "com.android.internal.policy.impl.PhoneWindow\$DecorView".toClass()
private val decorViewClass23 = "com.android.internal.policy.PhoneWindow\$DecorView".toClass()

var View.isInvisibleForScreenshot: Boolean
    get() {
        if (this is ViewGroup)
            when (this::class.java) {
                popupWindowDecorViewClass -> {
                    val child = getChildAt(0) as? ViewGroup

                    return if (child != null && child::class.java == popupWindowBackgroundViewClass)
                        child.getChildAt(0)?.isInvisibleForScreenshot == true
                    else
                        child?.isInvisibleForScreenshot == true
                }
                decorViewClass, decorViewClass21, decorViewClass23 -> {
                    return findViewById<View>(android.R.id.content)?.isInvisibleForScreenshot == true
                }
            }

        return getTag(R.id.sl_tag_invisible_screenshot) == true
    }
    set(value) = setTag(R.id.sl_tag_invisible_screenshot, value)

internal fun View.forEach(consumer: (View) -> Unit) {
    consumer(this)

    if (this is ViewGroup)
        for (i in 0 until childCount)
            getChildAt(i)?.forEach(consumer)
}
