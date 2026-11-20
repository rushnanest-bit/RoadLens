package com.roadlens.app.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import com.roadlens.app.ai.YoloConstants.INPUT_SIZE
import kotlin.math.min

/**
 * Handles image preprocessing for YOLO11 models.
 * Employs pre-allocated reusable buffers to achieve zero runtime memory allocations during continuous inference.
 */
class YoloPreprocessor {

    private val reusablePixels = IntArray(INPUT_SIZE * INPUT_SIZE)
    private val reusableInput = FloatArray(3 * INPUT_SIZE * INPUT_SIZE)
    private val reusableLetterboxBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888)
    private val letterboxCanvas = Canvas(reusableLetterboxBitmap)
    private val letterboxDstRect = Rect()

    /**
     * Resizes the input bitmap with letterboxing into a pre-allocated buffer and extracts
     * normalized NCHW floating-point values required by the ONNX model.
     *
     * @param bitmap Source image to preprocess.
     * @return Float array containing planar NCHW values normalized to [0.0, 1.0].
     */
    fun preprocessZeroAlloc(bitmap: Bitmap): FloatArray {
        val scale = min(
            INPUT_SIZE.toFloat() / bitmap.width,
            INPUT_SIZE.toFloat() / bitmap.height
        )

        val resizedW = (bitmap.width * scale).toInt()
        val resizedH = (bitmap.height * scale).toInt()
        val padX = (INPUT_SIZE - resizedW) / 2
        val padY = (INPUT_SIZE - resizedH) / 2

        // Standard YOLO letterbox padding background color (114, 114, 114)
        letterboxCanvas.drawColor(Color.rgb(114, 114, 114))

        // Draw scaled bitmap directly into destination rect without intermediate allocations
        letterboxDstRect.set(padX, padY, padX + resizedW, padY + resizedH)
        letterboxCanvas.drawBitmap(bitmap, null, letterboxDstRect, null)

        val area = INPUT_SIZE * INPUT_SIZE
        reusableLetterboxBitmap.getPixels(reusablePixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        // Bulk copy to NCHW normalized floats into reusableInput
        for (i in 0 until area) {
            val pixel = reusablePixels[i]
            reusableInput[i] = ((pixel shr 16) and 0xFF) / 255f
            reusableInput[area + i] = ((pixel shr 8) and 0xFF) / 255f
            reusableInput[(2 * area) + i] = (pixel and 0xFF) / 255f
        }

        return reusableInput
    }
}
