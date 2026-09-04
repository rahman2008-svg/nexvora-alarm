package com.example.nexvora.alarm.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.nexvora.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
  private val TAG = "BootReceiver"

  override fun onReceive(context: Context, intent: Intent) {
    Log.d(TAG, "Received broadcast: ${intent.action}")
    if (intent.action in listOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
        Intent.ACTION_MY_PACKAGE_REPLACED
      )
    ) {
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val db = AppDatabase.getDatabase(context)
          val enabledAlarms = db.alarmDao().getEnabledAlarmsList()
          Log.d(TAG, "Rescheduling ${enabledAlarms.size} alarms")
          for (alarm in enabledAlarms) {
            AlarmScheduler.scheduleAlarm(context, alarm)
          }
        } catch (e: Exception) {
          Log.e(TAG, "Error rescheduling alarms on boot/time change: ${e.message}")
        }
      }
    }
  }
}
