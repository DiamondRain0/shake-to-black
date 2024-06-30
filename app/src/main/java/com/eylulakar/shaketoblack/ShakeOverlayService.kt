// ShakeOverlayService.kt
package com.eylulakar.shaketoblack

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat

class ShakeOverlayService : Service(), SensorEventListener {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: LinearLayout
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var isOverlayVisible = false
    private var overlayOpacity: Int = 80 // Default opacity, can be adjusted
    private var shakeThresholdGravity: Float = 4.0F // Default shake threshold gravity
    private var touchEnable: Boolean = true
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "ShakeOverlayServiceChannel"

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        // Initialize WindowManager and SensorManager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Check if accelerometer sensor is available
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            stopSelf() // Stop the service if accelerometer sensor is not available
            return
        }

        // Inflate overlay layout
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_layout, null) as LinearLayout

        // Set WindowManager.LayoutParams for the overlay view
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FORMAT_CHANGED
        )

        val params2 = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FORMAT_CHANGED
        )
        params.gravity = Gravity.CENTER
        params2.gravity = Gravity.CENTER

        // params /////////////////////////////////
        if(!touchEnable){
            overlayView.layoutParams = params2
        }else {
            overlayView.layoutParams = params
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.hasExtra("opacity")) {
                setOpacity(intent.getIntExtra("opacity", 80))
            }
            if (intent.hasExtra("shakeThresholdGravity")) {
                setShakeGravity(intent.getFloatExtra("shakeThresholdGravity", 4.0F))
            }
            if (intent.hasExtra("touchEnable")) {
                setTouchEnable(intent.getBooleanExtra("touchEnable", true))
            }
            if (intent.action == "ACTION_STOP_SERVICE") {
                stopSelf() // Stop the service
            }
        }

        // Register sensor listener for accelerometer
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)

        // Create and display the foreground service notification
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister sensor listener and remove overlay view if visible
        sensorManager.unregisterListener(this)
        if (isOverlayVisible) {
            windowManager.removeView(overlayView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            val gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

            if (gForce > shakeThresholdGravity) {
                toggleOverlay()
            }
        }
    }

    private fun toggleOverlay() {
        if (isOverlayVisible) {
            windowManager.removeView(overlayView)
        } else {
            overlayView.alpha = overlayOpacity / 100f // Set opacity from 0 to 1
            windowManager.addView(overlayView, overlayView.layoutParams)
        }
        isOverlayVisible = !isOverlayVisible
    }

    private fun setOpacity(num: Int) {
        overlayOpacity = num
        if (isOverlayVisible) {
            overlayView.alpha = overlayOpacity / 100f
            windowManager.updateViewLayout(overlayView, overlayView.layoutParams)
        }
    }

    private fun setShakeGravity(gravity: Float) {
        shakeThresholdGravity = gravity
    }
    private fun setTouchEnable(touchEnable: Boolean){
        this.touchEnable=touchEnable
        onDestroy()
        onCreate()
    }

    private fun createNotification(): Notification {
        // Create an intent to stop the service
        val stopServiceIntent = Intent(this, ShakeOverlayService::class.java).apply {
            action = "ACTION_STOP_SERVICE"
        }
        val pendingStopServiceIntent = PendingIntent.getService(
            this,
            0,
            stopServiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create an action to stop the service
        val closeAction = NotificationCompat.Action.Builder(
            com.google.android.material.R.drawable.ic_m3_chip_close, // Replace with your close icon
            "Close", // Action title
            pendingStopServiceIntent // PendingIntent to handle the action
        ).build()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ShakeToBlack")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app icon
            .setPriority(NotificationCompat.PRIORITY_LOW) // Set priority to low to avoid sound
            .addAction(closeAction) // Add the close action button
            .build()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Shake Overlay Service Channel",
                NotificationManager.IMPORTANCE_LOW // Set importance to low to avoid sound
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
