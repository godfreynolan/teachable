package com.riis.cameraapp.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.riis.cameraapp.models.eventbus.ServiceConnectionEvent
import org.greenrobot.eventbus.EventBus

class StreamServiceConnection : ServiceConnection {
    var isBound = false
    var service: StreamService? = null
        private set

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
        EventBus.getDefault().post(ServiceConnectionEvent(false))
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as StreamBinder
        this.service = binder.getService()
        EventBus.getDefault().post(ServiceConnectionEvent(true))
    }
}