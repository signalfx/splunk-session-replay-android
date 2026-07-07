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

package com.splunk.android.instrumentation.recording.screenshot

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.splunk.android.common.utils.dpToPxF
import com.splunk.android.common.utils.extensions.barrier
import com.splunk.android.common.utils.extensions.copyOrNull
import com.splunk.android.common.utils.extensions.forEachFast
import com.splunk.android.common.utils.extensions.get
import com.splunk.android.common.utils.extensions.hasDimBehind
import com.splunk.android.common.utils.extensions.noneFast
import com.splunk.android.common.utils.extensions.plusAssign
import com.splunk.android.common.utils.extensions.safeSubmit
import com.splunk.android.common.utils.extensions.sortedItemsByDecorViews
import com.splunk.android.common.utils.runOnUiThread
import com.splunk.android.instrumentation.recording.screenshot.cache.BitmapCache
import com.splunk.android.instrumentation.recording.screenshot.extension.asMutableMap
import com.splunk.android.instrumentation.recording.screenshot.extension.drawRoundRect
import com.splunk.android.instrumentation.recording.screenshot.extension.forEach
import com.splunk.android.instrumentation.recording.screenshot.extension.isDrawnOnTop
import com.splunk.android.instrumentation.recording.screenshot.extension.isInvisibleForScreenshot
import com.splunk.android.instrumentation.recording.screenshot.extension.removeFirst
import com.splunk.android.instrumentation.recording.screenshot.extension.removeLast
import com.splunk.android.instrumentation.recording.screenshot.image.ImageCopy
import com.splunk.android.instrumentation.recording.screenshot.image.ImageCopy16
import com.splunk.android.instrumentation.recording.screenshot.image.ImageCopy24
import com.splunk.android.instrumentation.recording.screenshot.image.ImageCopy26
import com.splunk.android.instrumentation.recording.screenshot.image.ImageCopy34
import com.splunk.android.instrumentation.recording.screenshot.model.Screenshot
import com.splunk.android.instrumentation.recording.screenshot.stats.ScreenshotStats
import com.splunk.android.instrumentation.recording.screenshot.stats.StatsCollector
import com.splunk.android.instrumentation.recording.screenshot.utils.LazyWorker
import com.splunk.android.instrumentation.recording.screenshot.utils.SensitiveOverlayPaint
import com.splunk.android.instrumentation.recording.wireframe.extension.calcSensitiveViewsVisibleRects
import com.splunk.android.instrumentation.recording.wireframe.extension.findViewByInstance
import com.splunk.android.instrumentation.recording.wireframe.extension.findWindow
import com.splunk.android.instrumentation.recording.wireframe.extension.waitToFinish
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import java.lang.ref.WeakReference
import java.util.LinkedList
import java.util.concurrent.Executors

