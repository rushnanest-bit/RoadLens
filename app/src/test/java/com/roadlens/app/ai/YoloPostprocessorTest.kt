package com.roadlens.app.ai

import com.roadlens.app.ai.YoloConstants.CANDIDATE_COUNT
import com.roadlens.app.ai.YoloConstants.CLASS_COUNT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for YOLO postprocessing, coordinate un-letterboxing, and NMS suppression.
 * Strictly under 200 lines per project engineering rules.
 * Covers positive, negative, and boundary-condition scenarios (KPI 4.7).
 */
class YoloPostprocessorTest {

    private lateinit var postprocessor: YoloPostprocessor
    private val totalSize = (CLASS_COUNT + 4) * CANDIDATE_COUNT

    @Before
    fun setUp() {
        postprocessor = YoloPostprocessor()
    }

    private fun createEmptyPredictions(): FloatArray = FloatArray(totalSize)

    @Test
    fun processPredictions_noCandidateExceedsThreshold_returnsEmptyList() {
        // Negative scenario: All detection scores below 0.25 threshold
        val predictions = createEmptyPredictions()
        for (i in 0 until CANDIDATE_COUNT) {
            predictions[4 * CANDIDATE_COUNT + i] = 0.10f
        }

        val results = postprocessor.processPredictions(predictions, 640, 640)
        assertTrue(results.isEmpty())
    }

    @Test
    fun processPredictions_singleValidCandidate_extractsCorrectDefect() {
        // Positive scenario: Candidate 0 is a Pothole (classIndex 3) with score 0.88f
        val predictions = createEmptyPredictions()
        val i = 0
        predictions[i] = 320f                         // centerX
        predictions[CANDIDATE_COUNT + i] = 320f       // centerY
        predictions[2 * CANDIDATE_COUNT + i] = 100f   // boxWidth
        predictions[3 * CANDIDATE_COUNT + i] = 80f    // boxHeight
        predictions[(3 + 4) * CANDIDATE_COUNT + i] = 0.88f // Pothole (D40) score

        val results = postprocessor.processPredictions(predictions, 640, 640)

        assertEquals(1, results.size)
        val defect = results.first()
        assertEquals(3, defect.classIndex)
        assertEquals("Pothole (D40)", defect.className)
        assertEquals(0.88f, defect.confidence, 0.001f)
        assertEquals(270f, defect.left, 1.0f)
        assertEquals(280f, defect.top, 1.0f)
        assertEquals(370f, defect.right, 1.0f)
        assertEquals(360f, defect.bottom, 1.0f)
    }

    @Test
    fun processPredictions_overlappingSameClass_suppressesLowerConfidenceViaNms() {
        // Positive scenario for NMS: Two overlapping boxes of class 3 (Pothole)
        val predictions = createEmptyPredictions()

        // Candidate 0: High confidence
        predictions[0] = 300f
        predictions[CANDIDATE_COUNT + 0] = 300f
        predictions[2 * CANDIDATE_COUNT + 0] = 100f
        predictions[3 * CANDIDATE_COUNT + 0] = 100f
        predictions[(3 + 4) * CANDIDATE_COUNT + 0] = 0.95f

        // Candidate 1: Lower confidence overlapping almost identical coordinates
        predictions[1] = 302f
        predictions[CANDIDATE_COUNT + 1] = 302f
        predictions[2 * CANDIDATE_COUNT + 1] = 100f
        predictions[3 * CANDIDATE_COUNT + 1] = 100f
        predictions[(3 + 4) * CANDIDATE_COUNT + 1] = 0.70f

        val results = postprocessor.processPredictions(predictions, 640, 640)

        // Lower score box must be suppressed by NMS
        assertEquals(1, results.size)
        assertEquals(0.95f, results.first().confidence, 0.001f)
    }

    @Test
    fun processPredictions_overlappingDifferentClasses_retainsBoth() {
        // Positive scenario: Candidate 0 (Pothole) and Candidate 1 (Crack) overlap at same location
        val predictions = createEmptyPredictions()

        // Candidate 0: Pothole (class 3)
        predictions[0] = 200f
        predictions[CANDIDATE_COUNT + 0] = 200f
        predictions[2 * CANDIDATE_COUNT + 0] = 80f
        predictions[3 * CANDIDATE_COUNT + 0] = 80f
        predictions[(3 + 4) * CANDIDATE_COUNT + 0] = 0.90f

        // Candidate 1: Longitudinal Crack (class 0)
        predictions[1] = 200f
        predictions[CANDIDATE_COUNT + 1] = 200f
        predictions[2 * CANDIDATE_COUNT + 1] = 80f
        predictions[3 * CANDIDATE_COUNT + 1] = 80f
        predictions[(0 + 4) * CANDIDATE_COUNT + 1] = 0.85f

        val results = postprocessor.processPredictions(predictions, 640, 640)

        // NMS is class-specific; both must be retained
        assertEquals(2, results.size)
    }

    @Test
    fun processPredictions_outOfBoundsCoordinates_clipsToImageBoundary() {
        // Boundary scenario: Box center at boundary with width extending beyond canvas
        val predictions = createEmptyPredictions()
        val i = 5
        predictions[i] = 10f
        predictions[CANDIDATE_COUNT + i] = 10f
        predictions[2 * CANDIDATE_COUNT + i] = 100f // Extends to negative left (-40)
        predictions[3 * CANDIDATE_COUNT + i] = 100f // Extends to negative top (-40)
        predictions[(1 + 4) * CANDIDATE_COUNT + i] = 0.80f

        val results = postprocessor.processPredictions(predictions, 640, 640)

        assertEquals(1, results.size)
        val defect = results.first()
        assertTrue(defect.left >= 0f)
        assertTrue(defect.top >= 0f)
    }

    @Test
    fun processPredictions_degenerateBox_discardsZeroOrNegativeArea() {
        // Boundary/Negative scenario: Width = 0, Height = 0
        val predictions = createEmptyPredictions()
        val i = 10
        predictions[i] = 200f
        predictions[CANDIDATE_COUNT + i] = 200f
        predictions[2 * CANDIDATE_COUNT + i] = 0f // 0 width
        predictions[3 * CANDIDATE_COUNT + i] = 0f // 0 height
        predictions[(2 + 4) * CANDIDATE_COUNT + i] = 0.90f

        val results = postprocessor.processPredictions(predictions, 640, 640)
        assertTrue(results.isEmpty())
    }
}
