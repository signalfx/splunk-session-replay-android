package com.splunk.android.sr.testapp.ui.wireframe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.databinding.FragmentWireframeListBinding
import com.splunk.android.sr.testapp.ui.BaseFragment

class ListFragment : BaseFragment<FragmentWireframeListBinding>() {

    override val titleRes: Int = R.string.list_title
    override val subtitleRes: Int? = null

    override val viewBindingCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWireframeListBinding
        get() = FragmentWireframeListBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = (0..100).map { "Title $it" }
        viewBinding.list.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        viewBinding.list.onItemClickListener = onItemClickListener
    }

    private val onItemClickListener = OnItemClickListener { _, _, position, _ ->
        Toast.makeText(requireContext(), "$position clicked", Toast.LENGTH_SHORT).show()
    }
}
