package com.riis.cameraapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.Display
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.pedro.rtplibrary.rtmp.RtmpDisplay
import com.riis.cameraapp.models.RTMPCallback
import com.riis.cameraapp.models.RTPProvider

class StreamService : Service() {
    private lateinit var rtpProvider: RTPProvider
    private val streamBinder = StreamBinder(this)

    private val channelId = "rtpDisplayStreamChannel"

    private lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder {
        return streamBinder
    }

    override fun onCreate() {
        super.onCreate()

        rtpProvider = RTPProvider(RtmpDisplay(this, true, RTMPCallback(this)))

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        keepAliveTrick()
    }

    override fun onDestroy() {
        super.onDestroy()
        rtpProvider.stopStreaming()
        stopForeground(true)
    }

    private fun keepAliveTrick() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val notification = NotificationCompat.Builder(this, channelId)
                .setOngoing(true)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(1, notification)
        } else {
            startForeground(1, Notification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun onStreamingActivityResult(
        destination: String,
        display: Display,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        rtpProvider.onStreamingActivityResult(
            destination,
            display,
            requestCode,
            resultCode,
            data
        )
    }

    fun stopStreaming() {
        rtpProvider.stopStreaming()
    }

    fun toggleStreaming(fragment: Fragment) {
        rtpProvider.toggleStreaming(fragment)
    }
}