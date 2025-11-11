package com.example.kavach.guardian

import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.kavach.guardian.GuardianPINManager

@Composable
fun GuardianPinDialog(
    onDismiss: () -> Unit,
    onPinValidated: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Guardian PIN") },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("4-digit PIN") },
                visualTransformation = PasswordVisualTransformation()
            )
        },
        confirmButton = {
            Button(onClick = {
                if (GuardianPINManager.validatePIN(context, pin)) {
                    Toast.makeText(context, "SOS Cancelled", Toast.LENGTH_SHORT).show()
                    onPinValidated()
                    onDismiss()
                } else {
                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
