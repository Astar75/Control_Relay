package com.astar.osterrig.controlrelay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private val diff = DevicesDiffUtil()
    private val items = ArrayList<Device>()

    fun setItems(newItems: List<Device>) {
        diff.setItems(items, newItems)
        val result = DiffUtil.calculateDiff(diff)
        items.clear()
        items.addAll(newItems)
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DeviceViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.device_layout, parent, false)
    )

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun onBindViewHolder(
        holder: DeviceViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val payload = payloads[0]
            if (payload is Bundle && payload.containsKey(DevicesDiffUtil.KEY_RSSI)) {
                val rssi = payload.getInt(DevicesDiffUtil.KEY_RSSI)
                holder.updateRssi(rssi)
            }
        }
    }

    override fun getItemCount() = items.size

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textName: TextView = view.findViewById(R.id.textName)
        private val textAddress: TextView = view.findViewById(R.id.textAddress)
        private val textSignal: TextView = view.findViewById(R.id.textSignal)

        fun bind(item: Device) {
            textName.text = item.device.name ?: "Unnamed"
            textAddress.text = item.device.address
            textSignal.text = item.rssi.toString()
        }

        fun updateRssi(rssi: Int) {
            textSignal.text = rssi.toString()
        }
    }

}