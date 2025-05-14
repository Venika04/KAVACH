package com.example.kavach

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun UserInfoScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("KavachPrefs", Context.MODE_PRIVATE)

    var name by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter Your Name", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (name.isNotBlank()) {
                sharedPrefs.edit().putString("username", name).apply()
                navController.navigate("main") {
                    popUpTo("userinfo") { inclusive = true }
                }
            }
        }) {
            Text("Continue")
        }
    }
}
