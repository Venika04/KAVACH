package com.example.kavach.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kavach.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(navController: NavHostController) {
    val topBarColor = Color(0xFF4B0082)
    val backgroundColor = Color(0xFFF3E5F5)
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: "No Email Found"
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    var userName by remember { mutableStateOf("Loading...") }
    var editedName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(user?.phoneNumber ?: "") }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // Fetch user name from Firestore
    LaunchedEffect(userId) {
        userId?.let { uid ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nameFromDb = document.getString("name") ?: "No Name Found"
                        userName = nameFromDb
                        editedName = nameFromDb
                    } else {
                        userName = "No User Document"
                        editedName = ""
                    }
                }
                .addOnFailureListener {
                    userName = "Failed to load name"
                    editedName = ""
                }
        }
    }

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
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = topBarColor
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                item {
                    val visibilities = remember { List(5) { mutableStateOf(false) } }

                    LaunchedEffect(Unit) {
                        visibilities.forEachIndexed { index, state ->
                            delay(150)
                            state.value = true
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(
                        visible = visibilities[0].value,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                    ) {
                        InfoCard(
                            label = "Username",
                            value = editedName,
                            onValueChange = { editedName = it },
                            readOnly = false
                        )
                    }

                    AnimatedVisibility(
                        visible = visibilities[1].value,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                    ) {
                        InfoCard(
                            label = "Phone Number",
                            value = phoneNumber,
                            onValueChange = {},
                            readOnly = true
                        )
                    }

                    AnimatedVisibility(
                        visible = visibilities[2].value,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                    ) {
                        InfoCard(
                            label = "Email ID",
                            value = email,
                            onValueChange = {},
                            readOnly = true
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedVisibility(
                        visible = visibilities[3].value,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                    ) {
                        Button(
                            onClick = {
                                userId?.let { uid ->
                                    val updates = hashMapOf("name" to editedName)
                                    FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(uid)
                                        .update(updates as Map<String, Any>)
                                        .addOnSuccessListener {
                                            dialogMessage = "Changes saved successfully!"
                                            showDialog = true
                                        }
                                        .addOnFailureListener {
                                            dialogMessage = "Failed to save changes"
                                            showDialog = true
                                        }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF388E3C))
                        ) {
                            Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = visibilities[4].value,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                    ) {
                        Button(
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("auth") {
                                    popUpTo("main") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                        ) {
                            Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }


                val items = listOf("My Contacts", "Location History", "Safety Tips", "About App")
                val chunkedItems = items.chunked(2)

                chunkedItems.forEach { rowItems ->
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                rowItems.forEach { label ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(8.dp)
                                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                            .aspectRatio(1f)
                                            .clickable {
                                                if (label == "My Contacts") {
                                                    navController.navigate("add_contact")
                                                }

                                                if (label == "Safety Tips") {
                                                    navController.navigate("help_screen")
                                                }

                                                if (label == "About App") {
                                                    navController.navigate("about_app")
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Update Status") },
                    text = { Text(dialogMessage) },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun InfoCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = 6.dp,
        border = BorderStroke(1.dp, Color(0xFF4B0082)),
        backgroundColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = label,
                color = Color(0xFF4B0082),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent
                )
            )
        }
    }
}
