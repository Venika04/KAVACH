package com.example.kavach.main.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
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
import androidx.core.app.ActivityCompat
import com.example.kavach.R
import com.example.kavach.main.util.SOSRepository

class FloatingSOSService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: android.view.View
    private var cancelView: android.view.View? = null
    private var probationTimer: CountDownTimer? = null
    private var alarmPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceProperly()
        createFloatingView()
    }

    private fun startForegroundServiceProperly() {
        val channelId = "sos_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SOS Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setContentTitle("Kavach Active")
            .setContentText("Floating SOS is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    private fun createFloatingView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.BOTTOM or Gravity.START
        params.x = 40
        params.y = 250

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_sos, null)

        val sosButton = floatingView.findViewById<TextView>(R.id.btnSOS)
        sosButton.setOnClickListener {
            if (probationTimer != null) return@setOnClickListener

            Log.d("FloatingSOS", "Floating SOS tapped")
            Toast.makeText(this, "SOS initiated", Toast.LENGTH_SHORT).show()
            vibrate()
            animateButton(sosButton)

            startProbationPeriod()
        }

        windowManager.addView(floatingView, params)
    }

    private fun startProbationPeriod() {
        showCancelOverlay(initial = true)

        probationTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt() + 1
                // Update countdown text on overlay
                cancelView?.findViewById<TextView>(R.id.tvCountdown)?.text =
                    "Triggering SOS in $secondsLeft sec"
            }

            override fun onFinish() {
                probationTimer = null
                removeCancelOverlay()
                triggerSOS()
            }
        }.start()

    }

    private fun showCancelOverlay(initial: Boolean, remainingSeconds: Int = 5) {
        cancelView = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_sos, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        val cancelBtn = cancelView!!.findViewById<TextView>(R.id.btnCancel)
        // Assuming you have a TextView in the overlay to show countdown
        val countdownText = cancelView!!.findViewById<TextView>(R.id.tvCountdown)
        countdownText.text = "Tab to cancel"

        cancelBtn.setOnClickListener {
            probationTimer?.cancel()
            probationTimer = null

            // Stop alarm if playing
            alarmPlayer?.stop()
            alarmPlayer?.release()
            alarmPlayer = null

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

    private fun triggerSOS() {
        // Send SMS
        SOSRepository.triggerSOS(this)

        // Play alarm
        alarmPlayer = MediaPlayer.create(this, R.raw.alarm)
        alarmPlayer?.isLooping = true
        alarmPlayer?.start()

        // Stop alarm after 15s automatically
        object : CountDownTimer(15000, 15000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                alarmPlayer?.stop()
                alarmPlayer?.release()
                alarmPlayer = null
            }
        }.start()

        // Show cancel button so user can stop alarm manually
        showCancelOverlay(initial = false)
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(150)
        }
    }

    private fun animateButton(view: TextView) {
        view.animate()
            .scaleX(0.85f)
            .scaleY(0.85f)
            .setDuration(80)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .start()
            }
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        probationTimer?.cancel()
        alarmPlayer?.stop()
        alarmPlayer?.release()
        removeCancelOverlay()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
