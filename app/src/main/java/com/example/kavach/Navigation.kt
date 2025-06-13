package com.example.kavach

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kavach.auth.AuthScreen
import com.example.kavach.auth.PhoneAuthHelper
import com.example.kavach.auth.UserInfoScreen
import com.example.kavach.contact.AddContactScreen
import com.example.kavach.contact.ContactViewModel
import com.example.kavach.main.MainScreen
import com.example.kavach.profile.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.example.kavach.auth.OTPVerificationScreen
import com.example.kavach.help.HelpScreen
import com.example.kavach.rating.RateLocationScreen


@Composable
fun AppNavigation(
    navController: NavHostController,
    getLocation: () -> android.location.Location?,
    sendSOS: (android.location.Location?) -> Unit,
    auth: FirebaseAuth,
    isLoggedIn: Boolean
) {
    val contactViewModel: ContactViewModel = viewModel()

    // Decide the start destination based on auth status
    val startDestination = if (isLoggedIn) "main" else "auth"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("auth") {
            var isSignUp by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val activity = context as Activity
            var hasNavigated by remember { mutableStateOf(false) }

            fun checkUserNameAndNavigate(userId: String) {
                if (hasNavigated) return
                hasNavigated = true
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        val userName = document.getString("name")
                        if (userName.isNullOrBlank()) {
                            navController.navigate("user_info") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            navController.navigate("main") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
                    }
            }

            AuthScreen(
                isSignUp = isSignUp,
                onEmailAuthSubmit = { email, password ->
                    if (isSignUp) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("user_info") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                } else {
                                    // Show error (e.g., account already exists)
                                    Toast.makeText(context,"Signup failed: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    val db = FirebaseFirestore.getInstance()

                                    if (userId != null) {
                                        checkUserNameAndNavigate(userId)
                                    }
                                } else {
                                    // Switch to SignUp mode with notification
                                    isSignUp = true
                                    Toast.makeText(context, "User not found. Please sign up.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                },
                onPhoneAuthSubmit = { phone ->
                    PhoneAuthHelper.startPhoneNumberVerification(
                        phoneNumber = phone,
                        activity = activity,
                        onCodeSent = { verificationId ->
                            // Navigate to OTP screen and pass verificationId
                            navController.navigate("otpVerification/$verificationId")
                        },
                        onVerificationCompleted = { credential ->
                            PhoneAuthHelper.signInWithPhoneAuthCredential(
                                credential,
                                onSuccess = { user ->
                                    if (user != null) {
                                        checkUserNameAndNavigate(user.uid)
                                    } else {
                                        Toast.makeText(activity, "Login failed: user is null", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onFailure = { error ->
                                    Toast.makeText(activity, error.message, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onVerificationFailed = { e ->
                            Toast.makeText(activity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                },

                onToggleMode = {
                    isSignUp = !isSignUp
                },
                navController = navController
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

        composable("otpVerification/{verificationId}") { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            val context = LocalContext.current
            val activity = context as Activity
            var hasNavigated by remember { mutableStateOf(false) }

            fun checkUserNameAndNavigate(userId: String) {
                if (hasNavigated) return
                hasNavigated = true
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        val userName = document.getString("name")
                        if (userName.isNullOrBlank()) {
                            navController.navigate("user_info") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            navController.navigate("main") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
                    }
            }

            OTPVerificationScreen(
                verificationId = verificationId,
                onVerifySuccess = {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        checkUserNameAndNavigate(user.uid)
                    } else {
                        Toast.makeText(activity, "Login failed: user is null", Toast.LENGTH_SHORT).show()
                    }
                },
                onVerifyFailed = { errorMsg ->
                    Toast.makeText(context, "Verification failed: $errorMsg", Toast.LENGTH_SHORT).show()
                },
                onResendCode = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("rate_location") {
            RateLocationScreen(navController)
        }

        composable("help") {
            HelpScreen(navController = navController)
        }
    }
}
