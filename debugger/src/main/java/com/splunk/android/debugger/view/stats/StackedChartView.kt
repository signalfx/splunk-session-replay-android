package com.splunk.android.debugger.view.stats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.splunk.android.common.utils.dpToPxF
import com.splunk.android.debugger.extension.removeLast
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal abstract class StackedChartView<T>(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint()

    private val items = LinkedList<T>()

    private var targetChangedTimestamp = 0L
    private var targetMaxTime = -1f
    private var actualMaxTime = 0f

    override fun onDraw(canvas: Canvas) {
        if (items.isEmpty())
            return

        updateActualTotalTime()
        drawChart(canvas)
    }

    fun addItem(item: T) {
        items.add(0, item)

        if (width > 0) {
            val maxSize = (width - paddingLeft - paddingRight) / ITEM_WIDTH.toInt()
            val overflowSize = items.size - maxSize

            if (overflowSize > 0)
                items.removeLast(overflowSize)

            invalidate()
        }
    }

    fun getItems(): List<T> {
        return items
    }

    fun setItems(list: List<T>) {
        items.clear()
        items += list

        invalidate()
    }

    private fun updateActualTotalTime() {
        if (items.isEmpty())
            return

        val totalMax = items.maxOf { getTotalValue(it) }

        if (totalMax != actualMaxTime) {
            if (targetMaxTime == -1f)
                actualMaxTime = totalMax
            else
                postInvalidate()

            if (totalMax != targetMaxTime) {
                targetChangedTimestamp = System.currentTimeMillis()
                targetMaxTime = totalMax
            }
        }

        if (actualMaxTime - targetMaxTime != 0f) {
            val diff = abs(targetMaxTime - actualMaxTime)
            val speed = ((System.currentTimeMillis() - targetChangedTimestamp) / ANIMATION_DURATION) * diff

            actualMaxTime = if (targetMaxTime > actualMaxTime)
                min(actualMaxTime + speed, targetMaxTime)
            else
                max(actualMaxTime - speed, targetMaxTime)
        }
    }

    private fun drawChart(canvas: Canvas) {
        val contentWidth = (width - paddingLeft - paddingRight).toFloat()
        val contentHeight = (height - paddingTop - paddingBottom).toFloat()

        for (i in items.indices) {
            val item = items[i]
            val barTotalHeight = contentHeight * getTotalValue(item) / actualMaxTime

            val left = paddingLeft + contentWidth - ITEM_WIDTH * (i + 1)
            val right = left + ITEM_WIDTH

            var bottom = paddingTop + contentHeight
            var top = bottom

            val drawer: (fraction: Float, color: Int) -> Unit = { fraction, color ->
                val barHeight = fraction * barTotalHeight
                bottom = top
                top = bottom - barHeight

                paint.color = color
                canvas.drawRect(left, top, right, bottom, paint)
            }

            draw(item, drawer)
        }
    }

    protected abstract fun getTotalValue(item: T): Float

    protected abstract fun draw(item: T, drawer: (fraction: Float, color: Int) -> Unit)

    protected fun getColor(colorRes: Int): Int {
        return ResourcesCompat.getColor(resources, colorRes, null)
    }

    companion object {

        private const val ANIMATION_DURATION = 500f

        private val ITEM_WIDTH = dpToPxF(2f)
    }
}
