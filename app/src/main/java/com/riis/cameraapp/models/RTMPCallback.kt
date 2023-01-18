package com.riis.cameraapp.models

import android.content.Context
import android.widget.Toast
import com.pedro.rtmp.utils.ConnectCheckerRtmp

class RTMPCallback(private val context: Context) : ConnectCheckerRtmp {
    override fun onAuthSuccessRtmp() {
        // We don't care?
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
        // We don't care?
    }

    override fun onConnectionSuccessRtmp() {
        // We don't care?
    }

    override fun onConnectionFailedRtmp(reason: String) {
        Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {
        Toast.makeText(context, "RTMP Connection Started", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthErrorRtmp() {
        Toast.makeText(context, "RTMP Authorization Error", Toast.LENGTH_SHORT).show()
    }

    override fun onDisconnectRtmp() {
        Toast.makeText(context, "Stream disconnected", Toast.LENGTH_SHORT).show()
    }
}