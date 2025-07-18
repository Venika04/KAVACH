package com.example.kavach.auth

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kavach.R
import com.google.firebase.auth.PhoneAuthProvider


@Composable
fun OTPVerificationScreen(
    verificationId: String,
    onVerifySuccess: () -> Unit,
    onVerifyFailed: (String) -> Unit,
    onResendCode: () -> Unit,
    onBack: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val topBarColor = Color(0xFF4B0082)

    var showDialog1 by remember { mutableStateOf(false) }
    var showDialog2 by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.kavach_logo),
                            contentDescription = "Kavach Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("KAVACH", color = Color.White)
                    }
                },
                backgroundColor = topBarColor,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter OTP sent to your phone",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

//            val otpFocusRequester = remember { FocusRequester() }

            OtpInput(
                otpLength = 6,
                otpValue = otpCode,
                onOtpChange = { otpCode = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (otpCode.length == 6) {
                        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
                        PhoneAuthHelper.signInWithPhoneAuthCredential(
                            credential,
                            onSuccess = {
                                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                if (user != null) {
                                    dialogMessage = "Verification successful!"
                                    showDialog1 = true
                                    onVerifySuccess()
                                } else {
                                    onVerifyFailed("Verification succeeded but user is null")
                                }
                            },
                            onFailure = { e ->
                                onVerifyFailed(e.message ?: "Verification failed")
                            }
                        )
                    } else {
                        dialogMessage = "Enter valid 6-digit OTP"
                        showDialog2 = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onResendCode) {
                Text("Resend OTP")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
    if (showDialog1) {
        AlertDialog(
            onDismissRequest = { showDialog1 = false },
            title = { Text(text = "Authentication Successful!") },
            text = { Text(text = dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog1 = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDialog2) {
        AlertDialog(
            onDismissRequest = { showDialog2 = false },
            title = { Text(text = "Authentication Error") },
            text = { Text(text = dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog2 = false }) {
                    Text("OK")
                }
            }
        )
    }

}

@Composable
fun OtpInput(
    otpLength: Int = 6,
    otpValue: String,
    onOtpChange: (String) -> Unit
) {
    val focusRequester: FocusRequester = remember { FocusRequester() }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        repeat(otpLength) { index ->
            val char = otpValue.getOrNull(index)?.toString() ?: ""

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = 2.dp,
                        color = if (char.isNotEmpty()) MaterialTheme.colors.primary else Color.Gray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    style = MaterialTheme.typography.h6
                )
            }
        }

        // Invisible text field to handle typing
        BasicTextField(
            value = otpValue,
            onValueChange = {
                if (it.length <= otpLength && it.all { char -> char.isDigit() }) {
                    onOtpChange(it)
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .size(1.dp) // Invisible but still receives input
                .alpha(0f)
                .focusRequester(focusRequester)
                .focusable(true)
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
