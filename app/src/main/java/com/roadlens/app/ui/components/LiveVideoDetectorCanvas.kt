package com.roadlens.app.ui.components

import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.ai.YoloConstants.CLASS_COLORS
import com.roadlens.app.domain.model.RoadDetection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max

/**
 * Live hardware-decoded video surface with real-time YOLO bounding box and confidence label overlay.
 * Video plays once without looping and stops upon completion. Maintained under 200 lines.
 */
@Composable
fun LiveVideoDetectorCanvas(
    videoUri: Uri,
    detector: RoadDefectDetector,
    isPlaying: Boolean,
    seekToMs: Long?,
    onPlaybackStateChanged: (isPlaying: Boolean, currentMs: Long, durationMs: Long) -> Unit,
    onFrameAnalyzed: (detections: List<RoadDetection>, frameTimeMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var textureView by remember { mutableStateOf<TextureView?>(null) }
    var videoAspectRatio by remember { mutableFloatStateOf(16f / 9f) }
    var currentDetections by remember { mutableStateOf<List<RoadDetection>>(emptyList()) }

    val labelTextPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.BLACK; textSize = 26f; isFakeBoldText = true }
    }
    val labelBgPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL } }

    LaunchedEffect(seekToMs) {
        seekToMs?.let { target -> mediaPlayer?.let { runCatching { it.seekTo(target.toInt()) } } }
    }

    LaunchedEffect(isPlaying) {
        mediaPlayer?.let { player ->
            runCatching {
                if (isPlaying && !player.isPlaying) player.start()
                else if (!isPlaying && player.isPlaying) player.pause()
            }
        }
    }

    LaunchedEffect(mediaPlayer) {
        var lastSec = -1L
        while (isActive) {
            mediaPlayer?.let { player ->
                runCatching {
                    val curr = player.currentPosition.toLong()
                    val dur = player.duration.toLong().coerceAtLeast(0L)
                    val sec = curr / 1000
                    if (sec != lastSec) {
                        lastSec = sec
                        onPlaybackStateChanged(player.isPlaying, curr, dur)
                    }
                }
            }
            delay(350L)
        }
    }

    LaunchedEffect(textureView, mediaPlayer, isPlaying) {
        while (isActive) {
            val tv = textureView
            val mp = mediaPlayer
            if (tv != null && tv.isAvailable && mp != null && (isPlaying || currentDetections.isEmpty())) {
                val aspect = if (videoAspectRatio > 0f) videoAspectRatio else 16f / 9f
                val sampleW = 480
                val sampleH = (sampleW / aspect).toInt().coerceIn(180, 480)
                val bmp = runCatching { tv.getBitmap(sampleW, sampleH) }.getOrNull()

                if (bmp != null) {
                    val frameTime = runCatching { mp.currentPosition.toLong() }.getOrDefault(0L)
                    withContext(Dispatchers.Default) {
                        try {
                            val results = detector.detectOnly(bmp)
                            withContext(Dispatchers.Main) {
                                currentDetections = results
                                onFrameAnalyzed(results, frameTime)
                            }
                        } finally { bmp.recycle() }
                    }
                }
            }
            delay(100L)
        }
    }

    DisposableEffect(videoUri) {
        onDispose {
            runCatching { mediaPlayer?.stop(); mediaPlayer?.release() }
            mediaPlayer = null
        }
    }

    Box(
        modifier = modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF101815)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    textureView = this
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                            mediaPlayer = MediaPlayer().apply {
                                runCatching {
                                    setDataSource(ctx, videoUri); setSurface(Surface(st))
                                    setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                                    isLooping = false
                                    setOnPreparedListener { p ->
                                        if (p.videoWidth > 0 && p.videoHeight > 0) videoAspectRatio = p.videoWidth.toFloat() / p.videoHeight.toFloat()
                                        if (isPlaying) p.start()
                                        onPlaybackStateChanged(isPlaying, 0L, p.duration.toLong().coerceAtLeast(0L))
                                    }
                                    setOnCompletionListener { p -> onPlaybackStateChanged(false, p.duration.toLong().coerceAtLeast(0L), p.duration.toLong().coerceAtLeast(0L)) }
                                    prepareAsync()
                                }
                            }
                        }
                        override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) = Unit
                        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                            runCatching { mediaPlayer?.release() }; mediaPlayer = null; return true
                        }
                        override fun onSurfaceTextureUpdated(st: SurfaceTexture) = Unit
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (det in currentDetections) {
                val colorInt = CLASS_COLORS.getOrElse(det.classIndex) { android.graphics.Color.RED }
                val left = det.normLeft * size.width
                val top = det.normTop * size.height
                val width = (det.normRight - det.normLeft) * size.width
                val height = (det.normBottom - det.normTop) * size.height

                drawRect(Color(colorInt), Offset(left, top), Size(width, height), style = Stroke(width = 3.dp.toPx()))

                val label = "${det.className} ${String.format(Locale.US, "%.2f", det.confidence)}"
                val pad = 6f
                val textW = labelTextPaint.measureText(label) + (pad * 2)
                val textH = labelTextPaint.textSize + (pad * 2)
                val badgeTop = max(0f, top - textH)

                labelBgPaint.color = colorInt
                drawContext.canvas.nativeCanvas.apply {
                    drawRect(left, badgeTop, left + textW, badgeTop + textH, labelBgPaint)
                    drawText(label, left + pad, badgeTop + labelTextPaint.textSize + (pad / 2), labelTextPaint)
                }
            }
        }
    }
}
