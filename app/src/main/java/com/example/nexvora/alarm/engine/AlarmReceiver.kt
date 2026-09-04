package com.example.nexvora.alarm.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.nexvora.data.db.AppDatabase
import com.example.nexvora.data.repository.StreakRepository
import com.example.nexvora.ui.alarm.AlarmTriggerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
  companion object {
    const val ACTION_UPCOMING_ALARM = AlarmScheduler.ACTION_UPCOMING_ALARM
    const val ACTION_DISMISS_UPCOMING = "com.example.nexvora.ACTION_DISMISS_UPCOMING"
    const val ACTION_SNOOZE_NOTIFICATION = "com.example.nexvora.ACTION_SNOOZE_NOTIFICATION"
    const val ACTION_DISMISS_NOTIFICATION = "com.example.nexvora.ACTION_DISMISS_NOTIFICATION"
  }

  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action ?: AlarmScheduler.ACTION_TRIGGER_ALARM
    val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
    val isSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false)
    val snoozeCount = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0)

    when (action) {
      AlarmScheduler.ACTION_UPCOMING_ALARM -> {
        // Handle upcoming alarm alert (30 mins before)
        val triggerTime = intent.getLongExtra("extra_trigger_time", System.currentTimeMillis() + 30 * 60 * 1000L)
        CoroutineScope(Dispatchers.IO).launch {
          val db = AppDatabase.getDatabase(context)
          val alarm = db.alarmDao().getAlarmById(alarmId)
          if (alarm != null && alarm.isEnabled) {
            SmartNotificationManager.showUpcomingAlarmNotification(context, alarm, triggerTime)
          }
        }
      }

      ACTION_DISMISS_UPCOMING -> {
        // User dismissed upcoming alarm before it rang
        SmartNotificationManager.cancelUpcomingNotification(context, alarmId)
        AlarmScheduler.cancelAlarm(context, alarmId)

        CoroutineScope(Dispatchers.IO).launch {
          val db = AppDatabase.getDatabase(context)
          val alarm = db.alarmDao().getAlarmById(alarmId)
          if (alarm != null) {
            if (alarm.repeatDays.isNotBlank()) {
              // Reschedule for next cycle
              AlarmScheduler.scheduleAlarm(context, alarm)
            } else {
              db.alarmDao().updateAlarm(alarm.copy(isEnabled = false))
            }
          }
        }
        Toast.makeText(context, "Upcoming alarm dismissed for today", Toast.LENGTH_SHORT).show()
      }

      ACTION_SNOOZE_NOTIFICATION -> {
        // Snooze directly from notification shade
        val snoozeMins = intent.getIntExtra("extra_snooze_duration", 10)
        val stopServiceIntent = Intent(context, AlarmService::class.java).apply {
          this.action = AlarmService.ACTION_STOP_ALARM
        }
        context.startService(stopServiceIntent)

        if (alarmId != -1L) {
          AlarmScheduler.scheduleSnooze(context, alarmId, snoozeMins, snoozeCount)
          Toast.makeText(context, "Alarm snoozed for $snoozeMins minutes", Toast.LENGTH_SHORT).show()
        }
      }

      ACTION_DISMISS_NOTIFICATION -> {
        // User pressed Dismiss on the notification shade
        CoroutineScope(Dispatchers.IO).launch {
          val db = AppDatabase.getDatabase(context)
          val alarm = if (alarmId != -1L) db.alarmDao().getAlarmById(alarmId) else null
          val challenge = alarm?.challengeType ?: "NONE"

          // If alarm has active challenge (e.g. MATH, MEMORY), require opening app to solve
          if (challenge != "NONE") {
            val activityIntent = Intent(context, AlarmTriggerActivity::class.java).apply {
              flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
              putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
              putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, isSnooze)
              putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, snoozeCount)
            }
            context.startActivity(activityIntent)
          } else {
            // Dismiss immediately
            val stopServiceIntent = Intent(context, AlarmService::class.java).apply {
              this.action = AlarmService.ACTION_STOP_ALARM
            }
            context.startService(stopServiceIntent)

            val streakRepo = StreakRepository(db)
            streakRepo.recordWakeUp(null)
            val streakEntity = db.streakDao().getStreak().firstOrNull()
            val currentStreak = streakEntity?.currentStreak ?: 1

            SmartNotificationManager.showMorningSummaryNotification(
              context = context,
              streakCount = currentStreak,
              alarmLabel = alarm?.label ?: "Alarm",
              goalName = alarm?.goalName ?: ""
            )
          }
        }
      }

      AlarmScheduler.ACTION_TRIGGER_ALARM -> {
        // Cancel any pre-alarm heads-up notice
        SmartNotificationManager.cancelUpcomingNotification(context, alarmId)

        // Wake device screen briefly with WAKE_LOCK
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
          PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
          "nexvora:AlarmReceiverWakeLock"
        )
        wakeLock?.acquire(15000L)

        // 1. Start foreground alarm service
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
          putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
          putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, isSnooze)
          putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, snoozeCount)
        }
        ContextCompat.startForegroundService(context, serviceIntent)

        // 2. Open full-screen activity
        val activityIntent = Intent(context, AlarmTriggerActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
          putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
          putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, isSnooze)
          putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, snoozeCount)
        }
        context.startActivity(activityIntent)

        // 3. Database maintenance
        if (!isSnooze && alarmId != -1L) {
          CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val alarm = db.alarmDao().getAlarmById(alarmId)
            if (alarm != null) {
              if (alarm.repeatDays.isNotBlank()) {
                AlarmScheduler.scheduleAlarm(context, alarm)
              } else {
                db.alarmDao().updateAlarm(alarm.copy(isEnabled = false))
              }
            }
          }
        }
      }
    }
  }
}
