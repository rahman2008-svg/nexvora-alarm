package com.example.nexvora.alarm.engine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.nexvora.data.model.AlarmEntity
import java.util.Calendar

object AlarmScheduler {
  private const val TAG = "AlarmScheduler"
  const val EXTRA_ALARM_ID = "extra_alarm_id"
  const val EXTRA_IS_SNOOZE = "extra_is_snooze"
  const val EXTRA_SNOOZE_COUNT = "extra_snooze_count"
  const val ACTION_TRIGGER_ALARM = "com.example.nexvora.ACTION_TRIGGER_ALARM"
  const val ACTION_UPCOMING_ALARM = "com.example.nexvora.ACTION_UPCOMING_ALARM"

  /**
   * Calculates the next trigger timestamp (millis) for an alarm.
   */
  fun calculateNextTriggerTime(alarm: AlarmEntity, fromMillis: Long = System.currentTimeMillis()): Long {
    val now = Calendar.getInstance().apply { timeInMillis = fromMillis }
    val target = Calendar.getInstance().apply {
      timeInMillis = fromMillis
      set(Calendar.HOUR_OF_DAY, alarm.timeHour)
      set(Calendar.MINUTE, alarm.timeMinute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }

    val repeatDays = parseRepeatDays(alarm.repeatDays)

    if (repeatDays.isEmpty()) {
      // One-time alarm
      if (target.timeInMillis <= now.timeInMillis) {
        target.add(Calendar.DAY_OF_YEAR, 1)
      }
      return target.timeInMillis
    }

    // Repeating alarm: Find next matching day
    for (dayOffset in 0..7) {
      val candidate = Calendar.getInstance().apply {
        timeInMillis = target.timeInMillis
        add(Calendar.DAY_OF_YEAR, dayOffset)
      }

      if (dayOffset == 0 && candidate.timeInMillis <= now.timeInMillis) {
        continue
      }

      val standardDay = candidate.get(Calendar.DAY_OF_WEEK)
      val appDayNumber = when (standardDay) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 1
      }

      if (repeatDays.contains(appDayNumber)) {
        return candidate.timeInMillis
      }
    }

    return target.timeInMillis + 24 * 60 * 60 * 1000L
  }

  fun parseRepeatDays(repeatDaysStr: String): Set<Int> {
    if (repeatDaysStr.isBlank()) return emptySet()
    return repeatDaysStr.split(",")
      .mapNotNull { it.trim().toIntOrNull() }
      .toSet()
  }

  fun formatRepeatDays(days: Set<Int>): String {
    return days.sorted().joinToString(",")
  }

  fun scheduleAlarm(context: Context, alarm: AlarmEntity) {
    if (!alarm.isEnabled) {
      cancelAlarm(context, alarm.id)
      return
    }

    val triggerTime = calculateNextTriggerTime(alarm)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = ACTION_TRIGGER_ALARM
      putExtra(EXTRA_ALARM_ID, alarm.id)
      putExtra(EXTRA_IS_SNOOZE, false)
      putExtra(EXTRA_SNOOZE_COUNT, 0)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      alarm.id.toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val showIntent = Intent(context, MainActivity::class.java)
    val showPendingIntent = PendingIntent.getActivity(
      context,
      alarm.id.toInt(),
      showIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
          val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
          alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } else {
          alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
      } else {
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
      }
      Log.d(TAG, "Scheduled alarm ${alarm.id} for triggerTime: $triggerTime")
    } catch (e: SecurityException) {
      Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
      alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    // Schedule Upcoming Smart Alert (30 minutes prior to ringing)
    val upcomingTime = triggerTime - (30 * 60 * 1000L)
    if (upcomingTime > System.currentTimeMillis() + 60 * 1000L) {
      val upcomingIntent = Intent(context, AlarmReceiver::class.java).apply {
        action = ACTION_UPCOMING_ALARM
        putExtra(EXTRA_ALARM_ID, alarm.id)
        putExtra("extra_trigger_time", triggerTime)
      }
      val upcomingPendingIntent = PendingIntent.getBroadcast(
        context,
        (alarm.id + 600000).toInt(),
        upcomingIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      try {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, upcomingTime, upcomingPendingIntent)
      } catch (e: Exception) {
        Log.w(TAG, "Could not schedule upcoming alert: ${e.message}")
      }
    }
  }

  fun scheduleSnooze(context: Context, alarmId: Long, snoozeMinutes: Int, currentSnoozeCount: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = ACTION_TRIGGER_ALARM
      putExtra(EXTRA_ALARM_ID, alarmId)
      putExtra(EXTRA_IS_SNOOZE, true)
      putExtra(EXTRA_SNOOZE_COUNT, currentSnoozeCount + 1)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      (alarmId + 100000).toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val showIntent = Intent(context, MainActivity::class.java)
    val showPendingIntent = PendingIntent.getActivity(
      context,
      (alarmId + 100000).toInt(),
      showIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
      alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
      Log.d(TAG, "Scheduled snooze for alarm $alarmId in $snoozeMinutes minutes")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to schedule snooze: ${e.message}")
      alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }
  }

  fun cancelAlarm(context: Context, alarmId: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = ACTION_TRIGGER_ALARM
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      alarmId.toInt(),
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }

    val snoozePendingIntent = PendingIntent.getBroadcast(
      context,
      (alarmId + 100000).toInt(),
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (snoozePendingIntent != null) {
      alarmManager.cancel(snoozePendingIntent)
      snoozePendingIntent.cancel()
    }

    val upcomingPendingIntent = PendingIntent.getBroadcast(
      context,
      (alarmId + 600000).toInt(),
      Intent(context, AlarmReceiver::class.java).apply { action = ACTION_UPCOMING_ALARM },
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (upcomingPendingIntent != null) {
      alarmManager.cancel(upcomingPendingIntent)
      upcomingPendingIntent.cancel()
    }

    SmartNotificationManager.cancelUpcomingNotification(context, alarmId)
  }
}
