package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.recyclerview

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.common.utils.reflector.Reflector
import com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

/* MARK
 *  - Compatible with androidx.recyclerview:recyclerview:1.2.1
 *  - Requires Proguard rules
 */
internal open class RecyclerViewDescriptor : ViewGroupDescriptor() {

    private val canvas = SkeletonCanvas()
    private val reflector = Reflector(2, 0, 0)

    override val intendedClass: Class<*>? = "androidx.recyclerview.widget.RecyclerView".toClass()

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getSkeletons(view, isSensitive, result)

        if (view !is RecyclerView)
            return

        extractItemDecorationSkeletons(view, RecyclerView.ItemDecoration::onDraw, result)
    }

    override fun getForegroundSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getForegroundSkeletons(view, isSensitive, result)

        if (view !is RecyclerView)
            return

        extractItemDecorationSkeletons(view, RecyclerView.ItemDecoration::onDrawOver, result)
    }

    private fun extractItemDecorationSkeletons(view: RecyclerView, func: DrawFunc, result: MutableList<Window.View.Skeleton>) {
        reflector.reflect {
            try {
                val items = view.get<ArrayList<RecyclerView.ItemDecoration>>("mItemDecorations") ?: return@reflect
                val state = view.get<RecyclerView.State>("mState") ?: return@reflect

                for (item in items) {
                    func(item, canvas, view, state)
                    result += canvas.skeletons
                    canvas.skeletons.clear()
                }
            } catch (e: Exception) {
                Logger.e1(TAG, "extractItemDecorationSkeletons", e)
            }
        }
    }

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    private companion object {
        const val TAG = "RecyclerViewDescriptor"
    }
}

private typealias DrawFunc = RecyclerView.ItemDecoration.(c: Canvas, parent: RecyclerView, state: RecyclerView.State) -> Unit
