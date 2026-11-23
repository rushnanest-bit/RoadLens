package com.roadlens.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for geometric bounding box operations, IoU calculations, and center proximity.
 * Strictly under 200 lines per project engineering rules.
 * Covers positive, negative, and boundary-condition scenarios (KPI 4.7).
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
        // Box A: [0, 0, 100, 100] (Area 10,000), Box B: [50, 0, 150, 100] (Area 10,000)
        // Intersection: [50, 0, 100, 100] (Area 5,000), Union: 15,000 -> IoU = 1/3
        val iou = GeometryUtils.calculateIoU(
            aLeft = 0f, aTop = 0f, aRight = 100f, aBottom = 100f,
            bLeft = 50f, bTop = 0f, bRight = 150f, bBottom = 100f
        )
        assertEquals(0.3333f, iou, 0.001f)
    }

    @Test
    fun calculateIoU_containedBox_returnsAreaRatio() {
        // Box A: [0, 0, 100, 100] (Area 10,000)
        // Box B: [25, 25, 75, 75] (Area 2,500) -> Intersection: 2,500, Union: 10,000 -> IoU = 0.25
        val iou = GeometryUtils.calculateIoU(
            aLeft = 0f, aTop = 0f, aRight = 100f, aBottom = 100f,
            bLeft = 25f, bTop = 25f, bRight = 75f, bBottom = 75f
        )
        assertEquals(0.25f, iou, 0.001f)
    }

    @Test
    fun calculateIoU_touchingEdges_returnsZero() {
        // Touching at edge x = 50, zero 2D intersection area
        val iou = GeometryUtils.calculateIoU(
            aLeft = 0f, aTop = 0f, aRight = 50f, aBottom = 100f,
            bLeft = 50f, bTop = 0f, bRight = 100f, bBottom = 100f
        )
        assertEquals(0.0f, iou, 0.001f)
    }

    @Test
    fun calculateIoU_zeroAreaBox_returnsZero() {
        val iou = GeometryUtils.calculateIoU(
            aLeft = 10f, aTop = 10f, aRight = 10f, aBottom = 10f,
            bLeft = 0f, bTop = 0f, bRight = 100f, bBottom = 100f
        )
        assertEquals(0.0f, iou, 0.001f)
    }

    @Test
    fun calculateIoU_invertedCoordinates_returnsZero() {
        // Invalid box where left > right or top > bottom
        val iou = GeometryUtils.calculateIoU(
            aLeft = 100f, aTop = 100f, aRight = 0f, aBottom = 0f,
            bLeft = 0f, bTop = 0f, bRight = 100f, bBottom = 100f
        )
        assertEquals(0.0f, iou, 0.001f)
    }

    @Test
    fun areCentersClose_withinThreshold_returnsTrue() {
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

    @Test
    fun areCentersClose_zeroDistanceIdenticalCenters_returnsTrue() {
        val close = GeometryUtils.areCentersClose(
            aLeft = 0.2f, aTop = 0.2f, aRight = 0.4f, aBottom = 0.4f,
            bLeft = 0.2f, bTop = 0.2f, bRight = 0.4f, bBottom = 0.4f
        )
        assertTrue(close)
    }

    @Test
    fun areCentersClose_exactThresholdBoundary_returnsFalse() {
        // Centers: A(0.5, 0.5), B(0.7, 0.5) -> dx = 0.2f, threshold = 0.2f (< threshold expects strictly false)
        val close = GeometryUtils.areCentersClose(
            aLeft = 0.4f, aTop = 0.4f, aRight = 0.6f, aBottom = 0.6f,
            bLeft = 0.6f, bTop = 0.4f, bRight = 0.8f, bBottom = 0.6f,
            maxHorizontalDistance = 0.2f,
            maxVerticalDistance = 0.2f
        )
        assertFalse(close)
    }
}
