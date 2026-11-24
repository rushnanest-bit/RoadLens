package com.roadlens.app.domain.util

import android.Manifest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for PermissionHelper constants and definitions.
 * Strictly under 100 lines per project engineering rules.
 */
class PermissionHelperTest {

    @Test
    fun requiredScanPermissions_containsCameraAndLocation() {
        val permissions = PermissionHelper.REQUIRED_SCAN_PERMISSIONS.toList()
        assertEquals(3, permissions.size)
        assertTrue(permissions.contains(Manifest.permission.CAMERA))
        assertTrue(permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue(permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
    }
}
