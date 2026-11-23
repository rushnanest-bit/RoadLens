package com.roadlens.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modal dialog prompting the user to enter and register a new fleet vehicle.
 * Strictly under 200 lines per project engineering rules.
 */
@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onVehicleRegistered: (regNumber: String) -> Unit
) {
    var regNumber by remember { mutableStateOf("") }
    val brandGreen = Color(0xFF1E3A2F)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Register New Vehicle", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = brandGreen)
        },
        text = {
            Column {
                Text(
                    text = "Enter the vehicle registration number to attach to all upcoming road inspection sessions.",
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563)
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = regNumber,
                    onValueChange = { regNumber = it.uppercase() },
                    label = { Text("Registration Number") },
                    placeholder = { Text("e.g. KA-01-AB-1234") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (regNumber.isNotBlank()) {
                        onVehicleRegistered(regNumber.trim())
                    }
                },
                enabled = regNumber.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = brandGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Register Vehicle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF6B7280))
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
