package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.cardview

import android.view.View
import androidx.cardview.widget.CardView
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.dpToPxF
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags.Shadow

// FIXME foreground bounds

/* MARK
 *  - Compatible with androidx.cardview:cardview:1.0.0
 */
internal open class CardViewDescriptor : FrameLayoutDescriptor() {

    private val canvas = SkeletonCanvas()

    override val intendedClass: Class<*>? = "androidx.cardview.widget.CardView".toClass()

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Skeleton>) {
        if (view !is CardView)
            return

        view.background.draw(canvas)

        result += if (view.cardElevationSafe >= SHADOW_THRESHOLD)
            canvas.skeletons.map { if (it is Skeleton.Color) it.copy(flags = Skeleton.Color.Flags(Shadow.DARK)) else it }
        else
            canvas.skeletons

        canvas.skeletons.clear()
    }

    private val CardView.cardElevationSafe: Float
        get() = try {
            cardElevation
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "getCardElevationSafe", e)
            0f
        }

    private companion object {
        const val TAG = "CardViewDescriptor"

        val SHADOW_THRESHOLD = dpToPxF(5f)
    }
}
