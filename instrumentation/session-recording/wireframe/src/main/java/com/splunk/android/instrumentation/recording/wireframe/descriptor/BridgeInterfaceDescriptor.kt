package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.graphics.Rect
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import com.splunk.android.bridge.model.BridgeInterface
import com.splunk.android.common.utils.Lock
import com.splunk.android.instrumentation.recording.wireframe.extension.WireframeView
import com.splunk.android.instrumentation.recording.wireframe.extension.foregroundCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.isDrawDeterministic
import com.splunk.android.instrumentation.recording.wireframe.extension.removeAll
import com.splunk.android.instrumentation.recording.wireframe.extension.replaceView
import com.splunk.android.instrumentation.recording.wireframe.extension.scale
import com.splunk.android.instrumentation.recording.wireframe.extension.toWireframeView
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.instrumentation.recording.wireframe.util.FragmentConsumer
import com.splunk.android.instrumentation.recording.wireframe.util.ViewConsumer
import java.lang.ref.WeakReference

internal class BridgeInterfaceDescriptor(
    override val intendedClass: Class<*>?,
    private val bridgeInterface: BridgeInterface
) : ViewGroupDescriptor() {

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    override fun isDrawDeterministic(view: View): Boolean {
        return view !is SurfaceView && super.isDrawDeterministic(view) && (view !is ViewGroup || view.containsDrawDeterministicElements())
    }

    override fun describe(view: View, viewRect: Rect, clipRect: Rect, parentScaleX: Float, parentScaleY: Float, isParentSensitive: Boolean?, viewConsumer: ViewConsumer, fragmentConsumer: FragmentConsumer): Wireframe.Frame.Scene.Window.View {
        var description = super.describe(view, viewRect, clipRect, parentScaleX, parentScaleY, isParentSensitive, viewConsumer, fragmentConsumer)

        val subviews = description.subviews ?: ArrayList()
        val subviewsLock = Lock(true)
        val weakView = WeakReference(view)

        description = description.copy(subviews = subviews, subviewsLock = subviewsLock)

        bridgeInterface.obtainWireframeData(view) {
            val localView = weakView.get()

            if (it != null && localView != null) {
                val views = it.root.toWireframeView()

                transformView(localView, viewRect, views, parentScaleX, parentScaleY)
                description.removeAll { views.replaceView(it.id, it) }
                subviews += views
            }

            subviewsLock.unlock()
        }

        return description
    }

    private fun transformView(view: View, viewRect: Rect, wireframeView: WireframeView, parentScaleX: Float, parentScaleY: Float) {
        val viewScaleX = parentScaleX * view.scaleX
        val viewScaleY = parentScaleY * view.scaleY

        val offsetX = viewRect.left
        val offsetY = viewRect.top

        transformView(wireframeView, viewScaleX, viewScaleY, offsetX, offsetY)
    }

    private fun transformView(wireframeView: WireframeView, scaleX: Float, scaleY: Float, offsetX: Int, offsetY: Int) {
        wireframeView.rect.scale(scaleX, scaleY)
        wireframeView.rect.offset(offsetX, offsetY)

        if (wireframeView.skeletons != null)
            for (skeleton in wireframeView.skeletons) {
                skeleton.rect.scale(scaleX, scaleY)
                skeleton.rect.offset(offsetX, offsetY)
            }

        if (wireframeView.subviews != null)
            for (subview in wireframeView.subviews)
                transformView(subview, scaleX, scaleY, offsetX, offsetY)
    }

    private fun ViewGroup.containsDrawDeterministicElements(): Boolean {
        for (i in 0 until childCount) {
            val view = getChildAt(i)

            if (view is SurfaceView)
                return false

            if (view.background?.isDrawDeterministic == false || view.foregroundCompat?.isDrawDeterministic == false)
                return false

            if (view is ViewGroup && !view.containsDrawDeterministicElements())
                return false
        }

        return true
    }
}
