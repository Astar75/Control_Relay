package com.astar.osterrig.controlrelay

import android.content.Context

interface SettingsStore {

    fun saveEnableSortDevices(enable: Boolean)
    fun loadEnableSortDevices(): Boolean

    class Base(context: Context) : SettingsStore {
        private val preferences = context.getSharedPreferences(NAME_STORE, Context.MODE_PRIVATE)

        override fun saveEnableSortDevices(enable: Boolean) {
            preferences.edit().putBoolean(KEY_SORT_DEVICE, enable).apply()
        }

        override fun loadEnableSortDevices() = preferences.getBoolean(KEY_SORT_DEVICE, false)

        private companion object {
            const val KEY_SORT_DEVICE = "com.astar.osterrig.controlrelay.KEY_SORT_DEVICE"
            const val NAME_STORE = "control_relay_settings"
        }
    }
}