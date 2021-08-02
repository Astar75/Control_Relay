package com.astar.osterrig.controlrelay

import android.bluetooth.BluetoothDevice

data class Device(val device: BluetoothDevice, val rssi: Int = 0)