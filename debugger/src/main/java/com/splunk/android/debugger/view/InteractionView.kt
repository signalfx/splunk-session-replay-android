package com.splunk.android.debugger.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.Choreographer
import com.splunk.android.common.utils.dpToPxF
import com.splunk.android.common.utils.extensions.findInstance
import com.splunk.android.debugger.R
import com.splunk.android.debugger.extension.aspectRatio
import com.splunk.android.debugger.extension.clipOutPathCompat
import com.splunk.android.debugger.extension.none
import com.splunk.android.debugger.extension.plusAssign
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

internal class InteractionView(context: Context, attrs: AttributeSet? = null) : WrapContentView(context, attrs) {

    private val touchFingerIcons: TouchIcons by lazy {
        TouchIcons(
            pointerDrawable = context.resources.getDrawable(R.drawable.sld_interaction_finger_pointer, context.theme),
            tapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_finger_tap, context.theme),
            doubleTapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_finger_double_tap, context.theme),
            longPressDrawable = context.resources.getDrawable(R.drawable.sld_interaction_finger_long_press, context.theme),
            rageTapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_finger_rage_tap, context.theme)
        )
    }

    private val touchStylusIcons: TouchIcons by lazy {
        TouchIcons(
            pointerDrawable = context.resources.getDrawable(R.drawable.sld_interaction_stylus_pointer, context.theme),
            tapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_stylus_tap, context.theme),
            doubleTapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_stylus_double_tap, context.theme),
            longPressDrawable = context.resources.getDrawable(R.drawable.sld_interaction_stylus_long_press, context.theme),
            rageTapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_stylus_rage_tap, context.theme)
        )
    }

    private val touchMouseIcons: TouchIcons by lazy {
        TouchIcons(
            pointerDrawable = context.resources.getDrawable(R.drawable.sld_interaction_mouse_pointer, context.theme),
            tapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_mouse_tap, context.theme),
            doubleTapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_mouse_double_tap, context.theme),
            longPressDrawable = context.resources.getDrawable(R.drawable.sld_interaction_mouse_long_press, context.theme),
            rageTapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_mouse_rage_tap, context.theme)
        )
    }

    private val touchUnknownIcons: TouchIcons by lazy {
        TouchIcons(
            pointerDrawable = ColorDrawable(),
            tapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_unknown_tap, context.theme),
            doubleTapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_unknown_double_tap, context.theme),
            longPressDrawable = context.resources.getDrawable(R.drawable.sld_interaction_unknown_long_press, context.theme),
            rageTapDrawable = context.resources.getDrawable(R.drawable.sld_interaction_unknown_rage_tap, context.theme)
        )
    }

    private val phoneButtonBackDrawable = context.resources.getDrawable(R.drawable.sld_interaction_phone_button_back, context.theme)
    private val phoneButtonVolumeDownDrawable = context.resources.getDrawable(R.drawable.sld_interaction_phone_button_volume_down, context.theme)
    private val phoneButtonVolumeUpDrawable = context.resources.getDrawable(R.drawable.sld_interaction_phone_button_volume_up, context.theme)

    private val gesturePinchDrawable = context.resources.getDrawable(R.drawable.sld_interaction_gesture_pinch, context.theme)
    private val gestureRotateDrawable = context.resources.getDrawable(R.drawable.sld_interaction_gesture_rotation, context.theme)

    private val keyboardDrawable = resources.getDrawable(R.drawable.sld_interaction_keyboard, context.theme)

    private val choreographer = Choreographer.getInstance()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    init {
        paint.textSize = dpToPxF(14f)
        paint.textAlign = Paint.Align.CENTER

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
            setLayerType(LAYER_TYPE_SOFTWARE, null) // Because of shadow layer

        for (drawable in arrayOf(gesturePinchDrawable, gestureRotateDrawable, phoneButtonBackDrawable, phoneButtonVolumeDownDrawable, phoneButtonVolumeUpDrawable))
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    }

    var wireframeScene: Wireframe.Frame.Scene? = null
        set(value) {
            field = value

            requestLayout()
            invalidate()
        }

    var interactions: List<Interaction> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    var timestampReference: Long? = null
        set(value) {
            field = value
            invalidate()
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (visibility == VISIBLE)
            choreographer.postFrameCallback(frameCallback)
    }

    override fun onDetachedFromWindow() {
        choreographer.removeFrameCallback(frameCallback)
        super.onDetachedFromWindow()
    }

    override fun setVisibility(visibility: Int) {
        if (visibility != super.getVisibility())
            if (visibility == VISIBLE)
                choreographer.postFrameCallback(frameCallback)
            else
                choreographer.removeFrameCallback(frameCallback)

        super.setVisibility(visibility)
    }

    override fun getContentAspectRatio(): Float {
        return wireframeScene?.rect?.aspectRatio ?: 0f
    }

    override fun onDraw(canvas: Canvas) {
        val wireframeScene = wireframeScene ?: return
        val relevantInteractions = filterInteractions(interactions).asReversed()

        if (relevantInteractions.isNotEmpty())
            scheduleRedraw()

        val saveCount = canvas.save()
        val scale = width.toFloat() / wireframeScene.rect.width()
        canvas.scale(scale, scale)

        drawTargetElement(canvas, relevantInteractions)
        drawMultiTouchGestureConnection(canvas, relevantInteractions)
        drawMultiTouchGestureCenter(canvas, relevantInteractions)
        drawInteractions(canvas, relevantInteractions)

        canvas.restoreToCount(saveCount)
    }

    private fun scheduleRedraw() {
        choreographer.removeFrameCallback(frameCallback)
        choreographer.postFrameCallback(frameCallback)
    }

    private fun filterInteractions(interactions: List<Interaction>): List<Interaction> {
        val interactionsReversed = interactions.asReversed()

        val consumedContinuousGestures = HashSet<Class<out Interaction>>()
        val filteredPointerIds = HashSet<Int>()

        val result = ArrayList<Interaction>()
        val time = getTimestampReference()

        for (i in interactionsReversed.indices) {
            val interaction = interactionsReversed[i]
            val elapsed = time - interaction.timestamp

            when (interaction) {
                is Interaction.PhoneButton -> {
                    if (elapsed < PHONE_BUTTON_VISIBILITY_DURATION)
                        result += interaction
                }
                is Interaction.Touch.Gesture -> {
                    if (interaction is Interaction.Touch.Continuous) {
                        if (interaction::class.java !in consumedContinuousGestures) {
                            consumedContinuousGestures += interaction::class.java

                            if (!interaction.isLast || elapsed < GESTURE_VISIBILITY_DURATION) {
                                result += interaction

                                pointers@ for (pointerId in interaction.pointerIds)
                                    for (pointer in interactionsReversed)
                                        if (pointer is Interaction.Touch.Pointer && pointer.pointerId == pointerId && pointer.timestamp <= interaction.timestamp) {
                                            result += interaction
                                            continue@pointers
                                        }
                            }
                        }
                    } else if (elapsed < GESTURE_VISIBILITY_DURATION) {
                        filteredPointerIds += interaction.pointerIds
                        result += interaction
                    }
                }
                is Interaction.Touch.Pointer -> {
                    if ((!interaction.isLast || elapsed < POINTER_VISIBILITY_DURATION) && interaction.pointerId !in filteredPointerIds) {
                        filteredPointerIds += interaction.pointerId
                        result += interaction
                    } else if (interaction.isLast)
                        filteredPointerIds += interaction.pointerId
                }
                is Interaction.Keyboard -> {
                    if (interaction.rect != null)
                        if (interactions.none(interactions.size - i) { it is Interaction.Keyboard && it.rect == null })
                            result += interaction
                }
                is Interaction.Focus -> {
                    if (interaction.targetElementPath != null)
                        if (interactions.none(interactions.size - i) { it is Interaction.Focus })
                            result += interaction
                }
                is Interaction.Orientation ->
                    Unit // TODO Draw
            }
        }

        return result
    }

    private fun drawTargetElement(canvas: Canvas, interactions: List<Interaction>) {
        val wireframeScene = wireframeScene ?: return

        val gestures = HashSet<Class<out Interaction>>()
        var interactionOfTarget: Interaction.Touch? = null

        for (interaction in interactions)
            if (interaction is Interaction.Touch.Gesture && interaction.targetElementPath != null) {
                interactionOfTarget = interaction
                gestures.add(interaction::class.java)
            }

        val targetElementPath = interactionOfTarget?.targetElementPath ?: return

        for (window in wireframeScene.windows) {
            val view = targetElementPath.lastOrNull()?.view ?: continue

            val alpha = calcInteractionAlpha(interactionOfTarget, GESTURE_VISIBILITY_DURATION)
            val saveCountAlpha = canvas.saveLayerAlpha(null, alpha)

            paint.color = 0x80ffffff.toInt()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = TARGET_ELEMENT_INNER_STROKE

            val innerLeft = view.rect.left + TARGET_ELEMENT_INNER_STROKE_HALF
            val innerTop = view.rect.top + TARGET_ELEMENT_INNER_STROKE_HALF
            val innerRight = view.rect.right - TARGET_ELEMENT_INNER_STROKE_HALF
            val innerBottom = view.rect.bottom - TARGET_ELEMENT_INNER_STROKE_HALF

            canvas.drawRect(innerLeft, innerTop, innerRight, innerBottom, paint)

            paint.color = 0xffFFAE6D.toInt()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = TARGET_ELEMENT_STROKE

            canvas.drawRect(view.rect, paint)

            paint.color = 0xffFFAE6D.toInt()
            paint.style = Paint.Style.FILL

            val drawIcon: (Int, Drawable) -> Unit = { index, drawable ->
                val left = view.rect.right - TARGET_ELEMENT_STROKE_HALF - TARGET_ELEMENT_RECT_SIZE * (index + 1)
                val top = view.rect.top + TARGET_ELEMENT_STROKE_HALF
                val right = view.rect.right - TARGET_ELEMENT_STROKE_HALF - TARGET_ELEMENT_RECT_SIZE * index
                val bottom = view.rect.top + TARGET_ELEMENT_STROKE_HALF + TARGET_ELEMENT_RECT_SIZE

                canvas.drawRect(left, top, right, bottom, paint)

                val drawableLeft = left + ((right - left) - drawable.intrinsicWidth) / 2f
                val drawableTop = top + ((bottom - top) - drawable.intrinsicHeight) / 2f

                val saveCount = canvas.save()
                canvas.translate(drawableLeft, drawableTop)
                drawable.draw(canvas)
                canvas.restoreToCount(saveCount)
            }

            gestures.forEachIndexed { index, clazz ->
                when (clazz) {
                    Interaction.Touch.Gesture.Pinch::class.java ->
                        drawIcon(index, gesturePinchDrawable)
                    Interaction.Touch.Gesture.Rotation::class.java ->
                        drawIcon(index, gestureRotateDrawable)
                    Interaction.Touch.Gesture.Swipe::class.java ->
                        TODO("Not implemented")
                    else ->
                        Unit
                }
            }

            canvas.restoreToCount(saveCountAlpha)
            break
        }
    }

    private fun drawMultiTouchGestureConnection(canvas: Canvas, interactions: List<Interaction>) {
        val focus: Interaction.Touch.Focusable = interactions.findInstance() ?: return
        val gesture = focus as Interaction.Touch.Gesture
        val pointerCount = interactions.count { it is Interaction.Touch.Pointer && it.pointerId in gesture.pointerIds }

        if (pointerCount < 2)
            return

        val lines = FloatArray(pointerCount * 4)
        var lineIndex = 0

        for (interaction in interactions) {
            if (interaction !is Interaction.Touch.Pointer || interaction.pointerId !in gesture.pointerIds)
                continue

            val x1 = focus.focusX.toFloat()
            val y1 = focus.focusY.toFloat()

            var vx = interaction.x.toFloat() - x1
            var vy = interaction.y.toFloat() - y1

            val length = sqrt(vx * vx + vy * vy)
            val newLength = length - POINTER_RADIUS

            vx = vx / length * newLength
            vy = vy / length * newLength

            lines[lineIndex + 0] = x1
            lines[lineIndex + 1] = y1
            lines[lineIndex + 2] = x1 + vx
            lines[lineIndex + 3] = y1 + vy

            lineIndex += 4
        }

        val alpha = calcInteractionAlpha(gesture, GESTURE_VISIBILITY_DURATION)
        val saveCount = canvas.saveLayerAlpha(null, alpha)

        paint.color = 0xFF29479F.toInt()
        paint.strokeWidth = MULTI_TOUCH_GESTURE_CONNECTION_LINE_WIDTH

        canvas.drawLines(lines, paint)
        canvas.restoreToCount(saveCount)
    }

    private fun drawMultiTouchGestureCenter(canvas: Canvas, interactions: List<Interaction>) {
        for (interaction in interactions)
            if (interaction is Interaction.Touch.Focusable) {
                val alpha = calcInteractionAlpha(interaction, GESTURE_VISIBILITY_DURATION)

                val centerX = interaction.focusX.toFloat()
                val centerY = interaction.focusY.toFloat()

                val saveCount = canvas.saveLayerAlpha(null, alpha)

                paint.color = Color.WHITE
                paint.setShadowLayer(SHADOW_RADIUS, 0f, 0f, 0x807A8699.toInt())

                canvas.drawCircle(centerX, centerY, MULTI_TOUCH_GESTURE_CENTER_OUTER_RADIUS, paint)

                paint.clearShadowLayer()
                paint.color = 0xff29479F.toInt()

                canvas.drawCircle(centerX, centerY, MULTI_TOUCH_GESTURE_CENTER_INNER_RADIUS, paint)
                canvas.restoreToCount(saveCount)
                break
            }
    }

    private fun drawInteractions(canvas: Canvas, relevantInteractions: List<Interaction>) {
        relevantInteractions.sortedBy { if (it is Interaction.Touch.Pointer) -1 else 0 }

        for (interaction in relevantInteractions)
            when (interaction) {
                is Interaction.Touch.Gesture -> {
                    val pointers = findGesturePointers(interactions, interaction)

                    if (interaction is Interaction.Touch.Continuous && !interaction.isLast)
                        drawGesture(canvas, interaction, pointers)
                    else {
                        val elapsed = getTimestampReference() - interaction.timestamp
                        val fraction = (elapsed / GESTURE_VISIBILITY_DURATION.toFloat()).coerceAtMost(1f)
                        val alpha = 255 - (fraction.pow(8) * 255).toInt()

                        val count = canvas.saveLayerAlpha(null, alpha)
                        drawGesture(canvas, interaction, pointers)
                        canvas.restoreToCount(count)
                    }
                }
                is Interaction.PhoneButton ->
                    drawPhoneButton(canvas, interaction)
                is Interaction.Touch.Pointer ->
                    drawPointer(canvas, interaction)
                is Interaction.Keyboard ->
                    drawKeyboard(canvas, interaction)
                is Interaction.Focus ->
                    drawFocus(canvas, interaction)
                is Interaction.Orientation ->
                    Unit // TODO Draw
            }
    }

    private fun getTimestampReference(): Long {
        return timestampReference ?: System.currentTimeMillis()
    }

    private fun findGesturePointers(interactions: List<Interaction>, gesture: Interaction.Touch.Gesture): List<Interaction.Touch.Pointer> {
        val pointers = ArrayList<Interaction.Touch.Pointer>(gesture.pointerIds.size)

        pointer@ for (pointerId in gesture.pointerIds)
            for (interaction in interactions.reversed())
                if (interaction is Interaction.Touch.Pointer && interaction.pointerId == pointerId && interaction.timestamp <= gesture.timestamp) {
                    pointers += interaction
                    continue@pointer
                }

        if (pointers.size != gesture.pointerIds.size)
            throw IllegalStateException("Missing some gesture pointer")

        return pointers
    }

    private fun calcInteractionAlpha(interaction: Interaction, duration: Long): Int {
        val fraction = ((getTimestampReference() - interaction.timestamp) / duration.toFloat()).coerceAtMost(1f)
        return 255 - (fraction.pow(8) * 255).toInt()
    }

    private fun drawPhoneButton(canvas: Canvas, interaction: Interaction.PhoneButton) {
        val sceneRect = wireframeScene?.rect ?: return // Canvas draw is done in scene space

        val alpha = calcInteractionAlpha(interaction, PHONE_BUTTON_VISIBILITY_DURATION)
        val count = canvas.saveLayerAlpha(null, alpha)

        val drawable = when (interaction.name) {
            Interaction.PhoneButton.Name.BACK -> phoneButtonBackDrawable
            Interaction.PhoneButton.Name.VOLUME_DOWN -> phoneButtonVolumeDownDrawable
            Interaction.PhoneButton.Name.VOLUME_UP -> phoneButtonVolumeUpDrawable
        }

        val centerX = sceneRect.width() / 2f
        val centerY = sceneRect.height() / 2f
        val radius = max(drawable.intrinsicWidth, drawable.intrinsicHeight).toFloat()

        drawPointerBackground(canvas, centerX, centerY, radius, PointerState.ACTIVE)

        canvas.translate(centerX - drawable.intrinsicWidth / 2f, centerY - drawable.intrinsicHeight / 2f)
        drawable.draw(canvas)

        canvas.restoreToCount(count)
    }

    private fun drawKeyboard(canvas: Canvas, interaction: Interaction.Keyboard) {
        val rect = interaction.rect ?: return

        keyboardDrawable.bounds = rect
        keyboardDrawable.draw(canvas)
    }

    private fun drawGesture(canvas: Canvas, interaction: Interaction.Touch.Gesture, pointers: List<Interaction.Touch.Pointer>) {
        val pointer = pointers.first()

        val icons = when (pointer.type) {
            Interaction.Touch.Pointer.Type.FINGER -> touchFingerIcons
            Interaction.Touch.Pointer.Type.MOUSE -> touchMouseIcons
            Interaction.Touch.Pointer.Type.STYLUS -> touchStylusIcons
            Interaction.Touch.Pointer.Type.ERASER -> touchStylusIcons // FIXME Icon
            Interaction.Touch.Pointer.Type.UNKNOWN -> touchUnknownIcons
        }

        val drawable = when (interaction) {
            is Interaction.Touch.Gesture.Tap -> icons.tapDrawable
            is Interaction.Touch.Gesture.DoubleTap -> icons.doubleTapDrawable
            is Interaction.Touch.Gesture.RageTap -> icons.rageTapDrawable
            is Interaction.Touch.Gesture.LongPress -> icons.longPressDrawable
            else -> return
        }

        drawPointer(canvas, pointer, PointerState.ACTIVE, drawable)
    }

    private fun drawPointer(canvas: Canvas, interaction: Interaction.Touch.Pointer) {
        val alpha = if (interaction.isLast) calcInteractionAlpha(interaction, POINTER_VISIBILITY_DURATION) else 255
        val saveCount = canvas.saveLayerAlpha(null, alpha)

        val centerX = interaction.x.toFloat()
        val centerY = interaction.y.toFloat()

        val pointerState = if (interaction.isHovering) PointerState.HOVERING else PointerState.INACTIVE
        drawPointerBackground(canvas, centerX, centerY, POINTER_RADIUS, pointerState)

        val icons = when (interaction.type) {
            Interaction.Touch.Pointer.Type.FINGER -> touchFingerIcons
            Interaction.Touch.Pointer.Type.MOUSE -> touchMouseIcons
            Interaction.Touch.Pointer.Type.STYLUS -> touchStylusIcons
            Interaction.Touch.Pointer.Type.ERASER -> touchStylusIcons // FIXME Icon
            Interaction.Touch.Pointer.Type.UNKNOWN -> touchUnknownIcons
        }

        val drawable = icons.pointerDrawable

        canvas.translate(centerX - drawable.intrinsicWidth / 2f, centerY - drawable.intrinsicHeight / 2f)
        drawable.draw(canvas)

        canvas.restoreToCount(saveCount)
    }

    private fun drawPointer(canvas: Canvas, pointer: Interaction.Touch.Pointer, state: PointerState, drawable: Drawable) {
        val centerX = pointer.x.toFloat()
        val centerY = pointer.y.toFloat()

        drawPointerBackground(canvas, centerX, centerY, POINTER_SIZE_HALF, state)

        val saveCount = canvas.save()
        canvas.translate(centerX - drawable.intrinsicWidth / 2f, centerY - drawable.intrinsicHeight / 2f)
        drawable.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    private fun drawPointerBackground(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, state: PointerState) {
        path.reset()
        path.addCircle(centerX, centerY, radius, Path.Direction.CW)

        paint.style = Paint.Style.FILL
        paint.color = 0xb1ffffff.toInt()
        paint.setShadowLayer(SHADOW_RADIUS, 0f, 0f, 0x807A8699.toInt())

        val saveCount = canvas.save()

        canvas.clipOutPathCompat(path)
        canvas.drawCircle(centerX, centerY, radius, paint)
        canvas.restoreToCount(saveCount)

        paint.clearShadowLayer()
        canvas.drawCircle(centerX, centerY, radius, paint)

        val borderColor: Int
        val ringRadius: Float
        val strokeWidth: Float

        when (state) {
            PointerState.ACTIVE -> {
                borderColor = 0xff29479F.toInt()
                ringRadius = radius
                strokeWidth = dpToPxF(2f)
            }
            PointerState.HOVERING -> {
                borderColor = 0xff888888.toInt()
                ringRadius = radius
                strokeWidth = dpToPxF(2f)
            }
            PointerState.INACTIVE -> {
                borderColor = 0xb17A8699.toInt()
                ringRadius = radius
                strokeWidth = dpToPxF(2f)
            }
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = borderColor

        canvas.drawCircle(centerX, centerY, ringRadius, paint)
    }

    // FIXME View bounds are not clipped
    private fun drawFocus(canvas: Canvas, interaction: Interaction.Focus) {
        val targetElementPath = interaction.targetElementPath ?: return
        val wireframeScene = wireframeScene ?: return

        for (window in wireframeScene.windows) {
            val view = targetElementPath.lastOrNull()?.view ?: continue

            paint.color = 0xff854ddc.toInt()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = TARGET_ELEMENT_STROKE

            canvas.drawRect(view.rect, paint)
            break
        }
    }

    private val frameCallback = Choreographer.FrameCallback {
        invalidate()
    }

    private enum class PointerState {
        ACTIVE, HOVERING, INACTIVE
    }

    private class TouchIcons(
        val pointerDrawable: Drawable,
        val tapDrawable: Drawable,
        val doubleTapDrawable: Drawable,
        val longPressDrawable: Drawable,
        val rageTapDrawable: Drawable
    ) {

        init {
            for (drawable in arrayOf(pointerDrawable, tapDrawable, doubleTapDrawable, longPressDrawable, rageTapDrawable))
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        }
    }

    private companion object {

        const val POINTER_VISIBILITY_DURATION = 750L
        const val PHONE_BUTTON_VISIBILITY_DURATION = 1000L
        const val GESTURE_VISIBILITY_DURATION = 750L

        val TARGET_ELEMENT_INNER_STROKE = dpToPxF(6f)
        val TARGET_ELEMENT_INNER_STROKE_HALF = TARGET_ELEMENT_INNER_STROKE / 2f

        val TARGET_ELEMENT_STROKE = dpToPxF(2f)
        val TARGET_ELEMENT_STROKE_HALF = TARGET_ELEMENT_STROKE / 2f

        val TARGET_ELEMENT_RECT_SIZE = dpToPxF(16f)

        val MULTI_TOUCH_GESTURE_CENTER_OUTER_RADIUS = dpToPxF(10f)
        val MULTI_TOUCH_GESTURE_CENTER_INNER_RADIUS = dpToPxF(6f)

        val MULTI_TOUCH_GESTURE_CONNECTION_LINE_WIDTH = dpToPxF(2f)

        val SHADOW_RADIUS = dpToPxF(10f)

        val POINTER_RADIUS = dpToPxF(16f)

        val POINTER_SIZE_HALF = dpToPxF(16f)
    }
}
