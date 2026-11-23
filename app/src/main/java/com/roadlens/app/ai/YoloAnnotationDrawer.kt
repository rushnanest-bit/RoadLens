package com.roadlens.app.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.roadlens.app.ai.YoloConstants.CLASS_COLORS
import com.roadlens.app.domain.model.RoadDetection
import java.util.Locale
import kotlin.math.max

/**
 * Handles rendering bounding boxes, colored labels, and confidence tags on bitmaps.
 */
class YoloAnnotationDrawer {

    /**
     * Draws detected bounding boxes onto a mutable copy of the original bitmap.
     *
     * @param original Source bitmap.
     * @param detections List of detected road defects.
     * @return Annotated bitmap copy.
     */
    fun drawDetections(
        original: Bitmap,
        detections: List<RoadDetection>
    ): Bitmap {
        val output = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(3f, original.width / 250f)
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.BLACK
            textSize = max(24f, original.width / 28f)
            isFakeBoldText = true
        }

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        for (detection in detections) {
            val color = CLASS_COLORS.getOrElse(detection.classIndex) { Color.RED }
            boxPaint.color = color
            backgroundPaint.color = color

            canvas.drawRect(
                detection.left, detection.top,
                detection.right, detection.bottom,
                boxPaint
            )

            val label = "${detection.className} ${
                String.format(Locale.US, "%.2f", detection.confidence)
            }"

            val padding = 8f
            val labelWidth = textPaint.measureText(label) + (padding * 2)
            val labelHeight = textPaint.textSize + (padding * 2)
            val labelTop = max(0f, detection.top - labelHeight)

            canvas.drawRect(
                detection.left, labelTop,
                detection.left + labelWidth, labelTop + labelHeight,
                backgroundPaint
            )

            canvas.drawText(
                label,
                detection.left + padding,
                labelTop + textPaint.textSize + (padding / 2),
                textPaint
            )
        }

        return output
    }
}
