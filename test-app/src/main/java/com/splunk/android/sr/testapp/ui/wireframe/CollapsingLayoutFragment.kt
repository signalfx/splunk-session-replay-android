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
