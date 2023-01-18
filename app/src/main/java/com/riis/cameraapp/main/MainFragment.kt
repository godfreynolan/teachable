package com.riis.cameraapp.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.riis.cameraapp.R
import com.riis.cameraapp.databinding.MainFragmentBinding
import dji.keysdk.KeyManager
import dji.keysdk.ProductKey
import kotlinx.android.synthetic.main.main_fragment.view.*

class MainFragment : Fragment() {
    private val missingPermissions = ArrayList<String>()
    private val requestPermissionCode = 12345
    private val requiredPermissionList = arrayOf(
        Manifest.permission.VIBRATE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.RECORD_AUDIO
    )
    private val firmKey = ProductKey.create(ProductKey.FIRMWARE_PACKAGE_VERSION)

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpLiveData()
        checkAndRequestPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestPermissionCode) {
            for (i in grantResults.indices.reversed()) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.remove(permissions[i])
                }
            }
        }

        if (missingPermissions.isEmpty()) {
            viewModel.registerDJI(requireActivity())
        } else {
            Toast.makeText(requireActivity(), "Missing permissions!!!", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun checkAndRequestPermissions() {
        for (eachPermission in requiredPermissionList) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    eachPermission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions.add(eachPermission)
            }
        }

        if (missingPermissions.isEmpty()) {
            viewModel.registerDJI(requireActivity())
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                missingPermissions.toTypedArray(),
                requestPermissionCode
            )
        }
    }

    private fun setUpLiveData() {
        viewModel.notifyStatusChanged.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            refreshSDKRelativeUI()
        })

        viewModel.progressToVideo.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                requireView().findNavController().navigate(R.id.action_main_to_video)
                viewModel.progressToVideo.value = false
            }
        })

        viewModel.promptLogin.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                viewModel.loginDJIUserAccount(requireContext())
                viewModel.promptLogin.value = false
            }
        })
    }

    private fun refreshSDKRelativeUI() {
        val mProduct = viewModel.product

        val view = requireView()
        if (null != mProduct && mProduct.isConnected) {
            view.open_button.isEnabled = true

            view.connection_status_text_view.text = "Status: Connected"

            if (null != mProduct.model) {
                view.product_info_text_view.text = mProduct.model.displayName
            } else {
                view.product_info_text_view.text = "Product Information"
            }

            if (KeyManager.getInstance() != null) {
                KeyManager.getInstance().removeKey(firmKey)
                KeyManager.getInstance().addListener(firmKey) { _, _ ->
                }
            }
        } else {
            view.open_button.isEnabled = false
            view.product_info_text_view.text = "Product Information"
            view.connection_status_text_view.text = "Status: Not Connected"
        }
    }
}