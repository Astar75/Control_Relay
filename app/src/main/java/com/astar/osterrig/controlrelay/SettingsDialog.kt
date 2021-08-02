package com.astar.osterrig.controlrelay

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsDialog : DialogFragment() {

    private lateinit var sortDevicesSwitch: SwitchMaterial

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = createView()
        sortDevicesSwitch = view.findViewById(R.id.switchSortDevices)
        sortDevicesSwitch.isChecked = arguments?.getBoolean(SORT_DEVICES) ?: false

        val alertBuilder = AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.settings_text))
            setView(view)
            setPositiveButton("OK") { _, _ ->
                setFragmentResult(
                    KEY_RESULT_SORT_DEVICES,
                    bundleOf(KEY_RESULT_SORT_DEVICES to sortDevicesSwitch.isChecked)
                )
            }
        }.create()
        alertBuilder.setOnShowListener {
            setupViews()
        }
        return alertBuilder
    }

    private fun createView() = LayoutInflater
        .from(requireContext())
        .inflate(R.layout.dialog_settings, null)

    private fun setupViews() {
        // TODO: 02.08.2021 fix
    }

    companion object {

        const val KEY_RESULT_SORT_DEVICES = "com.astar.controlrelay.KEY_RESULT_SORT_DEVICES"
        const val SORT_DEVICES = "com.astar.controlrelay.KEY_SORT_DEVICES"

        @JvmStatic
        fun newInstance(sortEnable: Boolean) = SettingsDialog().apply {
            arguments = Bundle().apply {
                putBoolean(SORT_DEVICES, sortEnable)
            }
        }
    }
}