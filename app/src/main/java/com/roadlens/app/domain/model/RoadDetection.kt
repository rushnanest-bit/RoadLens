package com.roadlens.app.domain.model

/**
 * Represents a single detected road defect identified by the AI perception engine.
 *
 * @property classIndex The numerical index of the detected class (0: D00, 1: D10, 2: D20, 3: D40).
 * @property className Human-readable defect label (e.g., "Pothole (D40)").
 * @property confidence Detection probability score between 0.0 and 1.0.
 * @property left Bounding box left boundary in source image pixel coordinates.
 * @property top Bounding box top boundary in source image pixel coordinates.
 * @property right Bounding box right boundary in source image pixel coordinates.
 * @property bottom Bounding box bottom boundary in source image pixel coordinates.
 * @property imageWidth Source image width in pixels, used for coordinate normalization.
 * @property imageHeight Source image height in pixels, used for coordinate normalization.
 */
data class RoadDetection(
    val classIndex: Int,
    val className: String,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val imageWidth: Int = 1,
    val imageHeight: Int = 1
) {
    /** Left coordinate normalized to [0.0, 1.0] relative to image width. */
    val normLeft: Float get() = if (imageWidth > 0) left / imageWidth else 0f

    /** Top coordinate normalized to [0.0, 1.0] relative to image height. */
    val normTop: Float get() = if (imageHeight > 0) top / imageHeight else 0f

    /** Right coordinate normalized to [0.0, 1.0] relative to image width. */
    val normRight: Float get() = if (imageWidth > 0) right / imageWidth else 0f

    /** Bottom coordinate normalized to [0.0, 1.0] relative to image height. */
    val normBottom: Float get() = if (imageHeight > 0) bottom / imageHeight else 0f
}
