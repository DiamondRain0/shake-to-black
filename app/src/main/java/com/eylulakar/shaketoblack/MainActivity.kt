package com.eylulakar.shaketoblack
import androidx.appcompat.widget.SwitchCompat

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences

    private val SHAKE_THRESHOLD_GRAVITY_MIN = 2.0F
    private val SHAKE_THRESHOLD_GRAVITY_MAX = 10.0F
    private lateinit var requestOverlayPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the launcher for requesting overlay permission
        requestOverlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                startShakeService()
            }
        }

        // To store the preferred values
        sharedPreferences = this.getSharedPreferences("com.eylulakar.shaketoblack", MODE_PRIVATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + packageName)
                )
                requestOverlayPermissionLauncher.launch(intent)
            } else {
                startShakeService()
            }
        } else {
            startShakeService()
        }

        val seekBar: SeekBar = findViewById(R.id.seekBarOpacity)
        seekBar.progress = sharedPreferences.getInt("opacity", 80) // Set initial progress

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                changeOverlayOpacity(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Handle start of touch event if needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Handle end of touch event if needed
            }
        })

        val seekBar2: SeekBar = findViewById(R.id.seekBarSensitivity)
        seekBar2.progress = ((sharedPreferences.getFloat("newGravity", 4.0F) - SHAKE_THRESHOLD_GRAVITY_MIN) * 100 / (SHAKE_THRESHOLD_GRAVITY_MAX - SHAKE_THRESHOLD_GRAVITY_MIN)).toInt()
        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newGravity = SHAKE_THRESHOLD_GRAVITY_MIN + (progress / 100f) * (SHAKE_THRESHOLD_GRAVITY_MAX - SHAKE_THRESHOLD_GRAVITY_MIN)
                setShakeGravity(newGravity)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }
        })

        val switchControl: SwitchCompat = findViewById(R.id.switchControl)
        switchControl.isChecked = sharedPreferences.getBoolean("touchEnable",true)
        switchControl.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch is ON
                // Perform actions when switch is ON
                setTouchable(true)
            } else {
                // Switch is OFF
                // Perform actions when switch is OFF
                setTouchable(false)
            }
        }


    }

    private fun startShakeService() {
        val intent = Intent(this, ShakeOverlayService::class.java)
        intent.putExtra("opacity", sharedPreferences.getInt("opacity", 80)) // Initial opacity
        intent.putExtra("shakeThresholdGravity", sharedPreferences.getFloat("newGravity", 4.0F))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun changeOverlayOpacity(opacity: Int) {
        val intent = Intent(this, ShakeOverlayService::class.java)
        sharedPreferences.edit().putInt("opacity", opacity).apply()
        intent.putExtra("opacity", opacity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun setShakeGravity(newGravity: Float) {
        val intent = Intent(this, ShakeOverlayService::class.java)
        sharedPreferences.edit().putFloat("newGravity", newGravity).apply()
        intent.putExtra("shakeThresholdGravity", newGravity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    private fun setTouchable(enable: Boolean){
        val intent = Intent(this, ShakeOverlayService::class.java)
        sharedPreferences.edit().putBoolean("touchEnable", enable).apply()
        intent.putExtra("touchEnable", enable)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}