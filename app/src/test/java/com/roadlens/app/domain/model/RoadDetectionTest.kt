package com.roadlens.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for RoadDetection domain entity and normalized coordinate calculations.
 * Strictly under 200 lines per project engineering rules.
 * Covers positive, negative, and boundary-condition scenarios (KPI 4.7).
 */
class RoadDetectionTest {

    @Test
    fun normalizedCoordinates_standardDimensions_calculatesCorrectly() {
        val detection = RoadDetection(
            classIndex = 3,
            className = "Pothole (D40)",
            confidence = 0.90f,
            left = 160f,
            top = 120f,
            right = 320f,
            bottom = 240f,
            imageWidth = 640,
            imageHeight = 480
        )

        assertEquals(0.25f, detection.normLeft, 0.001f)
        assertEquals(0.25f, detection.normTop, 0.001f)
        assertEquals(0.50f, detection.normRight, 0.001f)
        assertEquals(0.50f, detection.normBottom, 0.001f)
    }

    @Test
    fun normalizedCoordinates_zeroWidthAndHeight_fallbackToZero() {
        // Boundary / negative test: zero dimensions must not throw ArithmeticException (/ by 0)
        val detection = RoadDetection(
            classIndex = 0,
            className = "Longitudinal Crack (D00)",
            confidence = 0.80f,
            left = 50f,
            top = 50f,
            right = 100f,
            bottom = 100f,
            imageWidth = 0,
            imageHeight = 0
        )

        assertEquals(0f, detection.normLeft, 0.001f)
        assertEquals(0f, detection.normTop, 0.001f)
        assertEquals(0f, detection.normRight, 0.001f)
        assertEquals(0f, detection.normBottom, 0.001f)
    }

    @Test
    fun normalizedCoordinates_fullImageCoverage_returnsOne() {
        // Boundary test: full image bounding box
        val detection = RoadDetection(
            classIndex = 1,
            className = "Transverse Crack (D10)",
            confidence = 0.95f,
            left = 0f,
            top = 0f,
            right = 640f,
            bottom = 480f,
            imageWidth = 640,
            imageHeight = 480
        )

        assertEquals(0f, detection.normLeft, 0.001f)
        assertEquals(0f, detection.normTop, 0.001f)
        assertEquals(1f, detection.normRight, 0.001f)
        assertEquals(1f, detection.normBottom, 0.001f)
    }

    @Test
    fun normalizedCoordinates_negativeCoordinates_returnsNegativeRatio() {
        // Negative test: out-of-bounds coordinates handled mathematically without crash
        val detection = RoadDetection(
            classIndex = 2,
            className = "Alligator Crack (D20)",
            confidence = 0.70f,
            left = -64f,
            top = -48f,
            right = 100f,
            bottom = 100f,
            imageWidth = 640,
            imageHeight = 480
        )

        assertEquals(-0.1f, detection.normLeft, 0.001f)
        assertEquals(-0.1f, detection.normTop, 0.001f)
    }
}
