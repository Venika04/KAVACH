package com.example.kavach.main.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    private var lastShakeTimestamp: Long = 0
    private var shakeCount = 0
    private var lastSosTriggerTime: Long = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
            val now = System.currentTimeMillis()

            if (acceleration > 12) {
                // Reset shakeCount if too much time has passed since last shake
                if (now - lastShakeTimestamp > 1500) {
                    shakeCount = 0
                }

                // Count shake
                if (now - lastShakeTimestamp > 300) {
                    shakeCount++
                    lastShakeTimestamp = now
                }

                // Trigger SOS if shakeCount reached AND cooldown passed
                if (shakeCount >= 2 && now - lastSosTriggerTime > 5000) {
                    onShake()
                    shakeCount = 0
                    lastSosTriggerTime = now
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}
