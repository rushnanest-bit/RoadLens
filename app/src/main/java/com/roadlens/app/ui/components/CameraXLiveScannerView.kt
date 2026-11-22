package com.roadlens.app.ui.components

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.ai.YoloConstants.CLASS_COLORS
import com.roadlens.app.domain.model.RoadDetection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.max

/**
 * CameraX live camera scanning component with 1 FPS paced inference and KEEP_ONLY_LATEST backpressure.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun CameraXLiveScannerView(
    detector: RoadDefectDetector,
    isScanningActive: Boolean,
    onFrameAnalyzed: (detections: List<RoadDetection>, timestampMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentDetections by remember { mutableStateOf<List<RoadDetection>>(emptyList()) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var lastAnalyzedTime by remember { mutableLongStateOf(0L) }

    val textPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.BLACK; textSize = 26f; isFakeBoldText = true }
    }
    val bgPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL } }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    Box(
        modifier = modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = this.surfaceProvider
                        }
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val now = System.currentTimeMillis()
                            if (isScanningActive && (now - lastAnalyzedTime >= 1000L)) {
                                lastAnalyzedTime = now
                                val bitmap = runCatching { imageProxy.toBitmap() }.getOrNull()
                                if (bitmap != null) {
                                    val results = detector.detectOnly(bitmap)
                                    bitmap.recycle()
                                    ContextCompat.getMainExecutor(ctx).execute {
                                        currentDetections = results
                                        onFrameAnalyzed(results, now)
                                    }
                                }
                            }
                            imageProxy.close()
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        runCatching {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
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
                val padding = 8f
                val labelWidth = textPaint.measureText(label) + (padding * 2)
                val labelHeight = textPaint.textSize + (padding * 2)
                val labelTop = max(0f, top - labelHeight)

                bgPaint.color = colorInt
                drawContext.canvas.nativeCanvas.apply {
                    drawRect(left, labelTop, left + labelWidth, labelTop + labelHeight, bgPaint)
                    drawText(label, left + padding, labelTop + textPaint.textSize + (padding / 2), textPaint)
                }
            }
        }
    }
}
