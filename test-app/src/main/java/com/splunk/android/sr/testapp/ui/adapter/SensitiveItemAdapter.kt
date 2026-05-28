package com.splunk.android.sr.testapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.splunk.android.instrumentation.recording.core.api.isSensitive
import com.splunk.android.sr.testapp.databinding.ItemSensitiveBinding

class SensitiveItemAdapter(
    context: Context,
    private val count: Int
) : RecyclerView.Adapter<SensitiveItemAdapter.ItemViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    override fun getItemCount(): Int {
        return count
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemSensitiveBinding.inflate(inflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.binding.a.isSensitive = true
        holder.binding.b.isSensitive = true
        holder.binding.c.isSensitive = true

        val context = holder.itemView.context
        holder.binding.a.setOnClickListener { Toast.makeText(context, "A", Toast.LENGTH_SHORT).show() }
        holder.binding.b.setOnClickListener { Toast.makeText(context, "B", Toast.LENGTH_SHORT).show() }
        holder.binding.c.setOnClickListener { Toast.makeText(context, "C", Toast.LENGTH_SHORT).show() }
    }

    class ItemViewHolder(val binding: ItemSensitiveBinding) : RecyclerView.ViewHolder(binding.root)
}
