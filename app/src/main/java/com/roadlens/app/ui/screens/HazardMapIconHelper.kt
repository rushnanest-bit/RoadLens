package com.roadlens.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory

/**
 * Generates custom, high-visibility map marker icons for road hazard categories,
 * user location indicators, and searched destination pins.
 * Strictly under 200 lines per project engineering rules.
 */
@Suppress("DEPRECATION")
object HazardMapIconHelper {

    private var potholeIcon: Icon? = null
    private var crackIcon: Icon? = null
    private var otherIcon: Icon? = null
    private var searchPinIcon: Icon? = null
    private var userLocationIcon: Icon? = null

    private const val COLOR_POTHOLE = 0xFFF59E0B.toInt() // Amber
    private const val COLOR_CRACK = 0xFFEF4444.toInt()   // Coral Red
    private const val COLOR_OTHER = 0xFF8B5CF6.toInt()   // Purple
    private const val COLOR_SEARCH = 0xFF2563EB.toInt()  // Royal Blue
    private const val COLOR_USER = 0xFF10B981.toInt()    // Emerald Green

    fun getIconForClass(context: Context, classId: Int): Icon {
        return when {
            classId == 3 -> potholeIcon ?: createPinIcon(context, COLOR_POTHOLE).also { potholeIcon = it }
            classId in 0..2 -> crackIcon ?: createPinIcon(context, COLOR_CRACK).also { crackIcon = it }
            else -> otherIcon ?: createPinIcon(context, COLOR_OTHER).also { otherIcon = it }
        }
    }

    fun getSearchPinIcon(context: Context): Icon {
        return searchPinIcon ?: createPinIcon(context, COLOR_SEARCH).also { searchPinIcon = it }
    }

    fun getUserLocationIcon(context: Context): Icon {
        return userLocationIcon ?: createUserDotIcon(context).also { userLocationIcon = it }
    }

    private fun createPinIcon(context: Context, fillColor: Int): Icon {
        val size = 76
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.argb(70, 0, 0, 0)
        }
        canvas.drawCircle(size / 2f, size / 2f + 3f, 26f, shadowPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(size / 2f, size / 2f, 24f, borderPaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillColor
        }
        canvas.drawCircle(size / 2f, size / 2f, 20f, fillPaint)

        val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(size / 2f, size / 2f, 7f, centerDotPaint)

        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    private fun createUserDotIcon(context: Context): Icon {
        val size = 64
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.argb(60, 16, 185, 129)
        }
        canvas.drawCircle(size / 2f, size / 2f, 28f, haloPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(size / 2f, size / 2f, 18f, borderPaint)

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = COLOR_USER
        }
        canvas.drawCircle(size / 2f, size / 2f, 14f, dotPaint)

        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }
}
