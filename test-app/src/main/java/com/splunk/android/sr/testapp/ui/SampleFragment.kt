package com.splunk.android.sr.testapp.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.splunk.android.sr.testapp.databinding.ItemExampleBinding

class SampleFragment : Fragment() {

    private lateinit var viewBinding: ItemExampleBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = ItemExampleBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.title.text = arguments?.getString("title")
        viewBinding.description.text = arguments?.getString("description")
        viewBinding.root.setBackgroundColor(arguments?.getInt("color") ?: Color.TRANSPARENT)
    }

    companion object {

        fun create(title: String, description: String, color: Int): SampleFragment {
            val fragment = SampleFragment()
            fragment.arguments = bundleOf(
                "title" to title,
                "description" to description,
                "color" to color
            )

            return fragment
        }
    }
}
