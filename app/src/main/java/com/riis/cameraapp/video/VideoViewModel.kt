package com.riis.cameraapp.video

import androidx.lifecycle.ViewModel
import com.riis.cameraapp.models.DJIResourceManager
import dji.sdk.products.Aircraft
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class VideoViewModel : ViewModel() {
    var product: Aircraft? = null
        get() {
            field = DJIResourceManager.instance.aircraft

            return field
        }
        private set
}