/* FIXME
 *  - Sensitive mask sometimes not cover sensitive area perfectly
 *  - Sensitive view after Fragment transaction
 *  - PopupMenu elevation (AmazeFileManager)
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenshotConstructor(private val listener: Listener) {

    private val worker = LazyWorker("ScreenshotConstructor")
    private val copyExecutor = Executors.newCachedThreadPool()

    private val imageCopy: ImageCopy

    private val surfaces = ArrayList<SurfaceView>()
    private val viewHolders = LinkedList<ViewHolder.Root>()
    private val pendingActions = LinkedList<PendingAction>()

    private val sensitivePaint = SensitiveOverlayPaint()
    private val debugPaint = Paint()
    private val tempRect = Rect()
    private val canvas = Canvas()

    private val elevationOutline = Outline()
    private val elevationPaint = Paint()
    private val elevationRect = Rect()

    private var lastFrameBitmap: Bitmap? = null
    private var isContentChanged = false

    var screenMasks: List<Rect>? = null

    init {
        elevationPaint.color = 0x00000000
        debugPaint.strokeWidth = DEBUG_BONE_SIZE

        imageCopy = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                ImageCopy34()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                ImageCopy26()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
                ImageCopy24()
            else ->
                ImageCopy16()
        }
    }

    fun openNewFrame() {}

    fun updateView(view: View, windowDescription: Wireframe.Frame.Scene.Window?) {
        pendingActions.removeLast { it.view === view }
        pendingActions += PendingAction.Update(view, windowDescription)
    }

    fun removeView(view: View) {
        pendingActions.removeLast { it.view === view }
        pendingActions += PendingAction.Remove(view)
    }

    fun closeFrame(frame: Wireframe.Frame): Boolean {
        return worker.submit(
            preprocess = { memorySafeBlock { processPendingActions() } },
            process = { memorySafeBlock { processFrame(frame) } }
        )
    }

    fun clear() {
        pendingActions.clear()

        worker.submitPriority {
            viewHolders.forEachFast { it.release() }
            viewHolders.clear()

            lastFrameBitmap?.let { BitmapCache.release(it) }
            lastFrameBitmap = null

            isContentChanged = true
        }
    }

    private fun processPendingActions() {
        isContentChanged = false

        for (action in pendingActions) {
            val view = action.view ?: continue

            when (action) {
                is PendingAction.Update ->
                    processUpdateViewAction(view, action.windowDescription)
                is PendingAction.Remove ->
                    processRemoveViewAction(view)
            }
        }

        pendingActions.clear()
    }

    private fun processFrame(frame: Wireframe.Frame) {
        val screenshot: Screenshot?

        StatsCollector.measureGeneralTime {
            screenshot = processCloseFrame(frame)
        }

        val stats = StatsCollector.popStats()
        runOnUiThread { listener.onNewScreenshot(screenshot, stats, isContentChanged) }
    }

    private fun processUpdateViewAction(view: View, windowDescription: Wireframe.Frame.Scene.Window?) {
        if (view.visibility != View.VISIBLE || view.width == 0 || view.height == 0 || view.isInvisibleForScreenshot) {
            val viewHolder = viewHolders.removeLast { it.view === view }

            if (viewHolder != null) {
                isContentChanged = true
                viewHolder.release()
            }

            return
        }

        val viewDescription = windowDescription?.findViewByInstance(view) ?: return
        val viewHolder = viewHolders.obtain(view) { ViewHolder.Root(view) }

        viewHolder.windowDescription = windowDescription
        viewHolder.viewDescription = viewDescription
        viewHolder.isUpdated = true

        view.forEach {
            if (it is SurfaceView && it.holder.surface.isValid && it.width > 0 && it.height > 0)
                surfaces += it
        }

        for (surface in surfaces) {
            val surfaceDescription = viewHolder.windowDescription.findViewByInstance(surface)

            if (surfaceDescription == null) {
                val surfaceViewHolder = viewHolder.surfaceViewHolders.removeFirst { it.view === surface } ?: continue
                BitmapCache.release(surfaceViewHolder.bitmap)
                continue
            }

            val surfaceViewHolder = viewHolder.surfaceViewHolders.obtain(surface) { ViewHolder.Surface(surface) }
            surfaceViewHolder.viewDescription = surfaceDescription
        }

        if (viewHolder.surfaceViewHolders.isNotEmpty()) {
            val iterator = viewHolder.surfaceViewHolders.iterator()

            while (iterator.hasNext()) {
                val surfaceHolder = iterator.next()

                if (surfaces.noneFast { it === surfaceHolder.view }) {
                    BitmapCache.release(surfaceHolder.bitmap)
                    iterator.remove()
                }
            }
        }

        isContentChanged = true
        surfaces.clear()
    }

    private fun processRemoveViewAction(view: View) {
        val viewHolder = viewHolders.removeFirst { it.view === view } ?: return
        isContentChanged = true
        viewHolder.release()
    }

    private fun processCloseFrame(frame: Wireframe.Frame): Screenshot? {
        val lastFrameBitmap = lastFrameBitmap

        if (!isContentChanged && lastFrameBitmap != null)
            return null

        val firstScene = frame.scenes.first()
        val sceneRect = firstScene.rect

        if (sceneRect.isEmpty)
            return Screenshot(firstScene.time, DUMMY_BITMAP)

        val sceneWidth = sceneRect.width()
        val sceneHeight = sceneRect.height()

        val bitmap = if (lastFrameBitmap == null || lastFrameBitmap.width != sceneWidth || lastFrameBitmap.height != sceneHeight) {
            if (lastFrameBitmap != null)
                BitmapCache.release(lastFrameBitmap)

            BitmapCache.obtain(sceneWidth, sceneHeight)
        } else {
            lastFrameBitmap.eraseColor(Color.BLACK)
            lastFrameBitmap
        }

        val preFrame = listener.onRequestPreFrame() ?: return null
        val midFrame = listener.onRequestMidFrame() ?: return null

        val viewHolderPairs = viewHolders.mapNotNull { item -> item.view?.let { it to item } } // TODO a lot of temp object creation
        val viewHolders = viewHolderPairs.sortedItemsByDecorViews()

        viewHolders.forEach { viewHolder, view ->
            if (viewHolder.isUpdated)
                copyViews(viewHolder, view)
        }

        val postFrame = listener.onRequestPostFrame() ?: return null

        viewHolders.forEach { viewHolder, view ->
            if (viewHolder.isUpdated) {
                viewHolder.isUpdated = false
                coverSensitiveViews(viewHolder, view, preFrame, midFrame, postFrame)
            }

            drawRoot(viewHolder, view, bitmap)
        }

        drawMasks(bitmap)

        this.lastFrameBitmap = bitmap

        val bitmapCopy = bitmap.copyOrNull() ?: return null
        return Screenshot(firstScene.time, bitmapCopy)
    }

    private inline fun MutableList<ViewHolder.Root>.forEach(crossinline consumer: (ViewHolder.Root, View) -> Unit) {
        var index = 0

        while (index < size) {
            val viewHolder = get(index)
            val view = viewHolder.view

            if (view == null) {
                viewHolder.release()
                removeAt(index)
                continue
            }

            consumer(viewHolder, view)
            index++
        }
    }

    private fun copyViews(viewHolder: ViewHolder.Root, view: View) {
        StatsCollector.measureCopyTime {
            barrier(0) {
                it.increase()
                copyExecutor.safeSubmit {
                    StatsCollector.measureWindowCopyTime {
                        imageCopy.copyWindow(view, viewHolder.windowDescription, viewHolder.viewDescription, viewHolder.bitmap)
                    }
                    it.decrease()
                }

                val surfaceIterator = viewHolder.surfaceViewHolders.iterator()

                while (surfaceIterator.hasNext()) {
                    val surfaceViewHolder = surfaceIterator.next()
                    val surfaceView = surfaceViewHolder.view

                    if (surfaceView == null) {
                        surfaceViewHolder.release()
                        surfaceIterator.remove()
                        continue
                    }

                    it.increase()
                    copyExecutor.safeSubmit {
                        StatsCollector.measureSurfaceCopyTime {
                            imageCopy.copySurface(surfaceView, surfaceViewHolder.bitmap)
                        }
                        it.decrease()
                    }
                }
            }
        }
    }

    private fun coverSensitiveViews(viewHolder: ViewHolder.Root, view: View, preFrame: Wireframe.Frame, midFrame: Wireframe.Frame, postFrame: Wireframe.Frame) {
        StatsCollector.measureSensitivityTime {
            val preWindow = preFrame.findWindow(view)
            val postWindow = postFrame.findWindow(view)

            preWindow?.waitToFinish()
            postWindow?.waitToFinish()

            val preRectMap = preWindow?.calcSensitiveViewsVisibleRects()
            val postRectMap = postWindow?.calcSensitiveViewsVisibleRects()?.asMutableMap()

            val viewRect = viewHolder.viewDescription.rect
            val offsetX = -viewRect.left
            val offsetY = -viewRect.top

            canvas.setBitmap(viewHolder.bitmap)

            if (isDebugModeEnabled) {
                val drawSensitiveArea: (map: Map<String, List<Rect>>, color: Int) -> Unit = { map, color ->
                    for ((_, rects) in map)
                        for (rect in rects) {
                            rect.offset(offsetX, offsetY)

                            debugPaint.style = Paint.Style.FILL
                            debugPaint.color = color
                            debugPaint.alpha = 50

                            canvas.drawRect(rect, debugPaint)

                            debugPaint.style = Paint.Style.STROKE
                            debugPaint.color = color

                            canvas.drawRect(rect, debugPaint)
                        }
                }

                val midWindow = midFrame.findWindow(view)

                midWindow?.waitToFinish()

                val midRectMap = midWindow?.calcSensitiveViewsVisibleRects()

                if (preRectMap != null)
                    drawSensitiveArea(preRectMap, 0xffff0000.toInt())

                if (midRectMap != null)
                    drawSensitiveArea(midRectMap, 0xff00ff00.toInt())

                if (postRectMap != null)
                    drawSensitiveArea(postRectMap, 0xff0000ff.toInt())
            } else {
                val drawCover: (Rect) -> Unit = {
                    it.offset(offsetX, offsetY)
                    canvas.drawRect(it, sensitivePaint)
                }

                if (preRectMap != null)
                    for ((preViewIdentity, preRects) in preRectMap) {
                        val postRects = postRectMap?.remove(preViewIdentity)

                        if (postRects != null) {
                            if (preRects.size == postRects.size)
                                preRects.forEachIndexed { i, preRect ->
                                    preRect.union(postRects[i])
                                    drawCover(preRect)
                                }
                            else {
                                tempRect.set(0, 0, 0, 0)

                                for (r in preRects)
                                    tempRect.union(r)

                                for (r in postRects)
                                    tempRect.union(r)

                                drawCover(tempRect)
                            }
                        } else
                            for (rect in preRects)
                                drawCover(rect)
                    }

                if (postRectMap?.isNotEmpty() == true)
                    for ((_, rects) in postRectMap)
                        for (rect in rects)
                            drawCover(rect)
            }
        }
    }

    private fun drawRoot(viewHolder: ViewHolder.Root, view: View, bitmap: Bitmap) {
        StatsCollector.measureFinalDrawTime {
            canvas.setBitmap(bitmap)

            val layoutParams = view.layoutParams as? WindowManager.LayoutParams
            if (layoutParams != null && layoutParams.hasDimBehind()) {
                val alpha = (layoutParams.dimAmount * 255).toInt()
                canvas.drawARGB(alpha, 0, 0, 0)
            }

            val drawSurfaces: ((SurfaceView) -> Boolean) -> Unit = { predicate ->
                for (surfaceViewHolder in viewHolder.surfaceViewHolders) {
                    val surfaceView = surfaceViewHolder.view ?: continue

                    if (predicate(surfaceView)) {
                        val rect = surfaceViewHolder.viewDescription.rect
                        canvas.drawBitmap(surfaceViewHolder.bitmap, null, rect, null)
                    }
                }
            }

            drawRootViewShadow(canvas, viewHolder, view)
            drawSurfaces { !it.isDrawnOnTop }
            canvas.drawBitmap(viewHolder.bitmap, null, viewHolder.viewDescription.rect, null)
            drawSurfaces { it.isDrawnOnTop }
        }
    }

    private fun drawRootViewShadow(canvas: Canvas, viewHolder: ViewHolder.Root, view: View) {
        val rect = viewHolder.viewDescription.rect
        val background = view.background
        val elevation = view.elevation

        if (elevation > 0f && background != null) {
            background.getOutline(elevationOutline)

            if (!elevationOutline.isEmpty) {
                var isBackgroundValid = false
                var outlineRect = elevationRect
                var outlineRadius = 0f

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (elevationOutline.getRect(elevationRect)) {
                        outlineRadius = elevationOutline.radius
                        outlineRect = elevationRect
                        isBackgroundValid = true
                    }
                } else {
                    try {
                        val localRadius = elevationOutline.get<Float>("mRadius")
                        val localRect = elevationOutline.get<Rect>("mRect")

                        if (localRadius != null && localRect != null) {
                            elevationRect.set(localRect)
                            outlineRect = elevationRect
                            outlineRadius = localRadius
                            isBackgroundValid = true
                        }
                    } catch (_: Exception) {
                    }
                }

                if (isBackgroundValid) {
                    outlineRect.offset(rect.left, rect.top)
                    elevationPaint.setShadowLayer(elevation, 0f, elevation / 3f, 0x48000000) // TODO spotShadowAlpha

                    if (outlineRadius == 0f)
                        canvas.drawRect(outlineRect, elevationPaint)
                    else if (outlineRadius > 0f)
                        canvas.drawRoundRect(outlineRect, outlineRadius, outlineRadius, elevationPaint)
                }
            }
        }
    }

    private fun drawMasks(bitmap: Bitmap) {
        val screenMasks = screenMasks?.takeIf { it.isNotEmpty() } ?: return
        canvas.setBitmap(bitmap)

        for (mask in screenMasks)
            canvas.drawRect(mask, sensitivePaint)
    }

    private inline fun memorySafeBlock(crossinline block: () -> Unit): Boolean {
        return try {
            block()
            true
        } catch (_: OutOfMemoryError) {
            false
        }
    }

    private inline fun <K : View, reified V : ViewHolder<K>> LinkedList<V>.obtain(view: K, crossinline create: () -> V): V {
        var viewHolder = find { it.view === view }

        if (viewHolder == null) {
            val bitmap = BitmapCache.obtain(view.width, view.height)

            viewHolder = create()
            viewHolder.bitmap = bitmap

            add(viewHolder)
        } else if (viewHolder.bitmap.width != view.width || viewHolder.bitmap.height != view.height) {
            BitmapCache.release(viewHolder.bitmap)

            viewHolder.bitmap = BitmapCache.obtain(view.width, view.height)
        }

        return viewHolder
    }

    private sealed class ViewHolder<T : View>(view: T) {

        private val weakView = WeakReference(view)

        abstract var bitmap: Bitmap

        val view: T?
            get() = weakView.get()

        class Root(view: View) : ViewHolder<View>(view) {

            val surfaceViewHolders = LinkedList<Surface>()

            var isUpdated = false

            override lateinit var bitmap: Bitmap

            lateinit var windowDescription: Wireframe.Frame.Scene.Window
            lateinit var viewDescription: Wireframe.Frame.Scene.Window.View

            fun release() {
                for (holder in surfaceViewHolders)
                    holder.release()

                BitmapCache.release(bitmap)
            }
        }

        class Surface(view: SurfaceView) : ViewHolder<SurfaceView>(view) {

            override lateinit var bitmap: Bitmap

            lateinit var viewDescription: Wireframe.Frame.Scene.Window.View

            fun release() {
                BitmapCache.release(bitmap)
            }
        }
    }

    private sealed class PendingAction(view: View) {

        private val weakView = WeakReference(view)

        val view: View?
            get() = weakView.get()

        class Update(view: View, val windowDescription: Wireframe.Frame.Scene.Window?) : PendingAction(view)

        class Remove(view: View) : PendingAction(view)
    }

    interface Listener {

        fun onNewScreenshot(screenshot: Screenshot?, stats: ScreenshotStats, isChanged: Boolean)

        fun onRequestPreFrame(): Wireframe.Frame?

        fun onRequestMidFrame(): Wireframe.Frame?

        fun onRequestPostFrame(): Wireframe.Frame?
    }

    companion object {
        private val DUMMY_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        private val DEBUG_BONE_SIZE = dpToPxF(2f)

        var isDebugModeEnabled = false
    }
}
