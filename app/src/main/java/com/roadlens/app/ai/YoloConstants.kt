package com.roadlens.app.ai

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

    /** Distinctive high-contrast visual ARGB color integers for overlaying defect bounding boxes. */
    val CLASS_COLORS = listOf(
        0xFF38BDF8.toInt(), // D00 - Sky Blue
        0xFFC084FC.toInt(), // D10 - Purple
        0xFF4ADE80.toInt(), // D20 - Green
        0xFFF87171.toInt()  // D40 - Coral Red
    )
}
