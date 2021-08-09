package com.astar.osterrig.controlrelay;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;

class DeviceManager extends BleManager {

    private static final UUID CONTROL_SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID CONTROL_REQUEST_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final UUID CONTROL_RESPONSE_UUID = UUID.fromString("0543e43b-ca02-46e2-a3a6-eef13baee160");

    public static final String RELAY_OPEN_CMD = "p=%s,ro";
    public static final String RELAY_CLOSE_CMD = "p=%s,rc";
    public static final String GET_STATE_CMD = "p=%s,get_state";
    public static final String CHANGE_NAME_CMD = "p=%s,chn=%s";
    public static final String CHANGE_PASS_CMD = "p=%s,chp=%s";

    private BluetoothGattCharacteristic requestCharacteristic;
    private BluetoothGattCharacteristic responseCharacteristic;

    private final MyCallback callback;

    public DeviceManager(@NonNull @NotNull Context context, MyCallback callback) {
        super(context);
        this.callback = callback;
    }

    @NonNull
    @NotNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new DeviceCallbackManager();
    }

    class DeviceCallbackManager extends BleManagerGattCallback {

        @Override
        protected void initialize() {
            super.initialize();

            enableNotifications(responseCharacteristic).enqueue();
            setNotificationCallback(responseCharacteristic).with((device, data) -> {
                parseResponse(data.getStringValue(0));
            });
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull @NotNull BluetoothGatt gatt) {
            BluetoothGattService controlService = gatt.getService(CONTROL_SERVICE_UUID);
            if (controlService != null) {
                requestCharacteristic = controlService.getCharacteristic(CONTROL_REQUEST_UUID);
                responseCharacteristic = controlService.getCharacteristic(CONTROL_RESPONSE_UUID);
            }

            return requestCharacteristic != null && responseCharacteristic != null;
        }

        @Override
        protected void onServicesInvalidated() {
            requestCharacteristic = null;
            responseCharacteristic = null;
        }
    }

    interface MyCallback {
        void onRelayStateResponse(RelayResponse response);
    }

    private void parseResponse(String response) {

        Log.e("DeviceManager", "Response - " + response);

        switch (response) {
            case "close":
                callback.onRelayStateResponse(new RelayResponse.RelayState(RelayResponse.State.CLOSE));
                break;
            case "open":
                callback.onRelayStateResponse(new RelayResponse.RelayState(RelayResponse.State.OPEN));
                break;
            case "correct_pass":
                callback.onRelayStateResponse(new RelayResponse.CorrectPassword(""));
                break;
            case "err_pass":
                callback.onRelayStateResponse(new RelayResponse.ErrorPassword(""));
                break;
        }
    }

    private void sendCommand(@NonNull byte[] command) {
        writeCharacteristic(requestCharacteristic, command).enqueue();
    }

    public void openRelay(@NonNull String password) {
        sendCommand(String.format(Locale.getDefault(), RELAY_OPEN_CMD, password).getBytes());
    }

    public void closeRelay(@NonNull String password) {
        sendCommand(String.format(Locale.getDefault(), RELAY_CLOSE_CMD, password).getBytes());
    }

    public void requestRelayState(@NotNull String password) {
        sendCommand(String.format(Locale.getDefault(), GET_STATE_CMD, password).getBytes());
    }

    public void changeName(@NotNull String password, String name) {
        sendCommand(String.format(
                Locale.getDefault(), CHANGE_NAME_CMD, password, name
        ).getBytes());
    }

    public void changePassword(@NotNull String password, String newPassword) {
        sendCommand(String.format(
                Locale.getDefault(), CHANGE_PASS_CMD, password, newPassword
        ).getBytes());
    }
}