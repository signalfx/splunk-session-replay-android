package com.splunk.android.sr.testapp.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.splunk.android.sr.testapp.databinding.FragmentDialogBinding

object AlertDialog {

    fun show(context: Context, isDimEnabled: Boolean = true) {
        val inflater = LayoutInflater.from(context)
        val viewBinding = FragmentDialogBinding.inflate(inflater, null, false)

        val dialog = AlertDialog.Builder(context)
            .setView(viewBinding.root)
            .show()

        viewBinding.button.setOnClickListener { dialog.dismiss() }

        if (!isDimEnabled)
            dialog.window?.setDimAmount(0f)
    }
}
