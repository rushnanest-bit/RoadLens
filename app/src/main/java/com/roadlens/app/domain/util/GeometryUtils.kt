package com.roadlens.app.domain.util

import kotlin.math.max
import kotlin.math.min

/**
 * Pure mathematical utility functions for geometric bounding box operations,
 * Intersection over Union (IoU), and spatial overlap analysis.
 */
object GeometryUtils {

    /**
     * Calculates the Intersection over Union (IoU) of two bounding boxes defined by coordinates.
     * Works with both absolute pixel coordinates and normalized [0.0, 1.0] coordinates.
     *
     * @return Float value in range [0.0, 1.0], where 1.0 represents complete overlap.
     */
    fun calculateIoU(
        aLeft: Float, aTop: Float, aRight: Float, aBottom: Float,
        bLeft: Float, bTop: Float, bRight: Float, bBottom: Float
    ): Float {
        val intersectionLeft = max(aLeft, bLeft)
        val intersectionTop = max(aTop, bTop)
        val intersectionRight = min(aRight, bRight)
        val intersectionBottom = min(aBottom, bBottom)

        val intersectionWidth = max(0f, intersectionRight - intersectionLeft)
        val intersectionHeight = max(0f, intersectionBottom - intersectionTop)
        val intersectionArea = intersectionWidth * intersectionHeight

        val areaA = max(0f, aRight - aLeft) * max(0f, aBottom - aTop)
        val areaB = max(0f, bRight - bLeft) * max(0f, bBottom - bTop)

        val unionArea = areaA + areaB - intersectionArea

        return if (unionArea <= 0f) 0f else intersectionArea / unionArea
    }

    /**
     * Checks if two boxes are spatial neighbors based on center point distance proximity.
     * Useful for tracking defects that shift slightly across consecutive video frames.
     */
    fun areCentersClose(
        aLeft: Float, aTop: Float, aRight: Float, aBottom: Float,
        bLeft: Float, bTop: Float, bRight: Float, bBottom: Float,
        maxHorizontalDistance: Float = 0.22f,
        maxVerticalDistance: Float = 0.22f
    ): Boolean {
        val centerAX = (aLeft + aRight) / 2f
        val centerAY = (aTop + aBottom) / 2f
        val centerBX = (bLeft + bRight) / 2f
        val centerBY = (bTop + bBottom) / 2f

        val dx = kotlin.math.abs(centerAX - centerBX)
        val dy = kotlin.math.abs(centerAY - centerBY)

        return dx < maxHorizontalDistance && dy < maxVerticalDistance
    }
}
