package com.example.aerofeed.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.pedro.library.view.OpenGlView

@Composable
fun CameraPreviewContent(viewModel: CameraViewModel) {
    val clipboardManager = LocalClipboardManager.current

    val isBound by viewModel.isBound.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val deviceIp by viewModel.deviceIp.collectAsState()
    val streamUrl by viewModel.streamUrl.collectAsState()

    // Pulsing animation for the indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Render OpenGL view for camera hardware rendering
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                OpenGlView(ctx).apply {
                    holder.addCallback(object : android.view.SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: android.view.SurfaceHolder) {}

                        override fun surfaceChanged(
                            holder: android.view.SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int
                        ) {
                            if (isBound) {
                                viewModel.startPreview(this@apply)
                            }
                        }

                        override fun surfaceDestroyed(holder: android.view.SurfaceHolder) {
                            viewModel.stopPreview()
                        }
                    })
                }
            },
            update = { view ->
                if (isBound && view.holder.surface.isValid) {
                    viewModel.startPreview(view)
                }
            },
            onRelease = {
                viewModel.stopPreview()
            }
        )

        // Overlay layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.65f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status indicator dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isStreaming) Color.Red.copy(alpha = dotAlpha)
                                    else Color(0xFFEAA300)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isStreaming) "Server Live" else "Server Standby",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val displayUrl = if (isStreaming && streamUrl.isNotEmpty()) {
                        streamUrl
                    } else {
                        "rtsp://$deviceIp:8554/live"
                    }

                    Text(
                        text = "URL: $displayUrl",
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Click to copy URL",
                        color = Color.Cyan.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(displayUrl))
                        }
                    )
                }
            }

            // Bottom Controller Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                        )
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch camera button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { viewModel.switchCamera() }
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Play / Stop button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (isStreaming) Color(0xFFFF3B30) else Color(0xFF34C759)
                        )
                        .clickable { viewModel.toggleStream() }
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isStreaming) "Stop Streaming" else "Start Streaming",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }

    // Clean preview
    DisposableEffect(key1 = viewModel) {
        onDispose {
            viewModel.stopPreview()
        }
    }
}
