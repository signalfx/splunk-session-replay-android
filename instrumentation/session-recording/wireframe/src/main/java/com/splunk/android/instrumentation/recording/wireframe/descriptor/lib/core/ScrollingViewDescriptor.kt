package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.core

import android.graphics.Point
import android.view.View
import androidx.core.view.ScrollingView
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

/* FIXME
 *  - Check RecyclerView from support library
 */
internal open class ScrollingViewDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.core.view.ScrollingView".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    override fun getScrollOffset(view: View): Point? {
        if (view !is ScrollingView)
            return super.getScrollOffset(view)

        return try {
            val x = view.computeHorizontalScrollOffset()
            val y = view.computeVerticalScrollOffset()

            Point(x, y)
        } catch (e: Throwable) {
            Logger.e1(TAG, "getScrollOffset", e)
            super.getScrollOffset(view)
        }
    }

    override fun useScrollOffsetForChildren(view: View): Boolean {
        return false
    }

    private companion object {
        const val TAG = "ScrollingViewDescriptor"
    }
}
