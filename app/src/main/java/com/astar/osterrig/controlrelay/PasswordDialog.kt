package com.astar.osterrig.controlrelay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.astar.osterrig.controlrelay.databinding.DialogPasswordBinding

class PasswordDialog : DialogFragment() {

    private var _binding: DialogPasswordBinding? = null
    private val binding: DialogPasswordBinding get() = _binding!!
    private var newPasswordMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            newPasswordMode = it.getBoolean(KEY_NEW_PASSWORD_MODE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPasswordBinding.inflate(inflater, container, false)
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
        containerFields.isVisible = newPasswordMode
        textInfo.isVisible = newPasswordMode
        okButton.setOnClickListener {
            val password = editPassword.text.toString()
            if (newPasswordMode) {
                val newPassword = editNewPassword.text.toString()
                val confirmPassword = editConfirmPassword.text.toString()

                if (validate(password, newPassword, confirmPassword)) {
                    setFragmentResult(KEY_PASSWORD, bundleOf(
                        KEY_NEW_PASSWORD_MODE to true,
                        KEY_PASSWORD to newPassword
                    ))
                    dismiss()
                }
            } else {
                if (validate(password)) {
                    setFragmentResult(KEY_PASSWORD, bundleOf(KEY_PASSWORD to password))
                    dismiss()
                }
            }
        }
    }

    private fun validate(password: String): Boolean {
        return if (password.length < 6) {
            binding.editPassword.error = getString(R.string.password_length_error)
            false
        } else {
            true
        }
    }

    private fun validate(password: String, newPassword: String, confirmPassword: String): Boolean {
        if (validate(password)) {
            return when {
                newPassword.length < 6 -> {
                    binding.editNewPassword.error = getString(R.string.password_length_error)
                    false
                }
                confirmPassword.length < 6 -> {
                    binding.editConfirmPassword.error = getString(R.string.password_length_error)
                    false
                }
                newPassword != confirmPassword -> {
                    binding.editConfirmPassword.error = getString(R.string.password_mismatch_error)
                    false
                }
                else -> true
            }
        }
        return false
    }

    companion object {
        const val KEY_NEW_PASSWORD_MODE = "password_dialog.KEY_NEW_PASSWORD"
        const val KEY_PASSWORD = "password_dialog.KEY_PASSWORD"
        fun newInstance(newPassword: Boolean = false) = PasswordDialog().apply {
            arguments = Bundle().apply {
                putBoolean(KEY_NEW_PASSWORD_MODE, newPassword)
            }
        }
    }
}