package com.riis.cameraapp.video

import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.riis.cameraapp.BuildConfig
import com.riis.cameraapp.databinding.VideoFragmentBinding
import com.riis.cameraapp.models.eventbus.ServiceConnectionEvent
import com.riis.cameraapp.service.StreamService
import com.riis.cameraapp.service.StreamServiceConnection
import dji.common.product.Model
import dji.sdk.camera.VideoFeeder
import dji.sdk.codec.DJICodecManager
import kotlinx.android.synthetic.main.video_fragment.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VideoFragment : Fragment() {
    private val viewModel: VideoViewModel by viewModels()

    private var codecManager: DJICodecManager? = null

    // The callback for receiving the raw H264 video data for camera live view
    private var mReceivedVideoDataListener =
        VideoFeeder.VideoDataListener { videoBuffer: ByteArray?, size: Int ->
            codecManager?.sendDataToDecoder(videoBuffer, size)
        }

    private val streamServiceConnection = StreamServiceConnection()

    private var streamService: StreamService? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = VideoFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViews(view)

        streamService?.toggleStreaming(this)
    }

    override fun onResume() {
        super.onResume()

        val product = viewModel.product

        if (product?.isConnected == false) {
            Toast.makeText(requireActivity(), "Product is null or not connected", Toast.LENGTH_LONG).show()
        } else if (product?.model != Model.UNKNOWN_AIRCRAFT) {
            VideoFeeder.getInstance().primaryVideoFeed.addVideoDataListener(
                mReceivedVideoDataListener
            )
        }

        if (streamService != null) {
            return
        }

        val intent = Intent(requireActivity(), StreamService::class.java)

        if (!streamServiceConnection.isBound) {
            val result = requireActivity().bindService(intent, streamServiceConnection, Context.BIND_AUTO_CREATE)

            streamServiceConnection.isBound = result
            Log.e("STREAM", "Service has been bound: $result")
        }

        streamService = streamServiceConnection.service
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        if (mReceivedVideoDataListener != null) {
            VideoFeeder.getInstance().primaryVideoFeed
                .removeVideoDataListener(mReceivedVideoDataListener)
        }

        streamService?.stopStreaming()
        super.onPause()
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)

        if (streamService != null) {
            if (streamServiceConnection.isBound) {
                requireActivity().unbindService(streamServiceConnection)
                streamServiceConnection.isBound = false
            }

            streamService?.stopSelf()
            streamService = null
        }

        super.onStop()
    }

    override fun onDestroy() {
        codecManager?.cleanSurface()
        codecManager?.destroyCodec()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Broadcasts to appriis twitch (twitch.tv/appriis)
        streamService?.onStreamingActivityResult(
            "rtmp://live.twitch.tv/app/" + BuildConfig.TWITCH_KEY,
            requireActivity().windowManager.defaultDisplay,
            requestCode,
            resultCode,
            data
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onServiceConnectionEvent(event: ServiceConnectionEvent) {
        if (event.connected) {
            streamService = streamServiceConnection.service

            streamService?.toggleStreaming(this)
        } else {
            streamService = null
        }
    }

    private fun setUpViews(view: View) {
        view.video_texture_view.surfaceTextureListener =
            object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {

                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    codecManager?.cleanSurface()
                    return false
                }

                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    if (codecManager == null) {
                        codecManager = DJICodecManager(requireActivity(), surface, width, height)
                    }
                }
            }
    }
}