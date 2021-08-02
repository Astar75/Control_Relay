package com.astar.osterrig.controlrelay

sealed class DeviceScanResult {
    data class Success(val devices: List<Device>): DeviceScanResult()
    data class Error(val message: String): DeviceScanResult()
}