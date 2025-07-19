package com.example.kavach.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

@Composable
fun LocationScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationText by remember { mutableStateOf("Fetching location...") }

    LaunchedEffect(Unit) {
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, locationPermission) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latDMS = convertToDMS(location.latitude, isLatitude = true)
                    val lonDMS = convertToDMS(location.longitude, isLatitude = false)
                    locationText = "Latitude: $latDMS\nLongitude: $lonDMS"
                } else {
                    locationText = "Unable to get location"
                }
            }
        } else {
            locationText = "Location permission not granted"
        }
    }

    Text(text = locationText, modifier = Modifier.padding(16.dp))
}

// 🔁 Helper function to convert decimal degrees to DMS format
fun convertToDMS(value: Double, isLatitude: Boolean): String {
    val absolute = kotlin.math.abs(value)
    val degrees = absolute.toInt()
    val minutesFloat = (absolute - degrees) * 60
    val minutes = minutesFloat.toInt()
    val seconds = ((minutesFloat - minutes) * 60).toInt()

    val direction = when {
        isLatitude && value >= 0 -> "N"
        isLatitude && value < 0 -> "S"
        !isLatitude && value >= 0 -> "E"
        else -> "W"
    }

    return "%d°%d'%d\" %s".format(degrees, minutes, seconds, direction)
}
