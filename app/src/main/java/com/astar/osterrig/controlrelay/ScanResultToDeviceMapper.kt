package com.astar.osterrig.controlrelay

import android.bluetooth.le.ScanResult

interface ScanResultToDeviceMapper {
    fun map(result: ScanResult) : Device

    class Base : ScanResultToDeviceMapper {
        override fun map(result: ScanResult) =
            Device(result.device, result.rssi)
    }
}