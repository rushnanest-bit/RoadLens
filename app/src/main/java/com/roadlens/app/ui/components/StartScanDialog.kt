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
 * Dialog prompting for vehicle registration and start location before initiating live scan.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun StartScanDialog(
    initialVehicle: String,
    onDismiss: () -> Unit,
    onConfirm: (vehicleRegistration: String, startLocation: String) -> Unit
) {
    var vehicleInput by remember { mutableStateOf(initialVehicle) }
    var locationInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val brandNavy = Color(0xFF0B1F3A)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Start Live Fleet Scan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = brandNavy)
                Spacer(Modifier.height(6.dp))
                Text("Enter vehicle and starting location details to track this inspection session.", fontSize = 13.sp, color = Color(0xFF6B7280))

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = vehicleInput,
                    onValueChange = { vehicleInput = it.uppercase(); isError = false },
                    label = { Text("Vehicle Registration") },
                    placeholder = { Text("KA-01-AB-1234") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it; isError = false },
                    label = { Text("Start Location Name") },
                    placeholder = { Text("Depot North / MG Road") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Spacer(Modifier.height(6.dp))
                    Text("Please enter both vehicle registration and start location.", color = Color(0xFFDC2626), fontSize = 12.sp)
                }

                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF6B7280), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanVeh = vehicleInput.trim()
                            val cleanLoc = locationInput.trim()
                            if (cleanVeh.isBlank() || cleanLoc.isBlank()) {
                                isError = true
                            } else {
                                onConfirm(cleanVeh, cleanLoc)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = brandNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Start Scan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
