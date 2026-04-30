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
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.kavach.R
import com.example.kavach.main.util.SOSRepository
import kotlin.math.abs

class FloatingSOSService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var cancelView: View? = null
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

        val notification = NotificationCompat.Builder(this, channelId)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 40
        params.y = 300

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_sos, null)

        val sosButton = floatingView.findViewById<TextView>(R.id.btnSOS)

        // 👉 DRAG VARIABLES
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        sosButton.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val dx = abs(event.rawX - initialTouchX)
                    val dy = abs(event.rawY - initialTouchY)

                    // 👉 If movement is small → treat as click
                    if (dx < 10 && dy < 10) {
                        handleSOSClick(sosButton)
                    }
                    true
                }

                else -> false
            }
        }

        windowManager.addView(floatingView, params)
    }

    // 👉 Extracted click logic
    private fun handleSOSClick(sosButton: TextView) {
        Log.d("FloatingSOS", "Floating SOS tapped")
        Toast.makeText(this, "Opening Kavach SOS", Toast.LENGTH_SHORT).show()

        vibrate()
        animateButton(sosButton)

        val intent = Intent(
            this@FloatingSOSService,
            com.example.kavach.MainActivity::class.java
        )
        intent.putExtra("START_FLOATING_SOS", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        startActivity(intent)
    }

    private fun startProbationPeriod() {
        showCancelOverlay(initial = true)

        probationTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt() + 1
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
        val countdownText = cancelView!!.findViewById<TextView>(R.id.tvCountdown)
        countdownText.text = "Tap to cancel"

        cancelBtn.setOnClickListener {

            val intent = Intent(this, com.example.kavach.MainActivity::class.java)
            intent.putExtra("SHOW_PIN_DIALOG", true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

            Toast.makeText(this, "Enter PIN to cancel SOS", Toast.LENGTH_SHORT).show()
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

        // 1. Trigger backend SOS (SMS + Firestore)
        SOSRepository.triggerSOS(this)

        // 2. 🚀 IMMEDIATELY bring app to foreground
        val intent = Intent(this@FloatingSOSService, com.example.kavach.MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        // 3. 🔔 Start alarm
        alarmPlayer = MediaPlayer.create(this, R.raw.alarm)
        alarmPlayer?.isLooping = true
        alarmPlayer?.start()

        // 4. Stop alarm after 15 sec
        object : CountDownTimer(15000, 15000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                alarmPlayer?.stop()
                alarmPlayer?.release()
                alarmPlayer = null
            }
        }.start()

        // 5. Show cancel overlay (optional)
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
