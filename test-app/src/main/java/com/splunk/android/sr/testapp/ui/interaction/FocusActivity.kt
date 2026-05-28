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

package com.splunk.android.sr.testapp.ui.interaction

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import com.splunk.android.common.utils.dpToPx
import java.util.UUID

class FocusActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setOnClickListener(onClickListener)

        val field = EditText(this)
        field.setBackgroundColor(0xffadd8e6.toInt())
        field.setText(UUID.randomUUID().toString())

        val fieldLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(50f))
        fieldLayoutParams.topMargin = (Math.random() * dpToPx(400f)).toInt()
        fieldLayoutParams.gravity = Gravity.CENTER_HORIZONTAL

        layout.addView(field, fieldLayoutParams)

        val numberPicker = NumberPicker(this)
        numberPicker.setBackgroundColor(0xffff7f7f.toInt())

        val numberPickerLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        numberPickerLayoutParams.topMargin = dpToPx(15f)
        numberPickerLayoutParams.gravity = Gravity.CENTER_HORIZONTAL

        layout.addView(numberPicker, numberPickerLayoutParams)

        setContentView(layout)
    }

    private val onClickListener = View.OnClickListener {
        startActivity(Intent(this, FocusActivity::class.java))
    }
}
