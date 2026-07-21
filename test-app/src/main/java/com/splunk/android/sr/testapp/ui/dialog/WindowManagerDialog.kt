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

package com.splunk.android.sr.testapp.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.FrameLayout
import com.splunk.rum.common.utils.dpToPxF
import com.splunk.rum.common.utils.extensions.windowManager
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
