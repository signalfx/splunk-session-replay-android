package com.splunk.android.instrumentation.recording.interactions.extension

import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import com.splunk.android.common.utils.reflector.Reflector

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
