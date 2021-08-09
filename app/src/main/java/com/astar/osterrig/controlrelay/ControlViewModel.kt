package com.astar.osterrig.controlrelay

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import no.nordicsemi.android.ble.observer.ConnectionObserver

class ControlViewModel(
    application: Application
) : AndroidViewModel(application), ConnectionObserver, DeviceManager.MyCallback {

    private val _relayConnectionState = MutableLiveData<RelayConnectionState>()
    val relayConnectionState: LiveData<RelayConnectionState> get() = _relayConnectionState

    private val _relayResponse = MutableLiveData<RelayResponse>()
    val relayResponse: LiveData<RelayResponse> get() = _relayResponse

    private val deviceManager: DeviceManager = DeviceManager(getApplication(), this)
    private var currentDevice: Device? = null
    private var password: String = ""

    val isDeviceConnected: Boolean
        get() = deviceManager.isConnected

    init {
        deviceManager.setConnectionObserver(this)
    }

    fun connect(device: Device) {
        if (currentDevice == null) {
            currentDevice = device
            reconnect()
        }
    }

    fun reconnect() {
        if (currentDevice != null) {
            deviceManager.connect(currentDevice!!.device)
                .retry(3, 100)
                .useAutoConnect(false)
                .done {
                    Log.d("Reconnect", "Connect successful!")
                }
                .enqueue()
        }
    }

    fun disconnect() {
        currentDevice = null
        deviceManager.disconnect().enqueue()
    }

    fun setPassword(pswd: String) {
        password = pswd
    }

    fun getPassword(): String {
        return password
    }

    fun openRelay() {
        deviceManager.openRelay(password)
    }

    fun closeRelay() {
        deviceManager.closeRelay(password)
    }

    fun requestRelayState() {
        deviceManager.requestRelayState(password)
    }

    fun changeName(name: String) {
        deviceManager.changeName(password, name)
    }

    fun changePassword(newPassword: String) {
        deviceManager.changePassword(password, newPassword)
    }

    override fun onRelayStateResponse(response: RelayResponse) {
        _relayResponse.value = response
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        _relayConnectionState.value = RelayConnectionState.Connecting(device)
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        _relayConnectionState.value = RelayConnectionState.Connected(device)
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        _relayConnectionState.value = RelayConnectionState.FailedToConnect(device, reason)
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        _relayConnectionState.value = RelayConnectionState.Ready(device)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {}

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        _relayConnectionState.value = RelayConnectionState.Disconnected(device)
    }

    override fun onCleared() {
        super.onCleared()
        if (deviceManager.isConnected) {
            disconnect()
        }
    }
}

sealed class RelayConnectionState {
    class Connecting(val device: BluetoothDevice) : RelayConnectionState()
    class Connected(val device: BluetoothDevice) : RelayConnectionState()
    class Ready(val device: BluetoothDevice) : RelayConnectionState()
    class Disconnected(val device: BluetoothDevice) : RelayConnectionState()
    class FailedToConnect(val device: BluetoothDevice, val reason: Int) : RelayConnectionState()
}