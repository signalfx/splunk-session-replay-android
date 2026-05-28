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

package com.splunk.android.debugger.extension

import android.graphics.Point
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

private val locBuffer = IntArray(2)

internal fun View.getRectOnScreen(): Rect {
    getLocationOnScreen(locBuffer)

    val left = locBuffer[0]
    val top = locBuffer[1]
    val right = left + width
    val bottom = top + height

    return Rect(left, top, right, bottom)
}

internal fun View.findParentWithAnimations(): ViewGroup? {
    var currentParent = parent as? ViewGroup ?: return null

    do {
        if (currentParent.layoutTransition != null)
            return currentParent

        currentParent = currentParent.parent as? ViewGroup ?: return null
    } while (true)
}

internal fun <T : View> T.withDisabledAnimations(block: (T) -> Unit) {
    findParentWithAnimations()?.withDisabledAnimations { block(this) } ?: block(this)
}

internal fun View.getOffsetRelativeToParent(root: ViewGroup): Point {
    if (this === root)
        throw IllegalArgumentException()

    var x = x
    var y = y

    var parent = parent as? ViewGroup

    while (parent != null && parent !== root) {
        x += parent.x
        y += parent.y

        parent = parent.parent as? ViewGroup
    }

    return Point(x.toInt(), y.toInt())
}

@Suppress("UNCHECKED_CAST")
internal fun <T : ViewGroup.LayoutParams> View.updateLayoutParams(block: (T) -> Unit) {
    val layoutParams = layoutParams
    block(layoutParams as T)
    this.layoutParams = layoutParams
}
