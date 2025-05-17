package com.example.kavach

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import android.location.Location
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation(
    navController: NavHostController,
    getLocation: () -> android.location.Location?,
    sendSOS: (android.location.Location?) -> Unit,
    auth: FirebaseAuth
) {
    val contactViewModel: ContactViewModel = viewModel()
    val isUserLoggedIn = auth.currentUser != null

    // Decide the start destination based on auth status
    val startDestination = if (isUserLoggedIn) "main" else "auth"
    var authSuccess by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    // Automatically navigate to MainScreen if already logged in
    val currentUser = auth.currentUser
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate("main") {
                popUpTo("auth") { inclusive = true }
            }
        }
    }

    // Navigate after login/signup success
    LaunchedEffect(authSuccess) {
        if (authSuccess) {
            navController.navigate("main") {
                popUpTo("auth") { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("auth") {
            var isSignUp by remember { mutableStateOf(false) }
            val context = LocalContext.current

            AuthScreen(
                isSignUp = isSignUp,
                onAuthSubmit = { email, password ->
                    if (isSignUp) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("user_info") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                } else {
                                    // Show error (e.g., account already exists)
                                    println("Signup failed: ${task.exception?.message}")
                                }
                            }
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    val db = FirebaseFirestore.getInstance()

//                                    val user = hashMapOf(
//                                        "name" to ""
//                                    )

                                    if (userId != null) {
                                        db.collection("users").document(userId)
                                            .get()
                                            .addOnSuccessListener { document ->
                                                val userName = document.getString("name")
                                                if(userName.isNullOrBlank()) {
                                                    navController.navigate("userinfo") {
                                                        popUpTo("auth") { inclusive = true}
                                                    }
                                                } else {
                                                    navController.navigate("main") {
                                                        popUpTo("auth") { inclusive = true }
                                                    }
                                                }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Failed to save user info", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }else {
                                    // Login failed – maybe account doesn't exist
                                    val errorMessage = task.exception?.message ?: "Login failed"
                                    println("Login failed: $errorMessage")

                                    // Switch to SignUp mode with notification
                                    isSignUp = true
                                    Toast.makeText(
                                        navController.context,
                                        "User not found. Please sign up.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                },

                onToggleMode = {
                    isSignUp = !isSignUp
                },
                onGoogleSignIn = {
                    // We'll integrate actual Google sign-in intent in MainActivity later
                }
            )
        }

        composable("main") {
            MainScreen(
                navController = navController,
                getLocation = getLocation,
                sendSOS = sendSOS,
                contactViewModel = contactViewModel
            )
        }

        composable("profile") {
            ProfileScreen(navController)
        }

        composable("add_contact") {
            AddContactScreen(navController)
        }

        composable("user_info") {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                UserInfoScreen(navController, userId)
            }
        }
    }
}
