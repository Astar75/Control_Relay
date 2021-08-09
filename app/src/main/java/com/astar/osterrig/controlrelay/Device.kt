package com.astar.osterrig.controlrelay

import android.bluetooth.BluetoothDevice
import android.os.Parcel
import android.os.Parcelable

data class Device(
    val device: BluetoothDevice,
    val rssi: Int = 0): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(BluetoothDevice::class.java.classLoader)!!,
        parcel.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeParcelable(device, flags)
    }

    companion object CREATOR : Parcelable.Creator<Device> {
        override fun createFromParcel(parcel: Parcel): Device {
            return Device(parcel)
        }

        override fun newArray(size: Int): Array<Device?> {
            return arrayOfNulls(size)
        }
    }
}