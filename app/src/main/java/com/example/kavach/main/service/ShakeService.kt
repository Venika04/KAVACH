package com.example.kavach.main.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.example.kavach.R
import com.example.kavach.main.util.SOSRepository
import com.example.kavach.main.util.ShakeDetector

class ShakeService : Service() {

    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector
    private var probationTimer: CountDownTimer? = null
    private var alarmPlayer: MediaPlayer? = null
    private var cancelView: android.view.View? = null
    private lateinit var windowManager: WindowManager

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        shakeDetector = ShakeDetector {
            if (probationTimer == null) {
                startProbation()
            }
        }

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    private fun startProbation() {
        Toast.makeText(this, "SOS shaking detected! You have 5 seconds to cancel.", Toast.LENGTH_SHORT).show()
        vibrate()

        showCancelOverlay(initial = true)

        probationTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                probationTimer = null
                removeCancelOverlay()
                triggerSOS()
            }
        }.start()
    }

    private fun triggerSOS() {
        // Send SMS via your SOSRepository
        SOSRepository.triggerSOS(this)

        // Start alarm
        alarmPlayer = MediaPlayer.create(this, R.raw.alarm)
        alarmPlayer?.isLooping = true
        alarmPlayer?.start()

        // Stop alarm automatically after 15 seconds
        object : CountDownTimer(15000, 15000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                stopAlarm()
            }
        }.start()

        // Show cancel button after SOS triggered
        showCancelOverlay(initial = false)
    }

    private fun stopAlarm() {
        alarmPlayer?.stop()
        alarmPlayer?.release()
        alarmPlayer = null
    }

    private fun showCancelOverlay(initial: Boolean) {
        cancelView = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_sos, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        val cancelBtn = cancelView!!.findViewById<TextView>(R.id.btnCancel)
        cancelBtn.setOnClickListener {
            // Cancel probation or alarm
            probationTimer?.cancel()
            probationTimer = null
            stopAlarm()
            removeCancelOverlay()
            Toast.makeText(this, "SOS cancelled", Toast.LENGTH_SHORT).show()
        }

        windowManager.addView(cancelView, params)
    }

    private fun removeCancelOverlay() {
        cancelView?.let {
            windowManager.removeView(it)
            cancelView = null
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        probationTimer?.cancel()
        stopAlarm()
        removeCancelOverlay()
        sensorManager.unregisterListener(shakeDetector)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
