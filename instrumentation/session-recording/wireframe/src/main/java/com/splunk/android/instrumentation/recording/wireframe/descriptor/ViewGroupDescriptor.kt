package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.graphics.Point
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import com.splunk.android.common.utils.extensions.getFragmentContainerViewTag
import com.splunk.android.instrumentation.recording.wireframe.extension.clipToPaddingCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.isInvisibleForWireframe
import com.splunk.android.instrumentation.recording.wireframe.extension.scale
import com.splunk.android.instrumentation.recording.wireframe.extension.zCompat
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window
import com.splunk.android.instrumentation.recording.wireframe.util.FragmentConsumer
import com.splunk.android.instrumentation.recording.wireframe.util.OrderedList
import com.splunk.android.instrumentation.recording.wireframe.util.ViewConsumer
import com.splunk.android.instrumentation.recording.wireframe.util.isRunningVisibilityAnimation
import kotlin.math.round

/*
* FIXME
*  - clipChildren
*  - First structure than drawables and texts
* */
open class ViewGroupDescriptor : ViewDescriptor() {

    override val intendedClass: Class<*>? = ViewGroup::class.java

    override fun describe(view: View, viewRect: Rect, clipRect: Rect, parentScaleX: Float, parentScaleY: Float, isParentSensitive: Boolean?, viewConsumer: ViewConsumer, fragmentConsumer: FragmentConsumer): Window.View {
        val description = super.describe(view, viewRect, clipRect, parentScaleX, parentScaleY, isParentSensitive, viewConsumer, fragmentConsumer)

        if (view !is ViewGroup)
            return description

        val type = getFragment(view)?.let { fragmentConsumer(it::class.java) } ?: description.type
        var subviews: MutableList<Window.View>? = null

        if (getExtractionMode(view) == ExtractionMode.TRAVERSE) {
            val viewClipRect = Rect(viewRect)

            if (view.clipToPaddingCompat) {
                viewClipRect.left += view.paddingLeft
                viewClipRect.top += view.paddingTop
                viewClipRect.right -= view.paddingRight
                viewClipRect.bottom -= view.paddingBottom
            }

            if (viewClipRect.intersect(clipRect)) {
                val scrollOffset = if (useScrollOffsetForChildren(view))
                    description.offset
                else
                    null

                val children = OrderedList<Window.View>(view.childCount)

                val viewScaleX = parentScaleX * view.scaleX
                val viewScaleY = parentScaleY * view.scaleY

                for (i in 0 until view.childCount) {
                    val childView = view.getChildAt(i)

                    if (childView.visibility != View.VISIBLE && !childView.isRunningVisibilityAnimation || childView.alpha == 0f || childView.isInvisibleForWireframe)
                        continue

                    val childRect = getViewRect(childView, viewRect, viewScaleX, viewScaleY, scrollOffset)

                    if (Rect.intersects(childRect, clipRect)) {
                        val isSensitive = description.isSensitive ?: isParentSensitive
                        children.insert(childView.zCompat, describeChild(view, childView, childRect, viewClipRect, viewScaleX, viewScaleY, isSensitive, viewConsumer))
                    }
                }

                if (children.isNotEmpty())
                    subviews = children.values
            }
        }

        return if (type != description.type || subviews != null)
            description.copy(
                type = type,
                subviews = subviews
            )
        else
            description
    }

    override fun getExtractionMode(view: View): ExtractionMode {
        return if ((view as ViewGroup).childCount == 0)
            ExtractionMode.CANVAS
        else
            ExtractionMode.TRAVERSE
    }

    protected open fun describeChild(view: ViewGroup, childView: View, childRect: Rect, viewClipRect: Rect, viewScaleX: Float, viewScaleY: Float, isParentSensitive: Boolean?, viewConsumer: ViewConsumer): Window.View {
        return viewConsumer(childView, childRect, viewClipRect, viewScaleX, viewScaleY, isParentSensitive)
    }

    protected open fun useScrollOffsetForChildren(view: View): Boolean {
        return true
    }

    private fun getViewRect(view: View, parentRect: Rect, parentScaleX: Float, parentScaleY: Float, scrollOffset: Point?): Rect {
        val scaledPivotX = round(view.pivotX * parentScaleX).toInt()
        val scaledPivotY = round(view.pivotY * parentScaleY).toInt()

        val scaledX = round(view.x * parentScaleX).toInt()
        val scaledY = round(view.y * parentScaleY).toInt()

        val rect = Rect(0, 0, view.width, view.height)
        rect.scale(parentScaleX, parentScaleY)
        rect.scale(view.scaleX, view.scaleY, scaledPivotX, scaledPivotY)

        var offsetX = parentRect.left + scaledX
        var offsetY = parentRect.top + scaledY

        if (scrollOffset != null) {
            offsetX -= scrollOffset.x
            offsetY -= scrollOffset.y
        }

        rect.offset(offsetX, offsetY)
        return rect
    }

    private fun getFragment(view: ViewGroup): Any? {
        if (fragmentContainerViewTag == null)
            fragmentContainerViewTag = view.context.getFragmentContainerViewTag()

        return fragmentContainerViewTag?.let { view.getTag(it) }
    }

    companion object {
        private var fragmentContainerViewTag: Int? = null
    }
}
