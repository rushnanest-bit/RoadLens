package com.roadlens.app.domain.model

import android.graphics.Bitmap

/**
 * Encapsulates the output of a single static image detection pass.
 *
 * @property annotatedBitmap A copy of the source image with drawn bounding boxes and labels.
 * @property detections The list of detected road defects with their coordinates and confidence.
 */
data class DetectionResult(
    val annotatedBitmap: Bitmap,
    val detections: List<RoadDetection>
)
