package com.astar.osterrig.controlrelay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.astar.osterrig.controlrelay.databinding.DialogChangeNameBinding

class ChangeNameDialog : DialogFragment() {

    private var _binding: DialogChangeNameBinding? = null
    private val binding: DialogChangeNameBinding get() = _binding!!

    private var deviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceName = it.getString(KEY_NAME, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChangeNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val width = (screenWidth() * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() = with(binding) {
        editDeviceName.setText(deviceName)
        okButton.setOnClickListener {
            val newDeviceName = editDeviceName.text.toString().trim()
            if (validate(newDeviceName)) {
                setFragmentResult(REQ_CODE, bundleOf(KEY_NAME to newDeviceName))
                dismiss()
            }
        }
    }

    private fun validate(deviceName: String): Boolean {
        if (deviceName.isEmpty()) {
            binding.editDeviceName.error = getString(R.string.name_cannot_be_empty_message)
            return false
        } else if (deviceName.length < 3) {
            binding.editDeviceName.error = getString(R.string.name_short_message)
            return false
        }
        return true
    }

    companion object {
        const val REQ_CODE = "change_name_dialog.REQ_CODE"
        const val KEY_NAME = "change_name_dialog.KEY_NAME"

        fun newInstance(name: String) = ChangeNameDialog().apply {
            arguments = Bundle().apply {
                putString(KEY_NAME, name)
            }
        }
    }
}