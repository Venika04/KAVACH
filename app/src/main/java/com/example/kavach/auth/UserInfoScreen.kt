package com.example.kavach.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun UserInfoScreen(
    navController: NavHostController,
    userId: String
) {
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter your name", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    isLoading = true
                    val db = FirebaseFirestore.getInstance()
                    val userDocRef = db.collection("users").document(userId)

                    // Use set with merge to create or update name field safely
                    userDocRef.set(mapOf("name" to name), SetOptions.merge())
                        .addOnSuccessListener {
                            isLoading = false
                            Toast.makeText(context, "Name saved!", Toast.LENGTH_SHORT).show()
                            navController.navigate("main") {
                                popUpTo("user_info") { inclusive = true }
                            }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Failed to save name.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Saving..." else "Continue")
        }
    }
}
