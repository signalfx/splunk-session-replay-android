package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.appbar

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.extensions.get
import com.splunk.android.common.utils.extensions.invoke
import com.splunk.android.common.utils.extensions.plusAssign
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window
import com.splunk.android.instrumentation.recording.wireframe.util.ViewConsumer
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.internal.CollapsingTextHelper

/* MARK
 *  - Compatible with com.google.android.material:material of versions 1.2.0 and 1.5.0
 *  - Requires Proguard rules
 */
internal open class CollapsingToolbarLayoutDescriptor : FrameLayoutDescriptor() {

    private val canvas = SkeletonCanvas()

    override val intendedClass: Class<*>? = "com.google.android.material.appbar.CollapsingToolbarLayout".toClass()

    override fun describeChild(view: ViewGroup, childView: View, childRect: Rect, viewClipRect: Rect, viewScaleX: Float, viewScaleY: Float, isParentSensitive: Boolean?, viewConsumer: ViewConsumer): Window.View {
        val description = super.describeChild(view, childView, childRect, viewClipRect, viewScaleX, viewScaleY, isParentSensitive, viewConsumer)

        if (view !is CollapsingToolbarLayout)
            return description

        val contentScrimSkeleton = getContentScrimSkeletons(view, viewClipRect, viewScaleX, viewScaleY, childView, childRect)

        return if (contentScrimSkeleton != null) {
            val skeletons = ArrayList<Window.View.Skeleton>()
            skeletons += contentScrimSkeleton
            skeletons += description.skeletons
            description.copy(skeletons = skeletons)
        } else
            description
    }

    override fun getForegroundSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getForegroundSkeletons(view, isSensitive, result)

        if (view !is CollapsingToolbarLayout)
            return

        getForegroundContentScrimSkeletons(view, result)
        getCollapsingTextSkeletons(view, result)
        getStatusBarScrimSkeletons(view, result)
    }

    private fun getContentScrimSkeletons(view: CollapsingToolbarLayout, viewClipRect: Rect, viewScaleX: Float, viewScaleY: Float, childView: View, childViewRect: Rect): Window.View.Skeleton? {
        try {
            val isToolbarChild = view.invoke<Boolean>("isToolbarChild", childView to View::class.java) ?: return null
            val scrimAlpha = view.get<Int>("scrimAlpha") ?: return null
            val contentScrim = view.contentScrimSafe

            if (contentScrim != null && scrimAlpha > 0 && isToolbarChild) {
                val skeleton = contentScrim.getSkeleton() ?: return null
                transformSkeleton(skeleton, childViewRect.left, childViewRect.top, viewScaleX, viewScaleY)

                if (skeleton.rect.intersect(viewClipRect))
                    return skeleton
            }
        } catch (e: Exception) {
            Logger.e1(TAG, "getContentScrimSkeletons", e)
        }

        return null
    }

    private fun getForegroundContentScrimSkeletons(view: CollapsingToolbarLayout, result: MutableList<Window.View.Skeleton>) {
        try {
            val scrimAlpha = view.get<Int>("scrimAlpha") ?: return
            val toolbar = view.get<Any>("toolbar")
            val contentScrim = view.contentScrimSafe

            if (toolbar == null && contentScrim != null && scrimAlpha > 0)
                result += contentScrim.getSkeleton() ?: return
        } catch (e: Exception) {
            Logger.e1(TAG, "getForegroundContentScrimSkeletons", e)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun getCollapsingTextSkeletons(view: CollapsingToolbarLayout, result: MutableList<Window.View.Skeleton>) {
        try {
            val collapsingTextHelper = view.get<CollapsingTextHelper>("collapsingTextHelper") ?: return
            val drawCollapsingTitle = view.get<Boolean>("drawCollapsingTitle") ?: return

            if (view.isTitleEnabledSafe && drawCollapsingTitle) {
                val saveCount = canvas.save()
                canvas.clipRect(0, 0, view.width, view.height)
                collapsingTextHelper.invoke<Unit>("draw", canvas to Canvas::class.java)
                canvas.restoreToCount(saveCount)

                for (skeleton in canvas.skeletons)
                    result += skeleton

                canvas.skeletons.clear()
            }
        } catch (e: Exception) {
            Logger.e1(TAG, "getCollapsingTextSkeletons", e)
        }
    }

    private fun getStatusBarScrimSkeletons(view: CollapsingToolbarLayout, result: MutableList<Window.View.Skeleton>) {
        try {
            val statusBarScrim = view.get<Drawable>("statusBarScrim")
            val scrimAlpha = view.get<Int>("scrimAlpha") ?: return

            if (statusBarScrim != null && scrimAlpha > 0f) {
                val lastInsets = view.get<WindowInsetsCompat>("lastInsets")

                if (lastInsets != null && lastInsets.systemWindowInsetTop > 0)
                    result += statusBarScrim.getSkeleton() ?: return
            }
        } catch (e: Exception) {
            Logger.e1(TAG, "getStatusBarScrimSkeletons", e)
        }
    }

    private val CollapsingToolbarLayout.contentScrimSafe: Drawable?
        get() = try {
            contentScrim
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "getContentScrimSafe", e)
            null
        }

    private val CollapsingToolbarLayout.isTitleEnabledSafe: Boolean
        get() = try {
            isTitleEnabled
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "getIsTitleEnabledSafe", e)
            true
        }

    private companion object {
        const val TAG = "CollapsingToolbarLayoutDescriptor"
    }
}
