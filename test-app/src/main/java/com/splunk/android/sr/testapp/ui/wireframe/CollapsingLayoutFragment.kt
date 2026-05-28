package com.splunk.android.sr.testapp.ui.wireframe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.databinding.FragmentCollapsingLayoutBinding
import com.splunk.android.sr.testapp.ui.adapter.SensitiveItemAdapter
import com.splunk.android.sr.testapp.ui.BaseFragment

class CollapsingLayoutFragment : BaseFragment<FragmentCollapsingLayoutBinding>() {

    override val titleRes: Int = R.string.wireframe_title
    override val subtitleRes: Int = R.string.wireframe_collapsing_layout_subtitle

    override val viewBindingCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCollapsingLayoutBinding
        get() = FragmentCollapsingLayoutBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.recycler.adapter = SensitiveItemAdapter(requireContext(), 20)
    }
}
