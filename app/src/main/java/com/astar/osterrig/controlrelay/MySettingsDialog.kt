package com.astar.osterrig.controlrelay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.astar.osterrig.controlrelay.databinding.DialogSettingsBinding

class MySettingsDialog: DialogFragment() {

    private var sorting = false

    private var _binding: DialogSettingsBinding? = null
    private val binding: DialogSettingsBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(KEY_SORTING)) {
                sorting = it.getBoolean(KEY_SORTING)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onStart() {
        super.onStart()
        val width = (screenWidth() * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun setupViews() {
        binding.switchSortDevices.isChecked = sorting
        binding.switchSortDevices.setOnCheckedChangeListener { _, isChecked ->
            setFragmentResult(KEY_SCAN_SETTINGS, bundleOf(KEY_SORTING to isChecked))
        }
        binding.okButton.setOnClickListener { dismiss() }
    }

    companion object{

        const val KEY_SCAN_SETTINGS = "com.astar.osterrig.controlrelay.key_scan_settings"
        const val KEY_SORTING = "com.astar.osterrig.controlrelay.key_sort"

        fun newInstance(sort: Boolean) = MySettingsDialog().apply {
            arguments = Bundle().apply {
                putBoolean(KEY_SORTING, sort)
            }
        }
    }
}