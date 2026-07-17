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

package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.Rect
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import com.splunk.rum.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas
import com.splunk.android.instrumentation.recording.wireframe.estimator.DrawableColorsEstimator
import com.splunk.android.instrumentation.recording.wireframe.estimator.DrawableColorsEstimator.unwrap
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton.Color.Type
import kotlin.math.min
import kotlin.math.roundToInt

/* FIXME
 *  - Rect creation
 *  - DrawableWrapper
 *  - NinePatchDrawable
 *  - Shadow in Paint
 */

// MARK TOM resources.get<Any>("mResourcesImpl")?.get<Any>("mDrawableCache")?.get<ArrayMap<Any, LongSparseArray<WeakReference<Any>>>>("mThemedEntries")

private val animatedVectorDrawableClass = "android.graphics.drawable.AnimatedVectorDrawable".toClass()
private val vectorDrawableClass = "android.graphics.drawable.VectorDrawable".toClass()

private val skeletonCanvas = SkeletonCanvas()

internal fun Drawable.getSkeleton(flags: Skeleton.Color.Flags? = null): Skeleton? {
    val drawable = unwrap() ?: return null
    val rect = getRect()

    val colors = DrawableColorsEstimator.estimate(drawable, rect)

    if (!colors.isClearlyVisible())
        return null

    val radii: Skeleton.Color.Radii?
    val isOpaque: Boolean

    when (drawable) {
        is GradientDrawable -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val drawableRadius = drawable.cornerRadius

                if (drawableRadius != 0f)
                    radii = Skeleton.Color.Radii(
                        radius = min(min(rect.width(), rect.height()) / 2, drawableRadius.roundToInt())
                    )
                else {
                    val drawableRadii = try {
                        drawable.cornerRadii
                    } catch (_: NullPointerException) {
                        // Because of a bug on older Android
                        // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.Object float[].clone()' on a null object reference
                        null
                    }

                    if (drawableRadii != null)
                        radii = Skeleton.Color.Radii(
                            topLeft = min(drawableRadii[0], drawableRadii[1]).roundToInt(),
                            topRight = min(drawableRadii[2], drawableRadii[3]).roundToInt(),
                            bottomRight = min(drawableRadii[4], drawableRadii[5]).roundToInt(),
                            bottomLeft = min(drawableRadii[6], drawableRadii[7]).roundToInt(),
                        )
                    else
                        radii = null
                }

                isOpaque = radii == null && colors.isOpaque()
            } else {
                isOpaque = false
                radii = null
            }
        }
        is ColorDrawable -> {
            isOpaque = colors.isOpaque()
            radii = null
        }
        else -> {
            isOpaque = false
            radii = null
        }
    }

    return Skeleton.Color(rect, null, Type.GENERAL, colors, radii, flags, isOpaque)
}

private fun Drawable.getRect(): Rect {
    val rect = Rect()

    when {
        this is LayerDrawable -> {
            for (i in 0 until numberOfLayers) {
                if (getId(i) == android.R.id.mask)
                    continue

                val drawable = getDrawable(i) ?: continue
                rect.union(drawable.getRect())
            }
        }
        this is InsetDrawable -> { // FIXME DrawableWrapper
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                drawable?.let { rect.union(it.getRect()) }
            else
                forEachSkeleton { rect.union(it.rect) }
        }
        this is DrawableContainer -> {
            @Suppress("UNNECESSARY_SAFE_CALL", "USELESS_ELVIS")
            val current = current?.getRect() ?: return rect // Null in Omni-Notes (create note > create category > tap on color)
            rect.union(current)
        }
        this is ClipDrawable || this::class.java == animatedVectorDrawableClass || this::class.java == vectorDrawableClass -> {
            rect.set(bounds)
        }
        else -> {
            forEachSkeleton {
                if (it is Skeleton.Color && it.colors.isClearlyVisible())
                    rect.union(it.rect)
            }
        }
    }

    return rect
}

private fun Drawable.forEachSkeleton(consumer: (Skeleton) -> Unit) {
    val saveCount = skeletonCanvas.save()
    draw(skeletonCanvas)
    skeletonCanvas.restoreToCount(saveCount)

    for (skeleton in skeletonCanvas.skeletons)
        consumer(skeleton)

    skeletonCanvas.skeletons.clear()
}
