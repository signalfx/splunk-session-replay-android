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
