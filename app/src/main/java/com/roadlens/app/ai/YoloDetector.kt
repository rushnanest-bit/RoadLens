package com.roadlens.app.ai

import android.content.Context
import android.graphics.Bitmap
import com.roadlens.app.domain.model.DetectionResult
import com.roadlens.app.domain.model.RoadDetection

/**
 * Backwards-compatible adapter delegating to [OnnxYoloDetector].
 * Adheres to SRP by reusing the decoupled preprocessor, postprocessor, and ONNX engine.
 */
class YoloDetector(context: Context) : RoadDefectDetector {

    companion object {
        const val INPUT_SIZE = YoloConstants.INPUT_SIZE
        val CLASS_NAMES = YoloConstants.CLASS_NAMES
        val CLASS_COLORS = YoloConstants.CLASS_COLORS
    }

    private val delegate = OnnxYoloDetector(context)

    override fun detectOnly(bitmap: Bitmap): List<RoadDetection> {
        return delegate.detectOnly(bitmap)
    }

    override fun detect(bitmap: Bitmap): DetectionResult {
        return delegate.detect(bitmap)
    }

    override fun close() {
        delegate.close()
    }
}
