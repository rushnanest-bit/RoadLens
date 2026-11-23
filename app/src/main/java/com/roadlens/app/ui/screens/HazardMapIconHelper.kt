package com.roadlens.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory

/**
 * Generates custom, high-visibility map marker icons for road hazard categories.
 * Category Palette:
 * - Potholes: Amber/Orange (#F59E0B)
 * - Cracks: Red (#EF4444)
 * - Other Hazards: Purple (#8B5CF6)
 * Strictly under 200 lines per project engineering rules.
 */
@Suppress("DEPRECATION")
object HazardMapIconHelper {

    private var potholeIcon: Icon? = null
    private var crackIcon: Icon? = null
    private var otherIcon: Icon? = null

    private const val COLOR_POTHOLE = 0xFFF59E0B.toInt()
    private const val COLOR_CRACK = 0xFFEF4444.toInt()
    private const val COLOR_OTHER = 0xFF8B5CF6.toInt()

    fun getIconForClass(context: Context, classId: Int): Icon {
        return when {
            classId == 3 -> potholeIcon ?: createPinIcon(context, COLOR_POTHOLE).also { potholeIcon = it }
            classId in 0..2 -> crackIcon ?: createPinIcon(context, COLOR_CRACK).also { crackIcon = it }
            else -> otherIcon ?: createPinIcon(context, COLOR_OTHER).also { otherIcon = it }
        }
    }

    private fun createPinIcon(context: Context, fillColor: Int): Icon {
        val size = 72
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Drop shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.argb(70, 0, 0, 0)
        }
        canvas.drawCircle(size / 2f, size / 2f + 3f, 26f, shadowPaint)

        // Crisp white outer border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(size / 2f, size / 2f, 24f, borderPaint)

        // Distinct category color body
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillColor
        }
        canvas.drawCircle(size / 2f, size / 2f, 20f, fillPaint)

        // Clean white center focal dot
        val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(size / 2f, size / 2f, 7f, centerDotPaint)

        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }
}
