package com.example.miniproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RegexAdapter(
    private val onDeleteClick: (Regexdatabase) -> Unit
) : ListAdapter<Regexdatabase, RegexAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Make sure you created 'item_regex.xml' from the previous step!
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_regex, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSender: TextView = itemView.findViewById(R.id.tvSenderId)
        private val tvPattern: TextView = itemView.findViewById(R.id.tvPattern)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteRegex)

        fun bind(item: Regexdatabase) {
            tvSender.text = "${item.name} (${item.type})"
            tvPattern.text = item.regex
            btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Regexdatabase>() {
        override fun areItemsTheSame(oldItem: Regexdatabase, newItem: Regexdatabase) = oldItem.tid == newItem.tid
        override fun areContentsTheSame(oldItem: Regexdatabase, newItem: Regexdatabase) = oldItem == newItem
    }
}