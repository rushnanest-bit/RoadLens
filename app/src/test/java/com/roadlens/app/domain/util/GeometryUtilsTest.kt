package com.roadlens.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for geometric bounding box operations and IoU math.
 * Strictly under 200 lines per project engineering rules.
 */
class GeometryUtilsTest {

    @Test
    fun calculateIoU_identicalBoxes_returnsOne() {
        val iou = GeometryUtils.calculateIoU(
            aLeft = 0f, aTop = 0f, aRight = 100f, aBottom = 100f,
            bLeft = 0f, bTop = 0f, bRight = 100f, bBottom = 100f
        )
        assertEquals(1.0f, iou, 0.001f)
    }

    @Test
    fun calculateIoU_completelyDisjointBoxes_returnsZero() {
        val iou = GeometryUtils.calculateIoU(
            aLeft = 0f, aTop = 0f, aRight = 10f, aBottom = 10f,
            bLeft = 20f, bTop = 20f, bRight = 30f, bBottom = 30f
        )
        assertEquals(0.0f, iou, 0.001f)
    }

    @Test
    fun calculateIoU_halfOverlappingBoxes_calculatesCorrectRatio() {
        // Box A: [0, 0, 100, 100] -> Area 10000
        // Box B: [50, 0, 150, 100] -> Area 10000
        // Intersection: [50, 0, 100, 100] -> Area 5000
        // Union: 10000 + 10000 - 5000 = 15000
        // Expected IoU: 5000 / 15000 = 1/3 ~ 0.3333
        val iou = GeometryUtils.calculateIoU(
            aLeft = 0f, aTop = 0f, aRight = 100f, aBottom = 100f,
            bLeft = 50f, bTop = 0f, bRight = 150f, bBottom = 100f
        )
        assertEquals(0.3333f, iou, 0.001f)
    }

    @Test
    fun areCentersClose_withinThreshold_returnsTrue() {
        // Centers: Box A (0.5, 0.5), Box B (0.55, 0.55) -> dx = 0.05, dy = 0.05
        val close = GeometryUtils.areCentersClose(
            aLeft = 0.4f, aTop = 0.4f, aRight = 0.6f, aBottom = 0.6f,
            bLeft = 0.45f, bTop = 0.45f, bRight = 0.65f, bBottom = 0.65f,
            maxHorizontalDistance = 0.1f,
            maxVerticalDistance = 0.1f
        )
        assertTrue(close)
    }

    @Test
    fun areCentersClose_beyondThreshold_returnsFalse() {
        val close = GeometryUtils.areCentersClose(
            aLeft = 0.1f, aTop = 0.1f, aRight = 0.2f, aBottom = 0.2f,
            bLeft = 0.7f, bTop = 0.7f, bRight = 0.8f, bBottom = 0.8f,
            maxHorizontalDistance = 0.2f,
            maxVerticalDistance = 0.2f
        )
        assertFalse(close)
    }
}
