package com.splunk.android.sr.testapp.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.FrameLayout
import com.splunk.android.common.utils.dpToPxF
import com.splunk.android.common.utils.extensions.windowManager
import com.splunk.android.sr.testapp.databinding.FragmentDialogBinding

object WindowManagerDialog {

    fun show(context: Context) {
        val windowManager = context.windowManager

        val inflater = LayoutInflater.from(context)
        val viewBinding = FragmentDialogBinding.inflate(inflater, null, false)

        val background = GradientDrawable()
        background.setColor(Color.WHITE)
        background.cornerRadius = dpToPxF(15f)

        val rootView = FrameLayout(context)
        rootView.background = background
        rootView.elevation = dpToPxF(20f)
        rootView.addView(viewBinding.root)

        viewBinding.button.setOnClickListener { windowManager.removeView(rootView) }

        val type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
        val flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        val layoutParams = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type, flags, PixelFormat.TRANSPARENT)
        layoutParams.gravity = Gravity.CENTER

        windowManager.addView(rootView, layoutParams)
    }
}
