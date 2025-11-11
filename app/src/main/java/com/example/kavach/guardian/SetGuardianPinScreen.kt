package com.example.kavach.guardian

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.kavach.guardian.GuardianPINManager

@Composable
fun SetGuardianPinScreen(
    onPinSet: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Set Guardian PIN", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("Enter 4-digit PIN") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPin,
            onValueChange = { confirmPin = it },
            label = { Text("Confirm PIN") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            if (pin.length != 4) {
                Toast.makeText(context, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
            } else if (pin != confirmPin) {
                Toast.makeText(context, "PINs do not match", Toast.LENGTH_SHORT).show()
            } else {
                GuardianPINManager.setPIN(context, pin)
                Toast.makeText(context, "Guardian PIN set successfully!", Toast.LENGTH_SHORT).show()
                onPinSet()
            }
        }) {
            Text("Save PIN")
        }
    }
}
