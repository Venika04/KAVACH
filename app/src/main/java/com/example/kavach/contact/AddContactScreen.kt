package com.example.kavach.contact

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
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
import androidx.compose.ui.Alignment
import com.example.kavach.R
import kotlinx.coroutines.launch


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
    var editIndex by remember { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isLoading = contactViewModel.isLoading

    LaunchedEffect(Unit) {
        contactViewModel.fetchContacts()
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                backgroundColor = Color(0xFF4B0082),
                contentColor = Color.White,
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        floatingActionButton = {
            IconButton(
                onClick = {
                    showDialog = true
                    name = ""
                    phone = ""
                    relationship = ""
                    editIndex = null
                },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(60.dp)
                    .background(Color(0xFF4B0082), shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Contact",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF2EAFB))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Emergency Contacts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (contactList.isEmpty()) {
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
                                                // Load data into form
                                                name = contact.name
                                                phone = contact.phone
                                                relationship = contact.relationship
                                                editIndex = index
                                                showDialog = true
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                                            }
                                            IconButton(onClick = {
                                                contactViewModel.removeContact(index)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Contact deleted")
                                                }
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Dialog
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showDialog = false
                                errorMessage = null
                            },
                            title = {
                                Text(if (editIndex == null) "Add Contact" else "Edit Contact")
                            },
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
                                        val contact = Contact(name, phone, relationship)
                                        if (editIndex != null) {
                                            contactViewModel.updateContact(editIndex!!, contact)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Contact updated")
                                            }
                                        } else {
                                            contactViewModel.addContact(contact)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Contact added")
                                            }
                                        }
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
        }
    )
}
