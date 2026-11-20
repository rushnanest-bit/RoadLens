package com.roadlens.app.ai

import com.roadlens.app.ai.YoloConstants.CANDIDATE_COUNT
import com.roadlens.app.ai.YoloConstants.CLASS_COUNT
import com.roadlens.app.ai.YoloConstants.CLASS_NAMES
import com.roadlens.app.ai.YoloConstants.CONFIDENCE_THRESHOLD
import com.roadlens.app.ai.YoloConstants.INPUT_SIZE
import com.roadlens.app.ai.YoloConstants.IOU_THRESHOLD
import com.roadlens.app.domain.model.RoadDetection
import com.roadlens.app.domain.util.GeometryUtils
import kotlin.math.min

/**
 * Handles YOLO11 output tensor parsing, coordinate un-letterboxing, and Non-Maximum Suppression (NMS).
 */
class YoloPostprocessor {

    /**
     * Parses raw flattened prediction floats from the model, maps them back to original bitmap
     * coordinates, and applies Non-Maximum Suppression to remove overlapping boxes.
     */
    fun processPredictions(
        predictions: FloatArray,
        imageWidth: Int,
        imageHeight: Int
    ): List<RoadDetection> {
        val scale = min(
            INPUT_SIZE.toFloat() / imageWidth,
            INPUT_SIZE.toFloat() / imageHeight
        )
        val resizedW = imageWidth * scale
        val resizedH = imageHeight * scale
        val padX = (INPUT_SIZE - resizedW) / 2f
        val padY = (INPUT_SIZE - resizedH) / 2f

        val candidates = mutableListOf<RoadDetection>()

        for (i in 0 until CANDIDATE_COUNT) {
            var bestClass = -1
            var bestScore = 0f

            for (classIndex in 0 until CLASS_COUNT) {
                val score = predictions[(classIndex + 4) * CANDIDATE_COUNT + i]
                if (score > bestScore) {
                    bestScore = score
                    bestClass = classIndex
                }
            }

            if (bestClass == -1 || bestScore < CONFIDENCE_THRESHOLD) {
                continue
            }

            val centerX = predictions[i]
            val centerY = predictions[CANDIDATE_COUNT + i]
            val boxWidth = predictions[2 * CANDIDATE_COUNT + i]
            val boxHeight = predictions[3 * CANDIDATE_COUNT + i]

            val left = (centerX - boxWidth / 2f - padX) / scale
            val top = (centerY - boxHeight / 2f - padY) / scale
            val right = (centerX + boxWidth / 2f - padX) / scale
            val bottom = (centerY + boxHeight / 2f - padY) / scale

            val clippedLeft = left.coerceIn(0f, imageWidth.toFloat())
            val clippedTop = top.coerceIn(0f, imageHeight.toFloat())
            val clippedRight = right.coerceIn(0f, imageWidth.toFloat())
            val clippedBottom = bottom.coerceIn(0f, imageHeight.toFloat())

            if (clippedRight <= clippedLeft || clippedBottom <= clippedTop) {
                continue
            }

            candidates.add(
                RoadDetection(
                    classIndex = bestClass,
                    className = CLASS_NAMES[bestClass],
                    confidence = bestScore,
                    left = clippedLeft,
                    top = clippedTop,
                    right = clippedRight,
                    bottom = clippedBottom,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight
                )
            )
        }

        return applyNms(candidates)
    }

    private fun applyNms(detections: List<RoadDetection>): List<RoadDetection> {
        val sorted = detections.sortedByDescending { it.confidence }
        val kept = mutableListOf<RoadDetection>()

        for (candidate in sorted) {
            val overlaps = kept.any { existing ->
                existing.classIndex == candidate.classIndex &&
                        GeometryUtils.calculateIoU(
                            existing.left, existing.top, existing.right, existing.bottom,
                            candidate.left, candidate.top, candidate.right, candidate.bottom
                        ) > IOU_THRESHOLD
            }

            if (!overlaps) {
                kept.add(candidate)
            }
        }

        return kept
    }
}
