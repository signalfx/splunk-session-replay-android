package com.splunk.android.debugger.drawer

import android.graphics.Canvas
import android.graphics.Paint
import com.splunk.android.instrumentation.recording.interactions.model.Interaction

internal object KeyboardDrawer {

    private const val BACKGROUND_COLOR = 0xffdbdee0.toInt()

    private val paint = Paint()

    fun draw(canvas: Canvas, frameWidth: Int, frameHeight: Int, outputWidth: Int, outputHeight: Int, interactions: List<Interaction>) {
        var keyboard: Interaction.Keyboard? = null

        for (interaction in interactions.asReversed()) {
            keyboard = interaction as? Interaction.Keyboard ?: continue

            if (keyboard.rect == null)
                return
            else
                break
        }

        val rect = keyboard?.rect ?: return

        val left = rect.left / frameWidth.toFloat() * outputWidth
        val top = rect.top / frameHeight.toFloat() * outputHeight
        val right = rect.right / frameWidth.toFloat() * outputWidth
        val bottom = rect.bottom / frameHeight.toFloat() * outputHeight

        paint.color = BACKGROUND_COLOR
        paint.style = Paint.Style.FILL
        canvas.drawRect(left, top, right, bottom, paint)
    }
}
