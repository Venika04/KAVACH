package com.example.kavach

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.kavach.main.service.FloatingSOSService
import com.example.kavach.main.service.ShakeService
import com.example.kavach.ui.theme.KavachTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun checkAndRequestPermissions() {
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 1001)
        }
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        // Existing permissions
        checkAndRequestPermissions()

        // 🔴 ADDED: Overlay permission (non-intrusive)
        checkOverlayPermission()

        // Existing Shake Service
        val shakeServiceIntent = Intent(this, ShakeService::class.java)
        startService(shakeServiceIntent)

        if (Settings.canDrawOverlays(this)) {
            startService(Intent(this, FloatingSOSService::class.java))
        }


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


    override fun onResume() {
        super.onResume()

        if (Settings.canDrawOverlays(this)) {
            // Start floating SOS only after permission is granted
            startService(Intent(this, FloatingSOSService::class.java))
        }
    }

}
