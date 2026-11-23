package com.roadlens.app.ai

import android.graphics.Color

/**
 * Constants and hyperparameters for YOLO11n road damage perception.
 */
object YoloConstants {
    /** Model input dimension (640x640 pixels). */
    const val INPUT_SIZE = 640

    /** Number of spatial prediction candidates output by YOLO11 (at 640x640). */
    const val CANDIDATE_COUNT = 8400

    /** Total number of detected road defect classes. */
    const val CLASS_COUNT = 4

    /** Minimum confidence probability to consider a detection candidate. */
    const val CONFIDENCE_THRESHOLD = 0.25f

    /** Intersection-over-Union threshold for Non-Maximum Suppression (NMS). */
    const val IOU_THRESHOLD = 0.45f

    /** Standard road distress classification labels (Japan Road Maintenance / RDD standard). */
    val CLASS_NAMES = listOf(
        "Longitudinal Crack (D00)",
        "Transverse Crack (D10)",
        "Alligator Crack (D20)",
        "Pothole (D40)"
    )

    /** Distinctive high-contrast visual colors for overlaying defect bounding boxes. */
    val CLASS_COLORS = listOf(
        Color.rgb(56, 189, 248),  // D00 - Sky Blue
        Color.rgb(192, 132, 252), // D10 - Purple
        Color.rgb(74, 222, 128),  // D20 - Green
        Color.rgb(248, 113, 113)  // D40 - Coral Red
    )
}
