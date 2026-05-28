package com.splunk.android.debugger.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.splunk.android.common.utils.dpToPx
import com.splunk.android.common.utils.dpToPxF
import com.splunk.android.instrumentation.recording.screenshot.extension.isInvisibleForScreenshot
import com.splunk.android.instrumentation.recording.wireframe.extension.isInvisibleForWireframe

/**
 * Property [View.isInvisibleForWireframe] is impossible to set on [Toast].
 */
internal object DebugToast {

    private const val backgroundColor = 0xff333333.toInt()
    private val padding = dpToPx(15f)
    private val cornerRadius = dpToPxF(20f)
    private val elevation = dpToPxF(10f)
    private val bottomShift = dpToPx(30f)

    private val handler = Handler(Looper.getMainLooper())

    fun showText(context: Context, text: CharSequence) {
        val background = GradientDrawable()
        background.setColor(backgroundColor)
        background.cornerRadius = cornerRadius

        val view = TextView(context)
        view.setPadding(padding, padding, padding, padding)
        view.gravity = Gravity.CENTER_HORIZONTAL
        view.isInvisibleForScreenshot = true
        view.isInvisibleForWireframe = true
        view.setTextColor(Color.WHITE)
        view.background = background
        view.text = text

        val window = PopupWindow()
        window.width = WindowManager.LayoutParams.WRAP_CONTENT
        window.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.animationStyle = android.R.style.Animation_Toast
        window.elevation = elevation
        window.contentView = view
        window.isFocusable = false
        window.isTouchable = false

        window.showAtLocation(view, Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, bottomShift)

        handler.postDelayed({ window.dismiss() }, 2000L)
    }
}
