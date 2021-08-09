package com.astar.osterrig.controlrelay

import android.content.Context

interface SettingsStore {

    fun saveEnableSortDevices(enable: Boolean)
    fun loadEnableSortDevices(): Boolean

    fun saveDeviceName(address: String, name: String)
    fun fetchDeviceName(address: String): String
    fun allDeviceNames(): Map<String, String>
    fun removeDeviceName(address: String)
    fun savePassword(address: String, password: String)
    fun fetchPassword(address: String): String
    fun removePassword(address: String)
    fun removeAllPasswords()

    class Base(context: Context) : SettingsStore {
        private val settingsStore = context.getSharedPreferences(NAME_STORE, Context.MODE_PRIVATE)
        private val passStore = context.getSharedPreferences(NAME_PASS_STORE, Context.MODE_PRIVATE)
        private val devicesStore = context.getSharedPreferences(NAME_DEVICE_STORE, Context.MODE_PRIVATE)

        override fun saveEnableSortDevices(enable: Boolean) {
            settingsStore.edit().putBoolean(KEY_SORT_DEVICE, enable).apply()
        }

        override fun loadEnableSortDevices() = settingsStore.getBoolean(KEY_SORT_DEVICE, false)

        override fun saveDeviceName(address: String, name: String) {
            devicesStore.edit().putString(address, name).apply()
        }

        override fun allDeviceNames(): Map<String, String> {
            return devicesStore.all.mapValues { it.toString() }
        }

        override fun fetchDeviceName(address: String): String {
            return devicesStore.getString(address, "") ?: ""
        }

        override fun removeDeviceName(address: String) {
            if (devicesStore.contains(address)) {
                devicesStore.edit().remove(address).apply()
            }
        }

        override fun savePassword(address: String, password: String) {
            if (!passStore.contains(address)) {
                passStore.edit().putString(address, password).apply()
            }
        }

        override fun fetchPassword(address: String): String {
            return passStore.getString(address, "") ?: ""
        }

        override fun removePassword(address: String) {
            passStore.edit().remove(address).apply()
        }

        override fun removeAllPasswords() {
            passStore.edit().clear().apply()
        }

        private companion object {
            const val KEY_SORT_DEVICE = "settings_store.KEY_SORT_DEVICE"
            const val NAME_STORE = "settings_store.settings_store"
            const val NAME_PASS_STORE = "settings_store.pass_store"
            const val NAME_DEVICE_STORE = "settings_store.devices_store"
        }
    }
}