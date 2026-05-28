package com.splunk.android.sr.testapp.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.splunk.android.instrumentation.recording.core.api.isSensitive
import com.splunk.android.sr.testapp.databinding.ItemExampleBinding
import com.splunk.android.sr.testapp.ui.wireframe.model.SimpleItem

class SpinnerAdapter(context: Context, items: List<SimpleItem>) : ArrayAdapter<SimpleItem>(context, 0, items) {

    private val inflater = LayoutInflater.from(context)

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = ItemExampleBinding.inflate(inflater, parent, false)
        val item = getItem(position) ?: error("Null item")

        binding.title.text = item.title
        binding.description.isSensitive = true
        binding.description.text = item.description
        binding.root.setBackgroundColor(item.backgroundColor)

        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View = getView(position, convertView, parent)
}
