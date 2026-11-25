package com.roadlens.app.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Permission requirement dialog informing users of camera and GPS necessity.
 * Strictly under 100 lines per project engineering rules.
 */
@Composable
fun PermissionRequiredDialog(
    isSettingsMode: Boolean = false,
    onDismiss: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val brandNavy = Color(0xFF0B1F3A)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isSettingsMode) "Permissions Disabled" else "Permissions Required",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = brandNavy
            )
        },
        text = {
            Text(
                if (isSettingsMode) {
                    "Camera and Location permissions are disabled in system settings. Tap 'Open Settings', then select 'Permissions' to allow access."
                } else {
                    "Camera and Location access are required to start scanning roads and geo-tag hazards in real time. Please grant these permissions to continue."
                },
                fontSize = 14.sp,
                color = Color(0xFF4B5563),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    if (isSettingsMode) onOpenSettings() else onRequestPermissions()
                },
                colors = ButtonDefaults.buttonColors(containerColor = brandNavy),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    if (isSettingsMode) "Open Settings" else "Grant Access",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF6B7280))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
