package com.example.kavach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onSosClick: () -> Unit,
    isPanicMode: Boolean,
    onPanicToggle: (Boolean) -> Unit,
    currentLocation: String
) {
    val backgroundColor = if (isPanicMode) Color.Red else Color(0xFF121212)
    val sosButtonColor = if (isPanicMode) Color.White else Color.Red

    Surface (
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (isPanicMode) "Panic Mode ON" else "Panic Mode OFF",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSosClick,
                colors = ButtonDefaults.buttonColors(containerColor = sosButtonColor),
                shape = CircleShape,
                modifier = Modifier
                    .size(150.dp)
            ) {
                Text(
                    text = "SOS",
                    color = if (isPanicMode) Color.Red else Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Panic Mode",
                    color = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Switch(
                    checked = isPanicMode,
                    onCheckedChange = { onPanicToggle(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Red)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Your Location:",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = currentLocation,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

