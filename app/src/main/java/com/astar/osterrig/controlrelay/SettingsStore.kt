package com.astar.osterrig.controlrelay

import android.content.Context

interface SettingsStore {

    fun saveEnableSortDevices(enable: Boolean)
    fun loadEnableSortDevices(): Boolean

    fun savePassword(address: String, password: String)
    fun fetchPassword(address: String): String
    fun removePassword(address: String)
    fun removeAllPasswords()

    class Base(context: Context) : SettingsStore {
        private val settings = context.getSharedPreferences(NAME_STORE, Context.MODE_PRIVATE)
        private val devicesStore =
            context.getSharedPreferences(NAME_DEVICES_STORE, Context.MODE_PRIVATE)

        override fun saveEnableSortDevices(enable: Boolean) {
            settings.edit().putBoolean(KEY_SORT_DEVICE, enable).apply()
        }

        override fun loadEnableSortDevices() = settings.getBoolean(KEY_SORT_DEVICE, false)

        override fun savePassword(address: String, password: String) {
            if (!devicesStore.contains(address)) {
                devicesStore.edit().putString(address, password).apply()
            }
        }

        override fun fetchPassword(address: String): String {
            return devicesStore.getString(address, "") ?: ""
        }

        override fun removePassword(address: String) {
            devicesStore.edit().remove(address).apply()
        }

        override fun removeAllPasswords() {
            devicesStore.edit().clear().apply()
        }

        private companion object {
            const val KEY_SORT_DEVICE = "com.astar.osterrig.controlrelay.KEY_SORT_DEVICE"
            const val NAME_STORE = "control_relay_settings"
            const val NAME_DEVICES_STORE = "devices_store"
        }
    }
}