package com.splunk.android.sr.testapp.ui.screenshot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.splunk.android.instrumentation.recording.core.api.isSensitive
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.databinding.FragmentTransactionsBinding
import com.splunk.android.sr.testapp.ui.BaseFragment

class AnimationFragment : BaseFragment<FragmentTransactionsBinding>() {

    override val viewBindingCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTransactionsBinding
        get() = FragmentTransactionsBinding::inflate

    override val titleRes: Int = R.string.screenshot_fragment_animation_title

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.sensitiveField.isSensitive = true
    }
}
