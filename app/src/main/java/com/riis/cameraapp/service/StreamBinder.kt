package com.riis.cameraapp.service

import android.os.Binder

class StreamBinder(private val service: StreamService): Binder() {
    fun getService(): StreamService {
        return service
    }
}