package com.splunk.android.sr.testapp.ui.wireframe

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.splunk.android.common.utils.extensions.contentView
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.ui.dialog.BottomSheetDialogFragment

class EmptyActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        contentView?.setOnClickListener { onBackPressed() }

        BottomSheetDialogFragment().show(supportFragmentManager, "BottomSheetDialogFragment")
    }
}
