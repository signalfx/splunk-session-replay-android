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

package com.splunk.android.sr.testapp.ui.systembars

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.databinding.ActivitySystemBarsBinding
import com.splunk.android.sr.testapp.ui.dialog.DialogActivity
import com.splunk.rum.common.utils.dpToPx

class SystemBarsActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivitySystemBarsBinding

    private val case: SystemBarsCase
        get() = intent.getStringExtra(EXTRA_CASE)?.let { SystemBarsCase.valueOf(it) } ?: SystemBarsCase.EDGE_TO_EDGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySystemBarsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setupWindow()
        setupPanel()

        if (savedInstanceState == null)
            viewBinding.root.post { showDialog() }
    }

    private fun setupWindow() {
        when (case) {
            SystemBarsCase.OPAQUE -> {
                window.statusBarColor = BAR_COLOR
                window.navigationBarColor = BAR_COLOR
            }
            SystemBarsCase.TRANSLUCENT -> {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                @Suppress("DEPRECATION")
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            }
            else -> {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.TRANSPARENT
            }
        }

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        when (case) {
            SystemBarsCase.HIDDEN_STATUS_BAR -> controller.hide(WindowInsetsCompat.Type.statusBars())
            SystemBarsCase.HIDDEN_NAVIGATION_BAR -> controller.hide(WindowInsetsCompat.Type.navigationBars())
            SystemBarsCase.HIDDEN_SYSTEM_BARS -> controller.hide(WindowInsetsCompat.Type.systemBars())
            SystemBarsCase.LANDSCAPE -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            SystemBarsCase.KEYBOARD -> {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                viewBinding.input.requestFocus()
            }
            else -> {}
        }
    }

    private fun setupPanel() {
        viewBinding.title.text = case.title

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime())
            val margin = dpToPx(PANEL_MARGIN_DP)
            val layoutParams = viewBinding.panel.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(bars.left + margin, bars.top + margin, bars.right + margin, bars.bottom + margin)
            viewBinding.panel.layoutParams = layoutParams
            insets
        }

        for (case in SystemBarsCase.values()) {
            val button = TextView(this, null, 0, R.style.Button)
            button.text = case.title
            button.setOnClickListener { open(case) }

            val layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.topMargin = dpToPx(5f)
            viewBinding.cases.addView(button, layoutParams)
        }
    }

    private fun showDialog() {
        when (case) {
            SystemBarsCase.DIALOG ->
                AlertDialog.Builder(this)
                    .setTitle(case.title)
                    .setMessage(R.string.system_bars_dialog_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            SystemBarsCase.BOTTOM_SHEET -> {
                val content = TextView(this)
                content.setText(R.string.system_bars_dialog_message)
                content.setPadding(dpToPx(20f), dpToPx(40f), dpToPx(20f), dpToPx(40f))

                val dialog = BottomSheetDialog(this)
                dialog.setContentView(content)
                dialog.show()
            }
            else -> {}
        }
    }

    private fun open(case: SystemBarsCase) {
        if (case == SystemBarsCase.DIALOG_ACTIVITY) {
            startActivity(Intent(this, DialogActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK))
            moveTaskToBack(true)
            return
        }

        startActivity(createIntent(this, case))
        finish()
    }

    companion object {
        private const val EXTRA_CASE = "case"
        private const val BAR_COLOR = 0xff143188.toInt()
        private const val PANEL_MARGIN_DP = 24f

        fun createIntent(context: Context, case: SystemBarsCase): Intent {
            return Intent(context, SystemBarsActivity::class.java).putExtra(EXTRA_CASE, case.name)
        }
    }
}
