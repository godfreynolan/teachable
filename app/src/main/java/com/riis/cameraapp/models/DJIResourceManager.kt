package com.riis.cameraapp.models

import android.content.Context
import dji.common.error.DJIError
import dji.common.error.DJISDKError
import dji.sdk.base.BaseComponent
import dji.sdk.base.BaseProduct
import dji.sdk.flightcontroller.FlightController
import dji.sdk.products.Aircraft
import dji.sdk.sdkmanager.DJISDKInitEvent
import dji.sdk.sdkmanager.DJISDKManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalCoroutinesApi
@FlowPreview
class DJIResourceManager {
    private object HOLDER {
        val INSTANCE = DJIResourceManager()
    }

    companion object {
        val instance: DJIResourceManager by lazy { HOLDER.INSTANCE }
    }

    var flightController: FlightController? = null
    var aircraft: Aircraft? = null

    private var _connectionStatus = ConflatedBroadcastChannel(false)
    val connectionStatus: Flow<Boolean> = _connectionStatus.asFlow()

    private var isRegistrationInProgress = false;

    suspend fun registerApp(context: Context?): Boolean {
        if (isRegistrationInProgress) {
            return false
        }

        isRegistrationInProgress = true

        return suspendCoroutine { continuation ->
            DJISDKManager.getInstance()
                .registerApp(context, object : DJISDKManager.SDKManagerCallback {
                    override fun onDatabaseDownloadProgress(p0: Long, p1: Long) {}

                    override fun onInitProcess(p0: DJISDKInitEvent?, p1: Int) {

                    }

                    override fun onComponentChange(
                        componentKey: BaseProduct.ComponentKey?,
                        old: BaseComponent?,
                        new: BaseComponent?
                    ) {

                    }

                    override fun onProductDisconnect() {
                        resetDefaultValues()
                    }

                    override fun onProductConnect(product: BaseProduct?) {
                        product?.let {
                            try {
                                aircraft = (it as Aircraft)
                                flightController = it.flightController
                                _connectionStatus.offer(true)
                                setupCallbacks()
                            } catch (ex: Exception) {
                                flightController = null
                            }
                        }
                    }

                    override fun onProductChanged(p0: BaseProduct?) {

                    }

                    override fun onRegister(error: DJIError?) {
                        error?.let {
                            if (it == DJISDKError.REGISTRATION_SUCCESS) {
                                DJISDKManager.getInstance().startConnectionToProduct()
                                continuation.resume(true)
                            } else {
                                continuation.resume(false)
                            }

                            isRegistrationInProgress = false
                        }
                    }
                })
        }
    }

    private fun resetDefaultValues() {
        flightController = null
        _connectionStatus.offer(false)
    }

    private fun setupCallbacks() {
        initDroneStateCallback()
        initRCLocationCallback()
        initDroneBatteryCallback()
        initRCBatteryCallback()
    }

    private fun initDroneStateCallback() {
        flightController?.setStateCallback { state ->
        }
    }

    private fun initRCLocationCallback() {
        aircraft?.remoteController?.setGPSDataCallback { data ->
        }
    }

    private fun initDroneBatteryCallback() {
        aircraft?.battery?.setStateCallback { state ->
        }
    }

    private fun initRCBatteryCallback() {
        aircraft?.remoteController?.setChargeRemainingCallback { charge ->
        }
    }
}