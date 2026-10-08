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

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import com.splunk.android.sr.testapp.R

object ActivityLaunchingDialog {

    fun show(context: Context) {
        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.activity_launching_dialog_title)
            .setMessage(R.string.activity_launching_dialog_message)
            .setPositiveButton(R.string.activity_launching_dialog_open, null)
            .setNegativeButton(R.string.activity_launching_dialog_close, null)
            .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            context.startActivity(Intent(context, PlainActivity::class.java))
        }
    }
}
