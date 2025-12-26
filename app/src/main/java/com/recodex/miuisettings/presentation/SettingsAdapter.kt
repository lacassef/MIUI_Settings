package com.recodex.miuisettings.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.recodex.miuisettings.R
import com.recodex.miuisettings.presentation.model.SettingSummary

class SettingsAdapter(
    private val onItemClick: (SettingSummary) -> Unit
) : ListAdapter<SettingSummary, SettingsAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(itemView: View, val onItemClick: (SettingSummary) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        private val badgeLegacy: TextView = itemView.findViewById(R.id.chip_legacy)
        private var currentItem: SettingSummary? = null

        init {
            itemView.setOnClickListener {
                currentItem?.let { onItemClick(it) }
            }
        }

        fun bind(item: SettingSummary) {
            currentItem = item
            tvTitle.text = item.title
            tvCategory.text = item.category
            
            badgeLegacy.visibility = if (item.isLegacyOnly) View.VISIBLE else View.GONE
            
            val iconRes = when (item.category) {
                "Rede" -> R.drawable.ic_network
                "Tela" -> R.drawable.ic_display
                "Bateria" -> R.drawable.ic_battery
                "Seguranca" -> R.drawable.ic_security
                "Sistema" -> R.drawable.ic_system
                "Desenvolvedor" -> R.drawable.ic_developer
                else -> R.drawable.ic_others
            }
            itemView.findViewById<android.widget.ImageView>(R.id.iv_icon).setImageResource(iconRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setting, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SettingSummary>() {
        override fun areItemsTheSame(oldItem: SettingSummary, newItem: SettingSummary): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SettingSummary, newItem: SettingSummary): Boolean {
            return oldItem == newItem
        }
    }
}
