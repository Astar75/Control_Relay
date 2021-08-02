package com.astar.osterrig.controlrelay

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil

class DevicesDiffUtil : DiffUtil.Callback() {

    private val oldDevices = ArrayList<Device>()
    private val newDevices = ArrayList<Device>()

    fun setItems(oldItems: List<Device>, newItems: List<Device>) {
        oldDevices.clear()
        oldDevices.addAll(oldItems)
        newDevices.clear()
        newDevices.addAll(newItems)
    }

    override fun getOldListSize() = oldDevices.size

    override fun getNewListSize() = newDevices.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldDevice = oldDevices[oldItemPosition]
        val newDevice = newDevices[newItemPosition]
        return oldDevice.device.address == newDevice.device.address
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldDevice = oldDevices[oldItemPosition]
        val newDevice = newDevices[newItemPosition]
        return oldDevice == newDevice
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val old = oldDevices[oldItemPosition]
        val new = newDevices[newItemPosition]

        val bundle = Bundle()

        if (old.rssi != new.rssi) {
            bundle.putInt(KEY_RSSI, new.rssi)
        }
        return bundle
    }

    companion object {
        const val KEY_RSSI = "com.astar.osterrig.controlrelay.rssi"
    }
}