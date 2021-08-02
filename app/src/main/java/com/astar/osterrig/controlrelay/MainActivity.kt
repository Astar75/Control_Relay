package com.astar.osterrig.controlrelay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.astar.osterrig.controlrelay.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun getSettingsStore(): SettingsStore = (application as ControlRelayApp).getSettingsStore()

}