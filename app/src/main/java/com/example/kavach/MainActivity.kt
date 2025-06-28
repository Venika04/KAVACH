package com.example.kavach

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.kavach.contact.ContactViewModel
import com.example.kavach.main.service.ShakeService
import com.example.kavach.main.util.ShakeDetector
import com.example.kavach.main.triggerSOS
import com.example.kavach.ui.theme.KavachTheme
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var shakeDetector: ShakeDetector
    private lateinit var sensorManager: SensorManager

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 1001)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        checkAndRequestPermissions()

        val shakeServiceIntent = Intent(this, ShakeService::class.java)
        startService(shakeServiceIntent)

        setContent {
            val navController = rememberNavController()
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            KavachTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        navController = navController,
                        auth = auth,
                        isLoggedIn = currentUser != null
                    )
                }
            }
        }
    }
}
