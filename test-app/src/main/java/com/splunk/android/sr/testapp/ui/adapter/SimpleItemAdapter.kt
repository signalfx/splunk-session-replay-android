package com.splunk.android.sr.testapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.splunk.android.sr.testapp.databinding.ItemExampleBinding
import com.splunk.android.sr.testapp.ui.wireframe.model.SimpleItem

class SimpleItemAdapter(
    context: Context,
    private val list: List<SimpleItem>
) : RecyclerView.Adapter<SimpleItemAdapter.SimpleItemViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleItemViewHolder {
        val binding = ItemExampleBinding.inflate(inflater, parent, false)
        return SimpleItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimpleItemViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class SimpleItemViewHolder(
        private val binding: ItemExampleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SimpleItem) {
            binding.title.text = item.title
            binding.description.text = item.description
            binding.root.setBackgroundColor(item.backgroundColor)
        }
    }
}
