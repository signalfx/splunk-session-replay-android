package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.graphics.Point
import android.graphics.Rect
import android.view.View
import com.splunk.android.common.utils.dpToPxF
import com.splunk.android.common.utils.extensions.ciscoIdWithPositionInList
import com.splunk.android.common.utils.extensions.identity
import com.splunk.android.common.utils.extensions.plusAssign
import com.splunk.android.instrumentation.recording.wireframe.canvas.LoggingSkeletonCanvas
import com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas
import com.splunk.android.instrumentation.recording.wireframe.extension.canScroll
import com.splunk.android.instrumentation.recording.wireframe.extension.elevationCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.foregroundCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.extension.isDrawDeterministic
import com.splunk.android.instrumentation.recording.wireframe.extension.isDrawn
import com.splunk.android.instrumentation.recording.wireframe.extension.isInside
import com.splunk.android.instrumentation.recording.wireframe.extension.isSensitiveOverride
import com.splunk.android.instrumentation.recording.wireframe.extension.isSimpleRect
import com.splunk.android.instrumentation.recording.wireframe.extension.scale
import com.splunk.android.instrumentation.recording.wireframe.extension.translate
import com.splunk.android.instrumentation.recording.wireframe.model.SensitivityDeterminer
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags.Shadow
import com.splunk.android.instrumentation.recording.wireframe.stats.StatsCollector
import com.splunk.android.instrumentation.recording.wireframe.util.FragmentConsumer
import com.splunk.android.instrumentation.recording.wireframe.util.LayoutTransitionObserver
import com.splunk.android.instrumentation.recording.wireframe.util.ViewConsumer

/*
 * MARK
 *  - All Fragment transactions are done by ViewGroup.getChildTransformation() that can not be obtained by reflection.
 *
 * FIXME
 *  - Check all Rect creations
 *  - Foreground first and maybe ignore content and background
 *  - support library
 *  - Blinking skeletons in OttPlayer
 *
 * FIXME Sample app
 *  - clipChildren
 *  - View negative scale (mirror)
 *  - Missing arrow in Spinner on Android 5 (NinePathDrawable)
 *  - Wrong ProgressBar size on Android 5 (NinePathDrawable)
 *  - progress_bar_3 in RTL
 *  * Android 5 - Missing foreground in view_1_2 (foreground in View is supported from Android 6)
 *
 * MARK NewPipe from 2022.04.29
 *
 * FIXME Wikipedia from 2022.05.17
 *  - Missing arrow in text "Today on Wikipedia ->" (DynamicDrawableSpan) (Only on newer Android)
 *  - Wrong dot skeleton on vertical line in Dashboard in "On this day" section. clipChildren in view_card_on_this_day
 *  - Preview is covered by bottom sheet in More -> Setting -> App theme
 *  * Missing detail content (WebView)
 *
 * FIXME WordPress from 2022.05.17
 *  - Unable to click on Smartlook button in "Log in" section
 *
 * FIXME EasyDiary from 2022.05.19
 *  - Missing tutorial overlay
 *  - Missing screen transitions
 *  - Detail -> Encrypt Diary - Covered debug tool
 *
 * MARK OmniNotes from 2022.03.24
 *  * FloatingButton - Label background color (algorithm limitation)
 *
 * FIXME AntennaPod from 2022.02.04
 *  * Statistics > Subscriptions/Downloads - Chart (Chart in Drawable)
 *  * Statistics > Years - Chart (Chart in Drawable)
 *  * Left menu > Very Scary People > Detail > Detail > Missing text (WebView)
 *  * Play something > Playnotes - Missing text (WebView)
 *  - Search - Missing cross in last items
 *
 * FIXME Amaze File Manager from 2022.04.01
 *  - Extras menu background is transparent
 *  - Missing elevation shadow under extras menu
 *  * Settings > About > Top image is one big Bitmap
 *  * Open text file > Details / File > Properties - Chart (Bitmap)
 *
 * FIXME Materialistic from 2021.08.02
 *  - Settings > Comments - Cropped "Color code opacity" slider
 *  * Missing content (WebView)
 *
 * MARK Phonograph from 2020.08.25
 *  * Window is under status bar - Custom StatusBar implementation
 *  * Settings > Appearance - Green objects (Images)
 *  * ProgressBar progress - Custom implementation
 *
 * FIXME Unipetrol
 *  - Wireframe is not updating on splash screen after close debug tool
 *  * Missing texts in Onboarding (WebView)
 *  * Wrong navigation bar color (navigation bar is drawn itself and gray color is from background under the bar) !!! Draw into SkeletonCanvas > discard drawn children (stack trace?)
 *
 * FIXME Loop Habit Tracker from 2022.09.11
 *  - Missing circles in color change dialog
 *
 * FIXME JetSurvey
 *  - Wrong skeleton color of options
 */
