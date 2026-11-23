package com.roadlens.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Dialog prompting for destination / stop location when ending a live scan session.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun StopScanDialog(
    totalDetections: Int,
    onDismiss: () -> Unit,
    onConfirm: (stopLocation: String) -> Unit
) {
    var locationInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val brandGreen = Color(0xFF1E3A2F)
    val dangerRed = Color(0xFFDC2626)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Complete Scan Session", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = brandGreen)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Recorded $totalDetections road defect(s) during this route. Enter the ending destination name to archive this session.",
                    fontSize = 13.sp, color = Color(0xFF6B7280)
                )

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it; isError = false },
                    label = { Text("Stop Location Name") },
                    placeholder = { Text("Central Terminal / Indiranagar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Spacer(Modifier.height(6.dp))
                    Text("Please enter a destination or stop location name.", color = dangerRed, fontSize = 12.sp)
                }

                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF6B7280), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanLoc = locationInput.trim()
                            if (cleanLoc.isBlank()) {
                                isError = true
                            } else {
                                onConfirm(cleanLoc)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = dangerRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Finish & Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
