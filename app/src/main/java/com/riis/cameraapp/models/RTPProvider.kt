package com.riis.cameraapp.models

import android.app.Activity
import android.content.Intent
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import androidx.fragment.app.Fragment
import com.pedro.rtplibrary.base.DisplayBase

class RTPProvider(
    private val streamCamera: DisplayBase
) {
    private val requestCodeScreenProjector = 179

    fun onStreamingActivityResult(
        destination: String,
        display: Display,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode != requestCodeScreenProjector || resultCode != Activity.RESULT_OK) {
            return
        }

        streamCamera.setIntentResult(Activity.RESULT_OK, data)

        val displayMetrics = DisplayMetrics()
        display.getRealMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        if (streamCamera.prepareAudio() && streamCamera.prepareVideo(
                width,
                height,
                30,
                1200 * 1024,
                0,
                displayMetrics.densityDpi
            )
        ) {
            streamCamera.startStream(destination)
        } else {
            /*This device cant init encoders, this could be for 2 reasons: The encoder selected
                 * doesn't support any configuration setted or your device hasn't a H264 or AAC encoder
                 * (in this case you can see log error valid encoder not found)
                 * */
            Log.e("STREAM", "Could not start stream")
        }
    }

    fun stopStreaming() {
        if (streamCamera.isStreaming) {
            streamCamera.stopStream()
        }
    }

    fun toggleStreaming(fragment: Fragment) {
        if (streamCamera.isStreaming) {
            streamCamera.stopStream()
        } else {
            fragment.startActivityForResult(streamCamera.sendIntent(), requestCodeScreenProjector)
        }
    }
}