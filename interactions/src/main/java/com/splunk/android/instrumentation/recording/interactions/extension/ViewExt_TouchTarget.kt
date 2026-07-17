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

import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import com.splunk.rum.common.utils.reflector.Reflector

private val reflector = Reflector(4, 0, 0)

internal fun View.findTouchTarget(): View? {
    if (this !is ViewGroup)
        return this

    return reflector.reflect {
        var view: View? = this@findTouchTarget
        var target: Any?

        do {
            target = view?.get("mFirstTouchTarget")

            if (target == null && view is AbsListView) {
                if (!view.isPressed)
                    break

                val position: Int
                val firstPosition: Int

                try {
                    position = view.get("mMotionPosition") ?: break
                    firstPosition = view.get("mFirstPosition") ?: break
                } catch (_: NoSuchFieldException) {
                    break
                }

                view = view.getChildAt(position - firstPosition) // TODO This is not accurate when fast click with mouse
            }

            view = target?.get("child") ?: break
        } while (view is ViewGroup)

        view
    }
}
