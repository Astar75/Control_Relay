package com.astar.osterrig.controlrelay

import android.app.Application

class ControlRelayApp: Application()  {

    private lateinit var settingsStore: SettingsStore

    override fun onCreate() {
        super.onCreate()
        
        settingsStore = SettingsStore.Base(applicationContext)
    }

    fun getSettingsStore() = settingsStore
}