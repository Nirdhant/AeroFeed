package com.example.aerofeed.presentation

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import com.example.aerofeed.data.service.RtspService
import com.pedro.library.view.OpenGlView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.NetworkInterface
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private var rtspService: RtspService? = null

    private val _isBound = MutableStateFlow(false)
    val isBound = _isBound.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming = _isStreaming.asStateFlow()

    private val _streamUrl = MutableStateFlow("")
    val streamUrl = _streamUrl.asStateFlow()

    private val _deviceIp = MutableStateFlow("0.0.0.0")
    val deviceIp = _deviceIp.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RtspService.LocalBinder
            val boundService = binder.getService()
            rtspService = boundService
            _isBound.value = true
            _isStreaming.value = boundService.isStreaming
            _streamUrl.value = "rtsp://${getLocalIpAddress()}:8554/live"
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            rtspService = null
            _isBound.value = false
            _isStreaming.value = false
        }
    }

    fun bindService(context: Context) {
        _deviceIp.value = getLocalIpAddress()
        val intent = Intent(context, RtspService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        if (_isBound.value) {
            context.unbindService(serviceConnection)
            _isBound.value = false
            rtspService = null
        }
    }

    fun startPreview(openGlView: OpenGlView) {
        rtspService?.startPreview(openGlView)
    }

    fun stopPreview() {
        rtspService?.stopPreview()
    }

    fun toggleStream() {
        rtspService?.let { service ->
            if (service.isStreaming) {
                service.stopStream()
            } else {
                service.startStream()
            }
            _isStreaming.value = service.isStreaming
            _streamUrl.value = "rtsp://${getLocalIpAddress()}:8554/live"
        }
    }

    fun switchCamera() {
        rtspService?.switchCamera()
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress) {
                        val sAddr = address.hostAddress
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4) {
                            return sAddr
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "0.0.0.0"
    }

    override fun onCleared() {
        super.onCleared()
        rtspService = null
    }
}
