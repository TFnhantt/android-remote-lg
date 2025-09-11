package com.uni.remote.tech.features.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.connectsdk.core.AppInfo
import com.uni.remote.tech.databinding.AppItemBinding

class AppsAdapter(
    private val context: Context,
    private val itemClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppsAdapter.AppsViewHolder>(AppInfoItemCallBack) {
    private object AppInfoItemCallBack : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }

    }

    inner class AppsViewHolder(
        private val binding: AppItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppInfo) {
            binding.root.setOnClickListener {
                itemClick(item)
            }
            binding.txtAppName.text = item.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsViewHolder {
        return AppsViewHolder(
            AppItemBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: AppsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}