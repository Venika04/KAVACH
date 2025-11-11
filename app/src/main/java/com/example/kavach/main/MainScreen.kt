package com.example.kavach.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.kavach.R
import com.example.kavach.contact.ContactViewModel
import com.example.kavach.guardian.GuardianPinDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("ServiceCast")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    contactViewModel: ContactViewModel = viewModel()
) {
    val topBarColor = Color(0xFF4B0082)
    val backgroundColor = Color(0xFFF3E5F5)
    val bottomBarColor = topBarColor
    val context = LocalContext.current

    val contactViewModel: ContactViewModel = viewModel()
    var isSosActive by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var alarmPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var lastSosDocId by remember { mutableStateOf<String?>(null) }

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
                actions = {
                    IconButton(onClick = {
                        navController.navigate("profile")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = topBarColor
            )
        },
        bottomBar = {
            BottomNavigation(backgroundColor = bottomBarColor) {
                BottomNavigationItem(
                    selected = false,
                    onClick = { navController.navigate("add_contact") },
                    label = { Text("Contact", color = Color.White) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_contact),
                            contentDescription = "Contact icon",
                            tint = Color.White
                        )
                    },
                    alwaysShowLabel = true
                )
                BottomNavigationItem(
                    selected = false,
                    onClick = { navController.navigate("rate_location") },
                    label = { Text("Rating", color = Color.White) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location_rating),
                            contentDescription = "Location rating",
                            tint = Color.White
                        )
                    },
                    alwaysShowLabel = true
                )
                BottomNavigationItem(
                    selected = false,
                    onClick = {
                        navController.navigate("help") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    label = { Text("Help", color = Color.White) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = "Help",
                            tint = Color.White
                        )
                    },
                    alwaysShowLabel = true
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(it),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp)
            ) {
                LocationBox()
                Spacer(modifier = Modifier.height(40.dp))
                if (!isSosActive) {
                    SOSButton {
                        triggerSOS(context, contactViewModel) { player, docId ->
                            alarmPlayer = player
                            isSosActive = true
                            lastSosDocId = docId
                        }
                    }
                } else {
                    Button(
                        onClick = { showPinDialog = true },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                        modifier = Modifier.size(100.dp)
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 16.sp)
                    }
                }
            }

            // Guardian PIN dialog
            if (showPinDialog) {
                GuardianPinDialog(
                    onDismiss = { showPinDialog = false },
                    onPinValidated = {
                        alarmPlayer?.stop()
                        alarmPlayer?.release()
                        alarmPlayer = null
                        isSosActive = false
                        showPinDialog = false
                        showConfirmationDialog = true
                        Toast.makeText(context, "SOS Cancelled Successfully", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Confirmation dialog (mistake or real)
            if (showConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    title = { Text("SOS Confirmation") },
                    text = { Text("Was the SOS triggered by mistake or was it real danger?") },
                    confirmButton = {
                        TextButton(onClick = {
                            sendFollowUpSMS(context, contactViewModel, "real")
                            recordSosResponse(lastSosDocId, "real")
                            showConfirmationDialog = false
                            Toast.makeText(context, "Marked as real danger", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Real Danger")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            sendFollowUpSMS(context, contactViewModel, "mistake")
                            recordSosResponse(lastSosDocId, "mistake")
                            showConfirmationDialog = false
                            Toast.makeText(context, "Marked as mistake", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Mistake")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LocationBox() {
    LocationScreen()
}

@SuppressLint("MissingPermission")
fun triggerSOS(
    context: Context,
    contactViewModel: ContactViewModel,
    onAlarmStarted: (MediaPlayer, String?) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    if (
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(context, "Please grant Location and SMS permissions", Toast.LENGTH_LONG).show()
        return
    }

    contactViewModel.fetchContacts()
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            val lat = location.latitude
            val lon = location.longitude
            val message = "SOS! I need help. My location: https://maps.google.com/?q=$lat,$lon"

            if (contactViewModel.contactList.isEmpty()) {
                Toast.makeText(context, "No emergency contacts available", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Send initial SOS SMS
            contactViewModel.contactList.forEach { contact ->
                try {
                    SmsManager.getDefault().sendTextMessage(contact.phone, null, message, null, null)
                    Log.d("KavachSOS", "SMS sent to ${contact.phone}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Failed to send SMS to ${contact.phone}", Toast.LENGTH_SHORT).show()
                }
            }

            // Store in Firestore
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
            val sosData = mapOf(
                "latitude" to lat,
                "longitude" to lon,
                "timestamp" to System.currentTimeMillis(),
                "userId" to userId
            )
            FirebaseFirestore.getInstance().collection("sos_alerts")
                .add(sosData)
                .addOnSuccessListener { docRef ->
                    val alarmPlayer = MediaPlayer.create(context, R.raw.alarm)
                    alarmPlayer.isLooping = true
                    alarmPlayer.start()
                    onAlarmStarted(alarmPlayer, docRef.id)
                    Toast.makeText(context, "SOS Sent to Contacts", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Could not fetch location", Toast.LENGTH_SHORT).show()
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Failed to get location.", Toast.LENGTH_SHORT).show()
    }
}

// Sends follow-up SMS after user chooses (mistake or real)
@SuppressLint("MissingPermission")
fun sendFollowUpSMS(context: Context, contactViewModel: ContactViewModel, responseType: String) {
    val smsManager = SmsManager.getDefault()
    val message = when (responseType) {
        "mistake" -> "Update: The previous SOS alert was triggered by mistake. No danger now."
        else -> "Update: The previous SOS alert was real. Assistance is still required!"
    }

    contactViewModel.fetchContacts()
    if (contactViewModel.contactList.isEmpty()) {
        Toast.makeText(context, "No emergency contacts available", Toast.LENGTH_SHORT).show()
        return
    }

    contactViewModel.contactList.forEach { contact ->
        try {
            smsManager.sendTextMessage(contact.phone, null, message, null, null)
            Log.d("KavachSOS", "Follow-up SMS sent to ${contact.phone}")
        } catch (e: Exception) {
            Log.e("KavachSOS", "Failed to send follow-up SMS to ${contact.phone}", e)
        }
    }
}

// Updates Firestore record with the response type
fun recordSosResponse(documentId: String?, response: String) {
    if (documentId == null) return
    val db = FirebaseFirestore.getInstance()
    db.collection("sos_alerts").document(documentId)
        .update("response", response)
        .addOnSuccessListener { Log.d("KavachSOS", "Response updated successfully") }
        .addOnFailureListener { Log.e("KavachSOS", "Failed to update response", it) }
}

@Composable
fun SOSButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
        modifier = Modifier.size(100.dp)
    ) {
        Text("SOS", color = Color.White, fontSize = 18.sp)
    }
}
