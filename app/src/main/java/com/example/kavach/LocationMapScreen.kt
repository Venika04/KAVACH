package com.example.kavach // Replace with your actual package

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun LocationMapScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var currentLocation by remember { mutableStateOf<Location?>(null) }

    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    // Request location permission
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
            }
        }
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
            }
        }
    }

    // UI layout with map + SOS button
    Column(modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .padding(16.dp)// or any custom height you prefer
        .border(
            width = 2.dp,
            color = Color.Gray,
            shape = RoundedCornerShape(12.dp)
        )
        .padding(8.dp)
    ) {

        // Map takes available space above SOS
        Box(modifier = Modifier.weight(1f)) {
            currentLocation?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                val cameraPositionState = rememberCameraPositionState()

                LaunchedEffect(currentLocation) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                ) {
                    Marker(
                        state = MarkerState(position = latLng),
                        title = "You are here"
                    )
                }
            } ?: Text("Fetching location...", modifier = Modifier.align(Alignment.Center))
        }
    }
}
