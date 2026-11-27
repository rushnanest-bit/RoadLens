package com.roadlens.app.domain.util

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for PermissionHelper runtime permission checks and constants.
 * Strictly under 100 lines per project engineering rules.
 */
class PermissionHelperTest {

    private class FakePermissionContext(
        private val granted: Set<String>
    ) : ContextWrapper(null) {
        override fun checkPermission(permission: String, pid: Int, uid: Int): Int {
            return if (permission in granted) {
                PackageManager.PERMISSION_GRANTED
            } else {
                PackageManager.PERMISSION_DENIED
            }
        }
    }

    @Test
    fun requiredScanPermissions_containsCameraAndLocation() {
        val permissions = PermissionHelper.REQUIRED_SCAN_PERMISSIONS.toList()
        assertEquals(3, permissions.size)
        assertTrue(permissions.contains(Manifest.permission.CAMERA))
        assertTrue(permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue(permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @Test
    fun hasCameraPermission_whenGranted_returnsTrue() {
        val ctx = FakePermissionContext(setOf(Manifest.permission.CAMERA))
        assertTrue(PermissionHelper.hasCameraPermission(ctx))
    }

    @Test
    fun hasCameraPermission_whenDenied_returnsFalse() {
        val ctx = FakePermissionContext(emptySet())
        assertFalse(PermissionHelper.hasCameraPermission(ctx))
    }

    @Test
    fun hasLocationPermission_fineOrCoarse_returnsTrue() {
        val fineCtx = FakePermissionContext(setOf(Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue(PermissionHelper.hasLocationPermission(fineCtx))

        val coarseCtx = FakePermissionContext(setOf(Manifest.permission.ACCESS_COARSE_LOCATION))
        assertTrue(PermissionHelper.hasLocationPermission(coarseCtx))

        val noneCtx = FakePermissionContext(emptySet())
        assertFalse(PermissionHelper.hasLocationPermission(noneCtx))
    }

    @Test
    fun hasAllScanPermissions_requiresBothCameraAndLocation() {
        val allGranted = FakePermissionContext(
            setOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
        )
        assertTrue(PermissionHelper.hasAllScanPermissions(allGranted))

        val cameraOnly = FakePermissionContext(setOf(Manifest.permission.CAMERA))
        assertFalse(PermissionHelper.hasAllScanPermissions(cameraOnly))

        val locationOnly = FakePermissionContext(setOf(Manifest.permission.ACCESS_FINE_LOCATION))
        assertFalse(PermissionHelper.hasAllScanPermissions(locationOnly))
    }
}
