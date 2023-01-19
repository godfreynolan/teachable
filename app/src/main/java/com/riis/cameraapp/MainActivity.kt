package com.riis.cameraapp

import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.riis.cameraapp.R
import dji.sdk.sdkmanager.DJISDKManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        supportActionBar?.hide()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val action = intent?.action
        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED == action) {
            val attachedIntent = Intent()
            attachedIntent.action = DJISDKManager.USB_ACCESSORY_ATTACHED
            sendBroadcast(attachedIntent)
        }
    }
}
