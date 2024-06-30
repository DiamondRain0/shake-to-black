package com.eylulakar.shaketoblack

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "NotificationReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.eylulakar.shaketoblack.ACTION_STOP_SERVICE") {
            Log.d(TAG, "Received stop service action")
            // Stop the service
            val serviceIntent = Intent(context, ShakeOverlayService::class.java)
            context.stopService(serviceIntent)
        }
    }
}
