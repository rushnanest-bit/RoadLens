package com.roadlens.app.ai

import android.graphics.Bitmap
import com.roadlens.app.domain.model.DetectionResult
import com.roadlens.app.domain.model.RoadDetection
import java.io.Closeable

typealias RoadDetection = com.roadlens.app.domain.model.RoadDetection
typealias DetectionResult = com.roadlens.app.domain.model.DetectionResult

/**
 * Common abstraction for computer vision road damage perception engines.
 * Enables adherence to the Open/Closed (OCP) and Liskov Substitution (LSP) principles,
 * allowing the app to seamlessly switch between ONNX, LiteRT, or Mock implementations.
 */
interface RoadDefectDetector : Closeable {

    /**
     * Executes fast inference on a single image frame, returning only bounding box detections.
     * Ideal for real-time video streaming where canvas allocation overhead must be avoided.
     *
     * @param bitmap Input bitmap frame from camera or video player.
     * @return List of detected road defects meeting the confidence threshold.
     */
    fun detectOnly(bitmap: Bitmap): List<RoadDetection>

    /**
     * Executes inference and draws bounding boxes directly onto an annotated copy of the bitmap.
     * Typically used for single image inspection workflows.
     *
     * @param bitmap Input bitmap to inspect and annotate.
     * @return [DetectionResult] containing the annotated image and detection metadata.
     */
    fun detect(bitmap: Bitmap): DetectionResult
}
