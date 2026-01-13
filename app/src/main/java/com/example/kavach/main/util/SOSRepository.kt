package com.example.kavach.main.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object SOSRepository {

    /** Sends SMS to all contacts immediately with proper permission checks */
    fun triggerSOS(context: Context) {

        // 1️⃣ Check SMS permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "SMS permission missing", Toast.LENGTH_SHORT).show()
            Log.e("KavachSOS", "SEND_SMS permission not granted")
            return
        }

        // 2️⃣ Check Location permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission missing", Toast.LENGTH_SHORT).show()
            Log.e("KavachSOS", "Location permission not granted")
            return
        }

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            Log.e("KavachSOS", "User not logged in")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // 3️⃣ Fetch contacts
        db.collection("users").document(userId).collection("contacts")
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    Toast.makeText(context, "No emergency contacts found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val phoneNumbers = docs.mapNotNull { it.getString("phone") }

                // 4️⃣ Fetch last location
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location == null) {
                        Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                        Log.e("KavachSOS", "Location is null")
                        return@addOnSuccessListener
                    }

                    val message = "SOS! I need help. My location: https://maps.google.com/?q=${location.latitude},${location.longitude}"

                    // 5️⃣ Send SMS
                    phoneNumbers.forEach { phone ->
                        try {
                            SmsManager.getDefault().sendTextMessage(phone, null, message, null, null)
                            Log.d("KavachSOS", "SMS sent to $phone")
                        } catch (e: Exception) {
                            Log.e("KavachSOS", "Failed to send SMS to $phone", e)
                        }
                    }

                    Toast.makeText(context, "SOS sent successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Log.e("KavachSOS", "Failed to get location", it)
                    Toast.makeText(context, "Failed to fetch location", Toast.LENGTH_SHORT).show()
                }

            }
            .addOnFailureListener {
                Log.e("KavachSOS", "Failed to fetch contacts", it)
                Toast.makeText(context, "Failed to fetch contacts", Toast.LENGTH_SHORT).show()
            }
    }
}
