package com.example.super_kemo_taro3

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DndReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_DND_ON  = "com.example.super_kemo_taro3.DND_ON"
        const val ACTION_DND_OFF = "com.example.super_kemo_taro3.DND_OFF"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DND", "onReceive: ${intent.action}")
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return

        when (intent.action) {
            ACTION_DND_ON  -> nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            ACTION_DND_OFF -> nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}