package com.example.kavach.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun ProfileScreen(navController: NavHostController) {
    val topBarColor = Color(0xFF6200EE)
    val backgroundColor = Color(0xFFF3E5F5)
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: "No Email Found"
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // State variables for user info
    var userName by remember { mutableStateOf("Loading...") }
    var editedName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(user?.phoneNumber ?: "") }

    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // Load user name from Firestore on screen start or userId change
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
            // Use LazyColumn for full screen scrolling and avoid nested scroll conflicts
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Header content: profile image, name, phone, email, save button
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_person),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Gray, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    InfoCard(
                        label = "Username",
                        value = editedName,
                        onValueChange = { editedName = it },
                        readOnly = false
                    )

                    InfoCard(
                        label = "Phone number",
                        value = phoneNumber,
                        onValueChange = {},
                        readOnly = true
                    )

                    InfoCard(
                        label = "Email Id.",
                        value = email,
                        onValueChange = {},
                        readOnly = true
                    )


                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            userId?.let { uid ->
                                val updates = hashMapOf(
                                    "name" to editedName
//                                    "phoneNumber" to phoneNumber
                                )
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

                    Spacer(modifier = Modifier.height(24.dp))
                }



                // Grid-like section with 2 columns created manually in LazyColumn rows
                val items = listOf("My Contacts", "Location History", "Safety Tips", "About App")
                val chunkedItems = items.chunked(2)
                chunkedItems.forEach { rowItems ->
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            rowItems.forEach { label ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        .aspectRatio(1f)
                                        .clickable {
                                            // TODO: Handle click event
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f)) // Fill space if odd number of items
                            }
                        }
                    }
                }

                // Logout button at bottom
                item {
                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {showDialog = false},
                    title = { Text("Update Status") },
                    text = { Text(dialogMessage) },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {showDialog = false}) {
                            Text("Cancel")
                        }
                    }
                )

            }
        }
    )
}


// Helper function to show a card with a single OutlinedTextField
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
        border = BorderStroke(1.dp, Color(0xFF6200EE)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = label,
                color = Color(0xFF6200EE),
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
