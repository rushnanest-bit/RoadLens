package com.roadlens.app.ai

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import com.roadlens.app.ai.YoloConstants.CANDIDATE_COUNT
import com.roadlens.app.ai.YoloConstants.CLASS_COUNT
import com.roadlens.app.ai.YoloConstants.INPUT_SIZE
import com.roadlens.app.domain.model.DetectionResult
import com.roadlens.app.domain.model.RoadDetection
import java.nio.FloatBuffer

/**
 * Concrete implementation of [RoadDefectDetector] using Microsoft ONNX Runtime Android.
 * Orchestrates preprocessing, model execution, and postprocessing.
 */
class OnnxYoloDetector(context: Context) : RoadDefectDetector {

    private val environment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession?
    private var isLoaded: Boolean = false

    private val preprocessor = YoloPreprocessor()
    private val postprocessor = YoloPostprocessor()
    private val annotationDrawer = YoloAnnotationDrawer()

    private val reusablePredictions = FloatArray((CLASS_COUNT + 4) * CANDIDATE_COUNT)
    private val inferenceLock = Any()

    init {
        var initializedSession: OrtSession? = null
        try {
            val modelBytes = context.assets.open("yolo11n.onnx").use { it.readBytes() }
            val options = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(2)
            }
            initializedSession = environment.createSession(modelBytes, options)
            options.close()
            isLoaded = true
        } catch (_: Exception) {
            initializedSession = null
            isLoaded = false
        }
        session = initializedSession
    }

    override fun isModelLoaded(): Boolean = isLoaded && session != null

    override fun detectOnly(bitmap: Bitmap): List<RoadDetection> = synchronized(inferenceLock) {
        if (!isModelLoaded()) return emptyList()
        val inputData = preprocessor.preprocessZeroAlloc(bitmap)
        val predictions = runInference(inputData) ?: return emptyList()
        return postprocessor.processPredictions(predictions, bitmap.width, bitmap.height)
    }

    override fun detect(bitmap: Bitmap): DetectionResult {
        val detections = detectOnly(bitmap)
        val annotated = annotationDrawer.drawDetections(bitmap, detections)
        return DetectionResult(annotatedBitmap = annotated, detections = detections)
    }

    private fun runInference(inputData: FloatArray): FloatArray? {
        val currentSession = session ?: return null
        val tensor = OnnxTensor.createTensor(
            environment,
            FloatBuffer.wrap(inputData),
            longArrayOf(1, 3, INPUT_SIZE.toLong(), INPUT_SIZE.toLong())
        )

        try {
            val results = currentSession.run(mapOf(currentSession.inputNames.first() to tensor))
            try {
                @Suppress("UNCHECKED_CAST")
                val output = results[0].value as Array<Array<FloatArray>>
                val predictions = output[0]

                for (channel in 0 until (CLASS_COUNT + 4)) {
                    predictions[channel].copyInto(
                        reusablePredictions,
                        channel * CANDIDATE_COUNT
                    )
                }

                return reusablePredictions
            } finally {
                results.close()
            }
        } catch (_: Exception) {
            return null
        } finally {
            tensor.close()
        }
    }

    override fun close() {
        session?.close()
    }
}
