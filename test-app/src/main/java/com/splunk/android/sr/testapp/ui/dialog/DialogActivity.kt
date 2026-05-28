package com.splunk.android.sr.testapp.ui.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.splunk.android.common.utils.extensions.contentView
import com.splunk.android.instrumentation.recording.core.api.isSensitive
import com.splunk.android.sr.testapp.databinding.FragmentDialogBinding

class DialogActivity : FragmentActivity() {

    private lateinit var viewBinding: FragmentDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = FragmentDialogBinding.inflate(layoutInflater, contentView, true)
        viewBinding.button.setOnClickListener { finishAfterTransition() }
        viewBinding.sampleEditText.isSensitive = true
    }
}
