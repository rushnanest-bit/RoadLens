package com.roadlens.app.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.core.app.ActivityCompat

/**
 * Enterprise permission requirement dialog informing users of camera and GPS necessity.
 * Strictly under 100 lines per project engineering rules.
 */
@Composable
fun PermissionRequiredDialog(
    context: Context,
    onDismiss: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    val brandGreen = Color(0xFF1E3A2F)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Permissions Required", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = brandGreen)
        },
        text = {
            Text(
                "Camera and Location access are required to start scanning roads and geo-tag hazards in real time. Please grant these permissions to continue.",
                fontSize = 14.sp,
                color = Color(0xFF4B5563),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    val activity = context as? Activity
                    val cameraDenied = activity?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
                    } ?: true
                    val locationDenied = activity?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)
                    } ?: true

                    // If permanently denied (Don't ask again), direct to App Settings
                    if (!cameraDenied && !locationDenied) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        onRequestPermissions()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = brandGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Grant Access", color = Color.White, fontWeight = FontWeight.SemiBold)
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
