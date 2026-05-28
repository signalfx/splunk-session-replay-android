package com.splunk.android.instrumentation.recording.wireframe.extension

import android.text.Layout
import com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

/* FIXME
 *  - Wrong text alignment when rect is small
 *  - DynamicDrawableSpan - SkeletonCanvas.drawText hook, measure width, create skeleton
 */

private val canvas = SkeletonCanvas()

internal fun Layout.forEachSkeleton(isTextSkeletonAllowed: Boolean, maxLines: Int, consumer: (Window.View.Skeleton) -> Unit) { // TODO Support IconSpan
    canvas.isTextSkeletonsAllowed = isTextSkeletonAllowed

    draw(canvas)

    var lineCount = 0
    var bottom = 0

    var i = 0
    while (i < canvas.skeletons.size) {
        val skeleton = canvas.skeletons[i]

        for (j in i + 1 until canvas.skeletons.size) {
            val nextSkeleton = canvas.skeletons[j]

            if (skeleton.rect.right != nextSkeleton.rect.left)
                break

            when {
                skeleton is Window.View.Skeleton.Color && nextSkeleton is Window.View.Skeleton.Color -> {
                    if (skeleton.colors != nextSkeleton.colors)
                        break

                    if (skeleton.radii != nextSkeleton.radii)
                        break
                }
                skeleton is Window.View.Skeleton.Text && nextSkeleton is Window.View.Skeleton.Text -> {
                    if (skeleton.color != nextSkeleton.color)
                        break

                    if (skeleton.size != nextSkeleton.size)
                        break

                    if (skeleton.font != nextSkeleton.font)
                        break
                }
                else ->
                    break
            }

            skeleton.rect.right = nextSkeleton.rect.right
            i = j
        }

        if (skeleton.rect.top > bottom) {
            if (++lineCount > maxLines)
                break

            bottom = skeleton.rect.bottom
        }

        consumer(skeleton)
        i++
    }

    canvas.skeletons.clear()
}
