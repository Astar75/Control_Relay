package com.astar.osterrig.controlrelay

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DevicesViewModel : ViewModel() {

    private val _deviceResults = MutableLiveData<DeviceScanResult>()
    val deviceResults: LiveData<DeviceScanResult> get() = _deviceResults

    private val adapter = BluetoothAdapter.getDefaultAdapter()
    private val scanDevices: MutableMap<BluetoothDevice, Device> = HashMap()
    private val handlerPostResult = Handler()
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: DeviceScanCallback? = null
    private var isSortBySignal = true

    private val scanResultMapper = ScanResultToDeviceMapper.Base()

    private val filter: List<ScanFilter>
    private val settings: ScanSettings

    init {
        filter = buildScanFilters()
        settings = buildScanSettings()
    }

    private fun buildScanFilters(): List<ScanFilter> {
        //todo add uuid for filter
        return listOf(ScanFilter.Builder().build())
    }

    private fun buildScanSettings() =
        ScanSettings
            .Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

    val isScanning get() = scanCallback != null

    fun startScan() {
        if (scanCallback == null) {
            scanner = adapter.bluetoothLeScanner
            scanCallback = DeviceScanCallback()
            scanner?.startScan(scanCallback)
            startPostResultTimer()
        } else {
            _deviceResults.postValue(DeviceScanResult.Error("Сканирование уже запущено!"))
        }
    }

    fun stopScan() {
        if (scanCallback != null) {
            scanner?.stopScan(scanCallback)
            stopPostResultTimer()
            scanCallback = null
            scanner = null
        }
    }

    fun enableSortBySignal(enable: Boolean) {
        isSortBySignal = enable
    }

    private var postResultRunnable = object : Runnable {
        override fun run() {
            val devices = sortDevices(scanDevices.values.toList())
            _deviceResults.postValue(DeviceScanResult.Success(devices))
            handlerPostResult.postDelayed(this, UPDATE_POST_RESULT)
        }
    }

    private fun startPostResultTimer() {
        handlerPostResult.postDelayed(postResultRunnable, 0)
    }

    private fun stopPostResultTimer() {
        handlerPostResult.removeCallbacks(postResultRunnable)
    }

    private fun sortDevices(devices: List<Device>): List<Device> {
        val filterResults = ArrayList<Device>(devices)
        return if (isSortBySignal) filterResults.sortedBy { it.rssi }
        else filterResults
    }

    inner class DeviceScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            scanDevices[result.device] = scanResultMapper.map(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach { result ->
                scanDevices[result.device] = scanResultMapper.map(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _deviceResults.postValue(DeviceScanResult.Error("Ошибка сканирования. Код $errorCode."))
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPostResultTimer()
        scanner = null
        scanCallback = null
        scanDevices.clear()
    }

    private companion object {
        private val TAG = DevicesViewModel::class.simpleName
        private const val UPDATE_POST_RESULT = 1000L
    }
}