/**
 * Describe [View] and create description for [Wireframe].
 */
open class ViewDescriptor {

    protected enum class ExtractionMode {
        TRAVERSE, CANVAS
    }

    /**
     * Defines for which [View] implementation is this [ViewDescriptor] determined.
     */
    open val intendedClass: Class<*>? = View::class.java

    open fun describe(view: View, viewRect: Rect, clipRect: Rect, parentScaleX: Float, parentScaleY: Float, isParentSensitive: Boolean?, viewConsumer: ViewConsumer, fragmentConsumer: FragmentConsumer): Window.View {
        val skeletons = ArrayList<Skeleton>(DEFAULT_SKELETON_LIST_SIZE)
        val foregroundSkeletons = ArrayList<Skeleton>(DEFAULT_FOREGROUND_SKELETON_LIST_SIZE)

        val isSensitive = view.isSensitiveOverride ?: sensitivityDeterminer?.isViewSensitive(view)
        val isWireframeSensitive = (isSensitive ?: isParentSensitive) != false

        when (getExtractionMode(view)) {
            ExtractionMode.TRAVERSE ->
                extractSkeletonsStandard(view, viewRect, clipRect, parentScaleX, parentScaleY, isWireframeSensitive, skeletons, foregroundSkeletons)
            ExtractionMode.CANVAS ->
                extractSkeletonsCanvas(extractionCanvas, view, viewRect, clipRect, parentScaleX, parentScaleY, isWireframeSensitive, skeletons)
        }

        LayoutTransitionObserver.listenTransitions(view)

        return Window.View(
            id = view.ciscoIdWithPositionInList,
            name = view::class.java.simpleName,
            rect = viewRect,
            type = getType(view),
            typename = view::class.java.name,
            hasFocus = view.hasFocus(),
            offset = getScrollOffset(view),
            alpha = view.alpha,
            skeletons = skeletons.ifEmpty { null },
            foregroundSkeletons = foregroundSkeletons.ifEmpty { null },
            subviews = null,
            identity = view.identity,
            isDrawDeterministic = isDrawDeterministic(view),
            isSensitive = isSensitive,
            subviewsLock = null
        )
    }

    protected open fun getType(view: View): Window.View.Type? {
        return if (view.isClickable)
            Window.View.Type.BUTTON
        else
            null
    }

    protected open fun getExtractionMode(view: View): ExtractionMode {
        return when (view::class) {
            View::class -> ExtractionMode.TRAVERSE
            else -> ExtractionMode.CANVAS
        }
    }

