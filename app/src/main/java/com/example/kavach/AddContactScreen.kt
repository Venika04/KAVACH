package com.example.kavach

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed


@Composable
fun AddContactScreen(
    navController: NavHostController,
    contactViewModel: ContactViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val contactList = contactViewModel.contactList

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2EAFB))
    ) {
        // Header
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            backgroundColor = Color(0xFF8000FF),
            contentColor = Color.White,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your Emergency Contacts",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (contactList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No contacts added yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                itemsIndexed(contactList) { index, contact ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Name: ${contact.name}")
                                Text("Phone: ${contact.phone}")
                                Text("Relation: ${contact.relationship}")
                            }
                            Row {
                                IconButton(onClick = {
                                    // Edit feature can be implemented here
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = {
                                    contactViewModel.removeContact(index)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Button
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .padding(16.dp)
                    .size(60.dp)
                    .background(Color(0xFF8000FF),
                shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Contact",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Contact Form Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Contact") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = relationship,
                            onValueChange = { relationship = it },
                            label = { Text("Relationship") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (phone.length != 10 || !phone.all { it.isDigit() } || name.isBlank() || relationship.isBlank()) {
                            errorMessage = "Please enter a valid 10-digit phone number and all fields are mandatory"
                        } else {
                            val newContact = Contact(name, phone, relationship)
                            contactViewModel.addContact(newContact)
                            name = ""
                            phone = ""
                            relationship = ""
                            errorMessage = null
                            showDialog = false
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        errorMessage = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
