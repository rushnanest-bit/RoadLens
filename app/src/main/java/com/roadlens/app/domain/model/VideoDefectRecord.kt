package com.roadlens.app.domain.model

/**
 * Represents a unique road defect detected during continuous video or camera scanning,
 * anchored to a specific timeline timestamp with spatial coordinates.
 *
 * @property id Unique identifier for this recorded defect within the scanning session.
 * @property className Human-readable defect label.
 * @property classIndex The numerical class index of the defect.
 * @property confidence The highest detection confidence recorded for this defect.
 * @property timestampMs Video timestamp or session elapsed time in milliseconds.
 * @property timeFormatted Human-readable formatted time string (e.g. "01:24").
 * @property left Pixel left coordinate of the bounding box.
 * @property top Pixel top coordinate of the bounding box.
 * @property right Pixel right coordinate of the bounding box.
 * @property bottom Pixel bottom coordinate of the bounding box.
 * @property normLeft Normalized left coordinate in range [0.0, 1.0].
 * @property normTop Normalized top coordinate in range [0.0, 1.0].
 * @property normRight Normalized right coordinate in range [0.0, 1.0].
 * @property normBottom Normalized bottom coordinate in range [0.0, 1.0].
 */
data class VideoDefectRecord(
    val id: Int,
    val className: String,
    val classIndex: Int,
    val confidence: Float,
    val timestampMs: Long,
    val timeFormatted: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val normLeft: Float,
    val normTop: Float,
    val normRight: Float,
    val normBottom: Float
)
