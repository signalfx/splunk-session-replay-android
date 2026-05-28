package com.splunk.android.sr.testapp.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.splunk.android.sr.testapp.R

class ColoredLinearLayout(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ColoredLinearLayout)
            val count = a.getInt(R.styleable.ColoredLinearLayout_colored_itemsCount, 0)
            val itemWidth = a.getDimension(R.styleable.ColoredLinearLayout_colored_itemWidth, 0f)
            val itemHeight = a.getDimension(R.styleable.ColoredLinearLayout_colored_itemHeight, 0f)
            a.recycle()

            createItems(count, itemWidth.toInt(), itemHeight.toInt())
        }
    }

    private fun createItems(count: Int, width: Int, height: Int) {
        for (i in 0 until count) {
            val view = View(context)
            view.setBackgroundColor(COLORS[i % COLORS.size])
            addView(view, LayoutParams(width, height))
        }
    }

    companion object {
        private val COLORS = intArrayOf(0xffff0000.toInt(), 0xff00ff00.toInt(), 0xff0000ff.toInt())
    }
}
