
package com.roadlens.app.ai

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import android.content.Context

class YoloModelInspector(private val context: Context) {

    private val environment = OrtEnvironment.getEnvironment()

    private var session: OrtSession? = null

    fun loadModel() {
        // Read the ONNX model from Android assets
        val modelBytes = context.assets
            .open("yolo11n.onnx")
            .use { it.readBytes() }

        // Create ONNX Runtime session
        val options = OrtSession.SessionOptions()

        try {
            session = environment.createSession(
                modelBytes,
                options
            )
        } finally {
            options.close()
        }
    }

    fun inspectModel(): String {

        val currentSession = session
            ?: throw IllegalStateException("Model not loaded")

        val report = StringBuilder()

        report.appendLine("YOLO MODEL INFORMATION")
        report.appendLine("----------------------")

        report.appendLine("\nINPUT TENSORS")

        for ((name, node) in currentSession.inputInfo) {
            val tensorInfo = node.info as TensorInfo

            report.appendLine("Name: $name")
            report.appendLine("Shape: ${tensorInfo.shape.contentToString()}")
            report.appendLine("Type: ${tensorInfo.type}")
        }

        report.appendLine("\nOUTPUT TENSORS")

        for ((name, node) in currentSession.outputInfo) {
            val tensorInfo = node.info as TensorInfo

            report.appendLine("Name: $name")
            report.appendLine("Shape: ${tensorInfo.shape.contentToString()}")
            report.appendLine("Type: ${tensorInfo.type}")
        }

        return report.toString()
    }

    fun close() {
        session?.close()
        session = null
    }
}
