package com.example.kavach

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kavach.auth.*
import com.example.kavach.contact.AddContactScreen
import com.example.kavach.contact.ContactViewModel
import com.example.kavach.help.HelpScreen
import com.example.kavach.main.MainScreen
import com.example.kavach.profile.ProfileScreen
import com.example.kavach.rating.RateLocationScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation(
    navController: NavHostController,
    auth: FirebaseAuth,
    isLoggedIn: Boolean
) {
    val contactViewModel: ContactViewModel = viewModel()
    val startDestination = if (isLoggedIn) "main" else "auth"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("auth") {
            var isSignUp by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val activity = context as Activity
            var hasNavigated by remember { mutableStateOf(false) }

            fun checkUserNameAndNavigate(userId: String) {
                if (hasNavigated) return
                hasNavigated = true
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
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
                                    Toast.makeText(context, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    auth.currentUser?.uid?.let { checkUserNameAndNavigate(it) }
                                } else {
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
                            navController.navigate("otpVerification/$verificationId")
                        },
                        onVerificationCompleted = { credential ->
                            PhoneAuthHelper.signInWithPhoneAuthCredential(
                                credential,
                                onSuccess = { user ->
                                    if (user != null) checkUserNameAndNavigate(user.uid)
                                    else Toast.makeText(activity, "Login failed: user is null", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = {
                                    Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onVerificationFailed = {
                            Toast.makeText(activity, "Verification failed: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                onToggleMode = { isSignUp = !isSignUp },
                navController = navController
            )
        }

        composable("main") {
            MainScreen(
                navController = navController,
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
            FirebaseAuth.getInstance().currentUser?.uid?.let {
                UserInfoScreen(navController, it)
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
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
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
                    FirebaseAuth.getInstance().currentUser?.uid?.let {
                        checkUserNameAndNavigate(it)
                    }
                },
                onVerifyFailed = {
                    Toast.makeText(context, "Verification failed: $it", Toast.LENGTH_SHORT).show()
                },
                onResendCode = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
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
