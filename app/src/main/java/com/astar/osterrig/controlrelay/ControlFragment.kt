package com.astar.osterrig.controlrelay

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.astar.osterrig.controlrelay.databinding.FragmentControlBinding

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ControlViewModel
    private val args: ControlFragmentArgs by navArgs()

    private var relayOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ControlViewModel::class.java)
        setFragmentResultListener(PasswordDialog.KEY_PASSWORD, onPasswordInputListener)
        setFragmentResultListener(ChangeNameDialog.REQ_CODE, onChangeNameDeviceListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupToolbar()
        setupSubscribeViewModel()
    }

    override fun onStart() {
        super.onStart()
        unlockControlUi(false)
        viewModel.connect(args.device)
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnect()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener)
    }

    private val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { menuItem ->
        if (viewModel.isDeviceConnected) {
            when (menuItem.itemId) {
                R.id.action_change_name -> {
                    openChangeNameDialog()
                }
                R.id.action_change_password -> {
                    openChangePasswordDialog()
                }
            }
        } else {
            toast(getString(R.string.device_not_connected))
        }
        true
    }

    private val onChangeNameDeviceListener: ((String, Bundle) -> Unit) = { _, bundle ->
        val newName = bundle.getString(ChangeNameDialog.KEY_NAME) ?: ""
        if (newName.isNotEmpty()) {
            viewModel.changeName(newName)
            findNavController().navigateUp()
        } else {
            toast(getString(R.string.error_changing_device_name))
        }
    }

    private fun openChangeNameDialog() {
        val dialog = ChangeNameDialog.newInstance(args.device.device.name)
        dialog.show(parentFragmentManager, "change_name_device")
    }

    private fun openChangePasswordDialog() {
        val dialog = PasswordDialog.newInstance(true)
        dialog.show(parentFragmentManager, "create_new_password")
    }

    private fun setupViews() = with(binding) {
        unlockControlUi(false)
        buttonControlRelay.setOnClickListener {
            if (relayOpen) {
                viewModel.closeRelay()
            } else {
                viewModel.openRelay()
            }
            viewModel.requestRelayState()
        }
        buttonTryAgain.setOnClickListener {
            viewModel.reconnect()
        }
    }

    private fun singIn() {
        val device = args.device

        val password = (requireActivity() as MainActivity)
            .getSettingsStore()
            .fetchPassword(device.device.address)

        if (password.isEmpty()) {
            openInputPasswordDialog()
        } else {
            viewModel.setPassword(password)
            viewModel.requestRelayState()
        }
    }

    private fun openInputPasswordDialog() {
        val dialog = PasswordDialog.newInstance()
        dialog.show(parentFragmentManager, "password")
    }

    private val onPasswordInputListener: ((String, Bundle) -> Unit) = { _, bundle ->
        val newPasswordMode = bundle.getBoolean(PasswordDialog.KEY_NEW_PASSWORD_MODE)
        if (newPasswordMode) {
            val newPassword = bundle.getString(PasswordDialog.KEY_PASSWORD) ?: ""
            if (newPassword.isNotEmpty()) {
                viewModel.changePassword(newPassword)
                replaceCurrentPassword(newPassword)
                findNavController().navigateUp()
            } else {
                toast(getString(R.string.password_change_error))
            }
        } else {
            val password = bundle.getString(PasswordDialog.KEY_PASSWORD) ?: ""
            if (password.isNotEmpty()) {
                viewModel.setPassword(password)
                viewModel.requestRelayState()
            }
        }
    }

    private fun replaceCurrentPassword(newPassword: String) {
        (requireActivity() as MainActivity).getSettingsStore().apply {
            removePassword(args.device.device.address)
            savePassword(args.device.device.address, newPassword)
        }
    }

    private fun unlockControlUi(enable: Boolean) = with(binding) {
        buttonControlRelay.isEnabled = enable
    }

    private fun connectionDevice() = with(binding) {
        buttonControlRelay.isVisible = true
        buttonControlRelay.isEnabled = false
        containerProgress.isVisible = true
        buttonTryAgain.isVisible = false
        toolbar.title = getString(R.string.connection)
    }

    private fun connectedDevice(device: BluetoothDevice) = with(binding) {
        containerProgress.isVisible = false
        buttonTryAgain.isVisible = false
        toolbar.title = device.name ?: getString(R.string.unnamed)
        toolbar.subtitle = device.address
    }

    private fun readyDevice() {
        unlockControlUi(true)
        singIn()
    }

    private fun failedToConnect(reason: Int) = with(binding) {
        toast("Ошибка при подключении. Код $reason")
        unlockControlUi(false)
        showTryAgain()
    }

    private fun showTryAgain() = with(binding) {
        unlockControlUi(false)
        buttonTryAgain.isVisible = true
        buttonControlRelay.isVisible = false
        containerProgress.isVisible = false
    }

    private fun updateUiRelayState(state: RelayResponse) = with(binding) {
        when (state) {
            is RelayResponse.RelayState -> {
                when (state.state) {
                    RelayResponse.State.OPEN -> {
                        relayOpen = true
                        toolbar.title = "${args.device.device.name} - ${getString(R.string.open)}"
                        buttonControlRelay.text = getString(R.string.close)
                    }
                    RelayResponse.State.CLOSE -> {
                        relayOpen = false
                        toolbar.title = "${args.device.device.name} - ${getString(R.string.close)}"
                        buttonControlRelay.text = getString(R.string.open)
                    }
                }
            }
            is RelayResponse.CorrectPassword -> {
                unlockControlUi(true)
                val password = viewModel.getPassword()
                if (password.isNotEmpty()) {
                    (requireActivity() as MainActivity)
                        .getSettingsStore()
                        .savePassword(args.device.device.address, password)
                }
            }
            is RelayResponse.ErrorPassword -> {
                openInputPasswordDialog()
                unlockControlUi(false)
            }
        }
    }

    private fun setupSubscribeViewModel() {
        viewModel.relayConnectionState.observe(viewLifecycleOwner, { state ->
            when (state) {
                is RelayConnectionState.Connecting -> connectionDevice()
                is RelayConnectionState.Connected -> connectedDevice(state.device)
                is RelayConnectionState.Ready -> readyDevice()
                is RelayConnectionState.Disconnected -> showTryAgain()
                is RelayConnectionState.FailedToConnect -> failedToConnect(state.reason)
            }
        })

        viewModel.relayResponse.observe(viewLifecycleOwner, { state ->
            updateUiRelayState(state)
        })
    }
}