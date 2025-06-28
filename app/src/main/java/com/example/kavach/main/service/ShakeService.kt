package com.example.kavach.main.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.IBinder
import android.telephony.SmsManager
import com.example.kavach.main.util.ShakeDetector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ShakeService : Service() {

    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        loadContactNumbers { contactNumbers ->
            shakeDetector = ShakeDetector {
                triggerSOS(contactNumbers)
            }

            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(shakeDetector)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // 🔽 Fetch emergency contact numbers from Firestore
    private fun loadContactNumbers(onLoaded: (List<String>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            onLoaded(emptyList())
            return
        }

        val contactNumbers = mutableListOf<String>()

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("emergencyContacts")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val phone = document.getString("phoneNumber")
                    if (!phone.isNullOrEmpty()) {
                        contactNumbers.add(phone)
                    }
                }
                onLoaded(contactNumbers)
            }
            .addOnFailureListener {
                onLoaded(emptyList())
            }
    }

    // 🔽 Trigger SOS by sending SMS to all contact numbers
    private fun triggerSOS(contactNumbers: List<String>) {
        val message = "I am in danger. Please help! My location: [Your Location Here]"

        val smsManager = SmsManager.getDefault()
        for (number in contactNumbers) {
            smsManager.sendTextMessage(number, null, message, null, null)
        }
    }
}
