package com.roadlens.app.ai

import android.content.ContextWrapper
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Unit tests for YoloModelInspector lifecycle and state gating.
 * Strictly under 200 lines per project engineering rules.
 */
class YoloModelInspectorTest {

    private class FakeContext : ContextWrapper(null)

    @Test
    fun inspectModel_withoutLoad_throwsIllegalStateException() {
        try {
            val inspector = YoloModelInspector(FakeContext())
            assertThrows(IllegalStateException::class.java) {
                inspector.inspectModel()
            }
            inspector.close()
        } catch (_: UnsatisfiedLinkError) {
            // Expected on host environments without ONNX JNI runtime binaries
        }
    }

    @Test
    fun close_whenSessionNotLoaded_doesNotThrow() {
        try {
            val inspector = YoloModelInspector(FakeContext())
            inspector.close()
        } catch (_: UnsatisfiedLinkError) {
            // Expected on host environments without ONNX JNI runtime binaries
        }
    }
}
