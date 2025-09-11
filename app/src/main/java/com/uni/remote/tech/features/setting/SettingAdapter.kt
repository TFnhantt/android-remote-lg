package com.uni.remote.tech.features.setting

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uni.remote.tech.databinding.ItemSettingBinding
import com.uni.remote.tech.utils.AppPref

class SettingAdapter(
    private val context: Context,
    private val onToggleChange: ((SettingItem, Boolean) -> Unit)? = null,
    private val onItemClick: ((SettingItem) -> Unit)? = null
) : ListAdapter<SettingItem, SettingAdapter.SettingViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SettingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SettingViewHolder(private val binding: ItemSettingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: SettingItem
        ) = with(binding) {
            tvItem.text = context.getString(item.title)
            tvItem.setCompoundDrawablesWithIntrinsicBounds(item.iconResId, 0, 0, 0)

            if (item.isToggle) {
                swItem.isChecked = AppPref.vibrationEnabled
                swItem.visibility = View.VISIBLE
                swItem.setOnCheckedChangeListener { _, isChecked ->
                    onToggleChange?.invoke(item, isChecked)
                }
                root.setOnClickListener(null)
            } else {
                swItem.visibility = View.GONE
                root.setOnClickListener { onItemClick?.invoke(item) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SettingItem>() {
            override fun areItemsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean =
                oldItem.title == newItem.title

            override fun areContentsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean =
                oldItem == newItem
        }
    }
}
