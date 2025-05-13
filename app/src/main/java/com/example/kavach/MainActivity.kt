package com.example.kavach

import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.kavach.ui.theme.KavachTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestPermissions()

//        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            KavachTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        navController = navController,
                        getLocation = { getLastLocation() },
                        sendSOS = { location -> sendEmergencySMS(location)}
                    )
                }
            }

//            MainScreen(
//                getLocation = { getLastLocation() },
//                sendSOS = { location -> sendEmergencySMS(location) }
//            )
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS
        )
        ActivityCompat.requestPermissions(this, permissions, 0)
    }
    @SuppressLint("MissingPermission")
    private fun getLastLocation(): Location? {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            currentLocation = location
        }
        return currentLocation
    }

    private fun sendEmergencySMS(location: Location?) {
        if (location != null) {
            val message = "Emergency! I need help. My location is: " +
                    "https://maps.google.com/?q=${location.latitude},${location.longitude}"

            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(
                    "1234567890", // Replace with emergency contact
                    null,
                    message,
                    null,
                    null
                )
                Toast.makeText(this, "SOS sent!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to send SMS.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
        }
    }
}

