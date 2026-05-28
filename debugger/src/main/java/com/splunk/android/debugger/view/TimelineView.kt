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

package com.splunk.android.debugger.view

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.TextView
import com.splunk.android.common.utils.dpToPx
import com.splunk.android.common.utils.dpToPxF
import com.splunk.android.common.utils.extensions.doOnLayout
import com.splunk.android.debugger.databinding.SldViewTimelineBinding
import com.splunk.android.debugger.view.seek.ABMark
import com.splunk.android.debugger.view.seek.ProgressMark
import com.splunk.android.instrumentation.recording.screenshot.extension.isInvisibleForScreenshot
import com.splunk.android.instrumentation.recording.wireframe.extension.isInvisibleForWireframe
import kotlin.math.max
import kotlin.math.min

class TimelineView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) { // FIXME Save state

    private val viewBinding = SldViewTimelineBinding.inflate(LayoutInflater.from(context), this, true)

    private var internalMarkA = Int.MIN_VALUE
    private var internalMarkB = Int.MAX_VALUE

    private var positionWindow: PositionWindow? = null

    var listener: Listener? = null

    var progress: Int
        get() = viewBinding.seek.progress
        set(value) {
            viewBinding.seek.progress = value
        }

    var max: Int
        get() = viewBinding.seek.max
        set(value) {
            viewBinding.seek.max = value
        }

    var isSaveButtonEnabled: Boolean
        get() = viewBinding.save.isEnabled
        set(value) {
            viewBinding.save.isEnabled = value
            viewBinding.save.alpha = if (value) 1f else 0.5f
        }

    val markA: Int
        get() = max(internalMarkA, 0)

    val markB: Int
        get() = min(internalMarkB, max)

    init {
        val onClickListener = OnClickListener()
        viewBinding.markA.setOnClickListener(onClickListener)
        viewBinding.markB.setOnClickListener(onClickListener)
        viewBinding.save.setOnClickListener(onClickListener)

        viewBinding.seek.setOnSeekBarChangeListener(OnSeekBarChangeListener())

        updateABMarks()
    }

    private fun markAPoint() {
        internalMarkA = viewBinding.seek.progress
        listener?.onMarkAChanged(this, markA)

        if (internalMarkA > internalMarkB) {
            internalMarkB = Int.MAX_VALUE
            listener?.onMarkBChanged(this, markB)
        }

        updateABMarks()
    }

    private fun markBPoint() {
        internalMarkB = viewBinding.seek.progress
        listener?.onMarkBChanged(this, markB)

        if (internalMarkB < internalMarkA) {
            internalMarkA = Int.MIN_VALUE
            listener?.onMarkAChanged(this, markA)
        }

        updateABMarks()
    }

    private fun updateABMarks() {
        viewBinding.seek.marks.clear()

        if (internalMarkA > 0 || markB < max) {
            viewBinding.seek.marks += ABMark(markA, ABMark.Type.A, MARK_AB_COLOR)
            viewBinding.seek.marks += ABMark(markB, ABMark.Type.B, MARK_AB_COLOR)
            viewBinding.seek.marks += ProgressMark(markA, markB, MARK_AB_COLOR)
        }

        viewBinding.seek.invalidate()
    }

    private inner class OnSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            positionWindow = PositionWindow(context)
            updateWindow()
            positionWindow?.show()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            positionWindow?.hide()
        }

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (!fromUser)
                return

            updateWindow()
            listener?.onProgressChanged(this@TimelineView, progress)
        }

        private fun updateWindow() {
            val buffer = IntArray(2)
            viewBinding.seek.getLocationInWindow(buffer)

            val x = buffer[0] + viewBinding.seek.let { it.paddingStart + (it.progress / it.max.toFloat()) * (it.width - it.paddingStart - it.paddingEnd) }.toInt()
            val y = buffer[1] - POSITION_WINDOW_SHIFT
            viewBinding.seek.progress

            positionWindow?.position = Point(x, y)
            positionWindow?.text = listener?.formatProgress(this@TimelineView, viewBinding.seek.progress)
        }
    }

    private inner class OnClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            when (v.id) {
                viewBinding.markA.id ->
                    markAPoint()
                viewBinding.markB.id ->
                    markBPoint()
                viewBinding.save.id ->
                    listener?.onSaveClicked(this@TimelineView)
            }
        }
    }

    interface Listener {

        fun onProgressChanged(view: TimelineView, progress: Int) {}
        fun onMarkAChanged(view: TimelineView, value: Int) {}
        fun onMarkBChanged(view: TimelineView, value: Int) {}
        fun onSaveClicked(view: TimelineView) {}

        fun formatProgress(view: TimelineView, progress: Int): String {
            return progress.toString()
        }
    }

    private class PositionWindow(context: Context) {

        private val window = PopupWindow()
        private val textView = TextView(context)

        var text: String?
            get() = textView.text?.toString()
            set(value) {
                textView.text = value
                updatePosition()
            }

        var position = Point(0, 0)
            set(value) {
                field = value
                updatePosition()
            }

        fun show() {
            val background = GradientDrawable()
            background.cornerRadius = dpToPxF(50f)
            background.setColor(0xff333333.toInt())

            textView.setPadding(dpToPx(12f), dpToPx(7f), dpToPx(12f), dpToPx(7f))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            textView.gravity = Gravity.CENTER_HORIZONTAL
            textView.setTextColor(Color.WHITE)
            textView.background = background

            window.width = WindowManager.LayoutParams.WRAP_CONTENT
            window.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.elevation = dpToPxF(10f)
            window.contentView = textView

            window.showAtLocation(textView, Gravity.START or Gravity.TOP, position.x, position.y)
            textView.doOnLayout { updatePosition() }

            val rootView = window.contentView.parent as? ViewGroup ?: return
            rootView.isInvisibleForScreenshot = true
            rootView.isInvisibleForWireframe = true
        }

        fun hide() {
            window.dismiss()
        }

        private fun updatePosition() {
            if (window.isShowing) {
                val x = position.x - textView.width / 2
                window.update(x, position.y, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
            }
        }
    }

    companion object {
        private const val MARK_AB_COLOR = 0xffe84043.toInt()
        private val POSITION_WINDOW_SHIFT = dpToPx(40f)
    }
}
