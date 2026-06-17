package com.example.aerofeed.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pedro.common.ConnectChecker
import com.pedro.library.view.OpenGlView
import com.pedro.rtspserver.RtspServerCamera2

class RtspService : Service(), ConnectChecker {

    private val binder = LocalBinder()
    private var rtspServerCamera2: RtspServerCamera2? = null

    var isStreaming = false
        private set
    var isPreviewing = false
        private set

    inner class LocalBinder : Binder() {
        fun getService(): RtspService = this@RtspService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize the RtspServerCamera2 with standard port 8554
        rtspServerCamera2 = RtspServerCamera2(this, this, 8554)
    }

    private fun startForegroundService() {
        val channelId = "rtsp_stream_channel"
        val channelName = "RTSP Stream Service"

        //Create Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("AeroFeed RTSP Server")
            .setContentText("Streaming live camera video on port 8554...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            )
        } else {
            startForeground(1, notification)
        }
    }


    fun startPreview(openGlView: OpenGlView) {
        if (!isPreviewing) {
            rtspServerCamera2?.replaceView(openGlView)
            rtspServerCamera2?.startPreview()
            isPreviewing = true
        }
    }

    fun stopPreview() {
        if (isPreviewing) {
            rtspServerCamera2?.stopPreview()
            isPreviewing = false
        }
    }

    fun startStream() {
        if (!isStreaming) {
            val audioPrepared = rtspServerCamera2?.prepareAudio() ?: false
            val videoPrepared = rtspServerCamera2?.prepareVideo(1280, 720, 1200 * 1024) ?: false
            if (audioPrepared && videoPrepared) {
                startForegroundService()
                rtspServerCamera2?.startStream()
                isStreaming = true
            }
        }
    }

    fun stopStream() {
        if (isStreaming) {
            rtspServerCamera2?.stopStream()
            stopForeground(true)
            isStreaming = false
        }
    }

    fun switchCamera() {
        rtspServerCamera2?.switchCamera()
    }

    fun getStreamUrl(): String {
        return "rtsp://0.0.0.0:8554/live"
    }


    override fun onDestroy() {
        super.onDestroy()
        stopStream()
        stopPreview()
        rtspServerCamera2 = null
    }

    // ConnectChecker Implementation
    override fun onConnectionStarted(rtspUrl: String) {
        // Callback when connection/stream starts
    }

    override fun onConnectionSuccess() {
        // Callback when connection/server starts successfully
    }

    override fun onConnectionFailed(reason: String) {
        // Callback on connection/server start failure
        stopStream()
    }

    override fun onNewBitrate(bitrate: Long) {
        // Callback on streaming bitrate updates
    }

    override fun onDisconnect() {
        // Callback on disconnect/server stop
        isStreaming = false
    }

    override fun onAuthError() {
        // Callback on auth error (not typically used in basic local server)
    }

    override fun onAuthSuccess() {
        // Callback on auth success
    }
}
