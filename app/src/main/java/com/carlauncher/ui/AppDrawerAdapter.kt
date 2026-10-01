package com.carlauncher.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carlauncher.data.AppEntry
import com.carlauncher.databinding.ItemAppBinding

class AppDrawerAdapter(
    private val onItemClick: (AppEntry) -> Unit,
    private val onItemLongClick: (AppEntry) -> Unit,
) : RecyclerView.Adapter<AppDrawerAdapter.Holder>() {

    private var all: List<AppEntry> = emptyList()
    private var filtered: List<AppEntry> = emptyList()

    fun submit(list: List<AppEntry>) {
        all = list
        filtered = list
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filtered = if (query.isBlank()) {
            all
        } else {
            all.filter { it.label.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    class Holder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int = filtered.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val entry = filtered[position]
        holder.binding.appIcon.setImageDrawable(entry.icon)
        holder.binding.appLabel.text = entry.label
        holder.itemView.setOnClickListener { onItemClick(entry) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(entry)
            true
        }
    }
}
