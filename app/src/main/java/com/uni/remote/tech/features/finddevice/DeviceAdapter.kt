package com.uni.remote.tech.features.finddevice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.connectsdk.device.ConnectableDevice
import com.uni.remote.tech.databinding.ItemDeviceBinding

class DeviceAdapter(private val listener: (ConnectableDevice) -> Unit) :
    ListAdapter<ConnectableDevice, DeviceAdapter.VH>(DeviceDiffCallback()) {
    inner class VH(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConnectableDevice) {
            binding.tvDeviceName.text = item.friendlyName
            binding.tvDeviceIp.text = item.ipAddress
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.invoke(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<ConnectableDevice>() {
        override fun areItemsTheSame(
            oldItem: ConnectableDevice,
            newItem: ConnectableDevice
        ): Boolean {
            return oldItem.ipAddress == newItem.ipAddress
        }

        override fun areContentsTheSame(
            oldItem: ConnectableDevice,
            newItem: ConnectableDevice
        ): Boolean {
            return oldItem == newItem
        }
    }
}
