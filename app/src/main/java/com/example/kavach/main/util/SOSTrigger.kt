package com.example.kavach.main.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.kavach.R
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun triggerSOSFromService(context: Context) {

    // 1️⃣ Permission check
    if (
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.e("SOS_SERVICE", "Permissions missing")
        return
    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId == null) {
        Log.e("SOS_SERVICE", "User not logged in")
        return
    }

    val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->

        if (location == null) {
            Log.e("SOS_SERVICE", "Location is NULL")
            return@addOnSuccessListener
        }

        val lat = location.latitude
        val lon = location.longitude
        val message =
            "🚨 SOS ALERT!\nI need help.\nLocation:\nhttps://maps.google.com/?q=$lat,$lon"

        // 🔥 CORRECT COLLECTION NAME
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("emergency_contacts")
            .get()
            .addOnSuccessListener { docs ->

                if (docs.isEmpty) {
                    Log.e("SOS_SERVICE", "No emergency contacts found")
                    return@addOnSuccessListener
                }

                val smsManager = SmsManager.getDefault()

                for (doc in docs) {
                    val phone = doc.getString("phone")
                    if (phone.isNullOrEmpty()) continue

                    try {
                        smsManager.sendTextMessage(
                            phone,
                            null,
                            message,
                            null,
                            null
                        )
                        Log.d("SOS_SERVICE", "SMS sent to $phone")
                    } catch (e: Exception) {
                        Log.e("SOS_SERVICE", "SMS failed for $phone", e)
                    }
                }

                // 🔊 Alarm (after SMS)
                val player = MediaPlayer.create(context, R.raw.alarm)
                player.isLooping = true
                player.start()

                Log.d("SOS_SERVICE", "SOS COMPLETED SUCCESSFULLY")
            }
            .addOnFailureListener {
                Log.e("SOS_SERVICE", "Firestore error", it)
            }
    }
}
