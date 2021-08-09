package com.astar.osterrig.controlrelay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import eo.view.signalstrength.SignalStrengthView

class DeviceAdapter(
    private val action: Action
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

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

    interface Action {
        fun onControl(device: Device)
    }

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textName: TextView = view.findViewById(R.id.textName)
        private val textAddress: TextView = view.findViewById(R.id.textAddress)
        private val textSignal: TextView = view.findViewById(R.id.textSignal)
        private val viewSignal: SignalStrengthView = view.findViewById(R.id.signalView)

        fun bind(item: Device) {
            itemView.setOnClickListener { action.onControl(item) }
            textName.text = item.device.name ?: itemView.context.getString(R.string.unnamed)
            textAddress.text = item.device.address
            textSignal.text = textSignal.context.getString(R.string.signal, item.rssi)
            viewSignal.signalLevel = item.rssi + 100
        }

        fun updateRssi(rssi: Int) {
            textSignal.text = textSignal.context.getString(R.string.signal, rssi)
            viewSignal.signalLevel = rssi + 100
        }
    }
}