package com.example.kavach.auth

import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kavach.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardOptions



@Composable
fun AuthScreen(
    isSignUp: Boolean = false,
    onEmailAuthSubmit: (String, String) -> Unit,
    onPhoneAuthSubmit: (String) -> Unit,
    onToggleMode: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val topBarColor = Color(0xFF6200EE)

    var selectedMethod by remember { mutableStateOf(AuthMethod.PHONE) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.kavach_logo),
                            contentDescription = "Kavach Logo",
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("KAVACH", color = Color.White)
                    }
                },
                backgroundColor = topBarColor
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSignUp) "Sign Up" else "Sign In",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Heading Row with better styling
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthHeading(
                    modifier = Modifier.weight(1f),
                    text = "Phone Login",
                    isSelected = selectedMethod == AuthMethod.PHONE,
                    onClick = { selectedMethod = AuthMethod.PHONE },
                    iconRes = R.drawable.ic_phone
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (selectedMethod == AuthMethod.PHONE) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val activity = context as ComponentActivity

                    Button(
                        onClick = {
                            if (phoneNumber.length == 10) {
                                val fullPhoneNumber = "+91$phoneNumber"
                                PhoneAuthHelper.startPhoneNumberVerification(
                                    phoneNumber = fullPhoneNumber,
                                    activity = activity,
                                    onCodeSent = { verificationId ->
                                        navController.navigate("otpVerification/$verificationId")
                                    },
                                    onVerificationCompleted = { credential ->
                                        PhoneAuthHelper.signInWithPhoneAuthCredential(
                                            credential,
                                            onSuccess = {
                                                navController.navigate("home") {
                                                    popUpTo("auth") { inclusive = true }
                                                }
                                            },
                                            onFailure = {
                                                dialogMessage = "Auto verification failed"
                                                showDialog = true
                                            }
                                        )
                                    },
                                    onVerificationFailed = { exception ->
                                        dialogMessage = "Verification failed: ${exception.message}"
                                        showDialog = true
                                    }
                                )
                            } else {
                                dialogMessage = "Enter valid phone number"
                                showDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send OTP")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }


                AuthHeading(
                    modifier = Modifier.weight(1f),
                    text = "Email Login",
                    isSelected = selectedMethod == AuthMethod.EMAIL,
                    onClick = { selectedMethod = AuthMethod.EMAIL },
                    iconRes = R.drawable.ic_email
                )

                if (selectedMethod == AuthMethod.EMAIL) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                dialogMessage = "Please enter a valid email address"
                                showDialog = true
                            } else if (password.length < 6) {
                                dialogMessage = "Password must be at least 6 characters"
                                showDialog = true
                            } else {
                                onEmailAuthSubmit(email, password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = if (isSignUp) "Register" else "Login")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            TextButton(onClick = onToggleMode) {
                Text(
                    text = if (isSignUp)
                        "Already have an account? Sign In"
                    else
                        "Don't have an account? Sign Up"
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Authentication Error") },
            text = { Text(text = dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

}

@Composable
fun AuthHeading(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconRes: Int? = null
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colors.primary else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colors.primary else Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Underline indicator for selected heading
        Divider(
            color = if (isSelected) MaterialTheme.colors.primary else Color.Transparent,
            thickness = 3.dp,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}

enum class AuthMethod {
    PHONE,
    EMAIL
}
