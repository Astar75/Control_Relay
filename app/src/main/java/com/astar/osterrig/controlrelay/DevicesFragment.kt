package com.astar.osterrig.controlrelay

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astar.osterrig.controlrelay.databinding.FragmentDevicesBinding

class DevicesFragment : Fragment() {

    private var _binding: FragmentDevicesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DevicesViewModel by viewModels()
    private val adapter = DeviceAdapter()

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startScan()
        } else {
            toast(getString(R.string.location_permission_message))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerDevices()
        setupSubscribeToViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        scanDevices(true)
        viewModel.enableSortBySignal(true)
    }

    override fun onStop() {
        super.onStop()
        scanDevices(false)
    }

    private fun scanDevices(enable: Boolean) {
        if (enable) {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        if (viewModel.isScanning) {
            viewModel.stopScan()
        }
    }

    private fun setupRecyclerDevices() {
        with(binding) {
            recyclerDevices.setHasFixedSize(true)
            recyclerDevices.layoutManager = LinearLayoutManager(requireContext())
            val divider = DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
            recyclerDevices.addItemDecoration(divider)
            recyclerDevices.adapter = adapter
        }
    }

    private fun setupSubscribeToViewModel() {
        viewModel.deviceResults.observe(viewLifecycleOwner, { results ->
            when(results) {
                is DeviceScanResult.Success -> updateListDevices(results.devices)
                is DeviceScanResult.Error -> toast(results.message)
            }
        })
    }

    private fun updateListDevices(devices: List<Device>) {
        adapter.setItems(devices)
    }
}