    protected open fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Skeleton>) {
        val flags = if (view.elevationCompat >= SHADOW_THRESHOLD) // FIXME This is not correct
            Skeleton.Color.Flags(Shadow.DARK)
        else
            null

        result += view.background?.getSkeleton(flags)
    }

    protected open fun getForegroundSkeletons(view: View, isSensitive: Boolean, result: MutableList<Skeleton>) {
        result += view.foregroundCompat?.getSkeleton()
    }

    protected open fun getScrollOffset(view: View): Point? {
        return if (view.isScrollContainer || view.canScroll())
            Point(view.scrollX, view.scrollY)
        else
            null
    }

    protected open fun isDrawDeterministic(view: View): Boolean {
        return view.background?.isDrawDeterministic != false && view.foregroundCompat?.isDrawDeterministic != false
    }

    private fun extractSkeletonsStandard(view: View, viewRect: Rect, clipRect: Rect, parentScaleX: Float, parentScaleY: Float, isSensitive: Boolean, skeletons: MutableList<Skeleton>, foregroundSkeletons: MutableList<Skeleton>) {
        getSkeletons(view, isSensitive, skeletons)
        getForegroundSkeletons(view, isSensitive, foregroundSkeletons)

        transformSkeletons(skeletons, view, viewRect, parentScaleX, parentScaleY)
        transformSkeletons(foregroundSkeletons, view, viewRect, parentScaleX, parentScaleY)

        clipSkeletons(skeletons, viewRect, clipRect)
        clipSkeletons(foregroundSkeletons, viewRect, clipRect)
    }

    private fun transformSkeletons(skeletons: MutableList<Skeleton>, view: View, viewRect: Rect, parentScaleX: Float, parentScaleY: Float) {
        val scaleX = parentScaleX * view.scaleX
        val scaleY = parentScaleY * view.scaleY

        for (i in skeletons.indices) {
            val skeleton = skeletons[i]

            val translationX = viewRect.left
            val translationY = viewRect.top

            transformSkeleton(skeleton, translationX, translationY, scaleX, scaleY)
        }
    }

    protected fun transformSkeleton(skeleton: Skeleton, left: Int, top: Int, scaleX: Float, scaleY: Float) {
        skeleton.rect.scale(scaleX, scaleY)
        skeleton.rect.offset(left, top)
    }

    private fun clipSkeletons(skeletons: MutableList<Skeleton>, viewRect: Rect, clipRect: Rect) {
        for (i in skeletons.indices.reversed()) {
            val skeleton = skeletons[i]

            if (!skeleton.rect.intersect(viewRect)) {
                skeletons.removeAt(i)
                continue
            }

            if (skeleton.isSimpleRect) {
                if (!skeleton.rect.intersect(clipRect))
                    skeletons.removeAt(i)

                continue
            }

            if (!Rect.intersects(skeleton.rect, clipRect))
                skeletons.removeAt(i)
            else if (!skeleton.rect.isInside(clipRect))
                skeletons[i] = when (skeleton) {
                    is Skeleton.Color ->
                        skeleton.copy(clipRect = Rect(clipRect))
                    is Skeleton.Text ->
                        skeleton.copy(clipRect = Rect(clipRect))
                }
        }
    }

    // To keep the exact behavior with Android, all element are rendered, even when they are later cropped by clipRect.
    internal fun extractSkeletonsCanvas(canvas: SkeletonCanvas, view: View, viewRect: Rect, clipRect: Rect, parentScaleX: Float, parentScaleY: Float, isSensitive: Boolean, skeletons: MutableList<Skeleton>?) {
        if (view.isLayoutRequested || !Rect.intersects(viewRect, clipRect) || !view.isDrawn)
            return

        StatsCollector.measureCanvasTime {
            VIEW_CLIP_RECT.set(viewRect)
            VIEW_CLIP_RECT.offset(-viewRect.left, -viewRect.top)

            canvas.isTextSkeletonsAllowed = !isSensitive

            val saveCount = canvas.save()
            canvas.translate(viewRect.left, viewRect.top)
            canvas.scale(view.scaleX * parentScaleX, view.scaleY * parentScaleY)
            canvas.clipRect(VIEW_CLIP_RECT)
            view.draw(canvas)
            canvas.restoreToCount(saveCount)

            canvas.isTextSkeletonsAllowed = false

            if (skeletons != null)
                for (i in canvas.skeletons.indices) {
                    val skeleton = canvas.skeletons[i]

                    if (skeleton.isSimpleRect) {
                        if (skeleton.rect.intersect(clipRect))
                            skeletons += skeleton

                        continue
                    }

                    if (!Rect.intersects(skeleton.rect, clipRect))
                        continue

                    skeletons += if (!skeleton.rect.isInside(clipRect))
                        when (skeleton) {
                            is Skeleton.Color -> skeleton.copy(clipRect = clipRect)
                            is Skeleton.Text -> skeleton.copy(clipRect = clipRect)
                        }
                    else
                        skeleton
                }

            val count = canvas.skeletons.size
            canvas.skeletons.clear()

            count
        }
    }

    companion object {

        private const val DEFAULT_SKELETON_LIST_SIZE = 8
        private const val DEFAULT_FOREGROUND_SKELETON_LIST_SIZE = 2

        private val LOGGING_SKELETON_CANVAS = LoggingSkeletonCanvas()
        private val SKELETON_CANVAS = SkeletonCanvas()

        private val VIEW_CLIP_RECT = Rect()

        private var extractionCanvas: SkeletonCanvas = SKELETON_CANVAS

        private val SHADOW_THRESHOLD = dpToPxF(5f)

        internal var isCanvasCallsLoggingEnabled: Boolean
            get() = extractionCanvas === LOGGING_SKELETON_CANVAS
            set(value) {
                extractionCanvas = if (value) LOGGING_SKELETON_CANVAS else SKELETON_CANVAS
            }

        internal var sensitivityDeterminer: SensitivityDeterminer? = null
    }
}
