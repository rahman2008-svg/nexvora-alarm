package com.example.nexvora.alarm.engine

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.nexvora.data.model.AlarmEntity
import com.example.nexvora.ui.alarm.AlarmTriggerActivity
import java.text.SimpleDateFormat
import java.util.*

object SmartNotificationManager {
  const val CHANNEL_RINGING_ALARM = "nexvora_ringing_alarm"
  const val CHANNEL_UPCOMING_ALERTS = "nexvora_upcoming_alerts"
  const val CHANNEL_SMART_BRIEFING = "nexvora_smart_briefing"

  const val NOTIFICATION_ID_RINGING = 2001
  const val NOTIFICATION_ID_BRIEFING = 3001
  const val BASE_UPCOMING_ID = 5000

  fun initNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return

      // 1. Ringing Alarm Channel (Highest Priority, Heads-up, Bypasses DND)
      val ringingChannel = NotificationChannel(
        CHANNEL_RINGING_ALARM,
        "Active Ringing Alarms",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Full-screen ringing alarm alerts with snooze and dismiss controls"
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        setBypassDnd(true)
        enableVibration(true)
        setShowBadge(true)
      }

      // 2. Upcoming Alarm Channel (Smart Heads-up before alarm rings)
      val upcomingChannel = NotificationChannel(
        CHANNEL_UPCOMING_ALERTS,
        "Upcoming Alarm Notices",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Gentle alerts before an alarm rings with quick dismiss"
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        setShowBadge(false)
      }

      // 3. Smart Briefing & Habit Channel
      val briefingChannel = NotificationChannel(
        CHANNEL_SMART_BRIEFING,
        "Morning Briefings & Sleep Reminders",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Wake-up confirmations, streak updates, and bedtime wind-down reminders"
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
      }

      notificationManager.createNotificationChannels(
        listOf(ringingChannel, upcomingChannel, briefingChannel)
      )
    }
  }

  /**
   * Builds the rich, persistent foreground notification for an actively ringing alarm.
   */
  fun buildRingingNotification(
    context: Context,
    alarmId: Long,
    label: String,
    timeStr: String,
    isSnooze: Boolean,
    snoozeCount: Int,
    snoozeMinutes: Int = 10
  ): Notification {
    initNotificationChannels(context)

    // Full screen launch intent
    val fullScreenIntent = Intent(context, AlarmTriggerActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
      putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
      putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, isSnooze)
      putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, snoozeCount)
    }
    val fullScreenPendingIntent = PendingIntent.getActivity(
      context,
      alarmId.toInt(),
      fullScreenIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Quick Snooze from notification shade
    val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_SNOOZE_NOTIFICATION
      putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
      putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, snoozeCount)
      putExtra("extra_snooze_duration", snoozeMinutes)
    }
    val snoozePendingIntent = PendingIntent.getBroadcast(
      context,
      (alarmId + 200000).toInt(),
      snoozeIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Quick Dismiss from notification shade
    val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_DISMISS_NOTIFICATION
      putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
    }
    val dismissPendingIntent = PendingIntent.getBroadcast(
      context,
      (alarmId + 300000).toInt(),
      dismissIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val contentSubtitle = if (isSnooze) {
      "Snoozed alarm (round $snoozeCount) • $timeStr"
    } else {
      "Scheduled wake-up at $timeStr"
    }

    return NotificationCompat.Builder(context, CHANNEL_RINGING_ALARM)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle(if (label.isNotBlank()) "⏰ $label" else "⏰ NexVora Alarm")
      .setContentText(contentSubtitle)
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText("$contentSubtitle\nTap to open full screen or use quick actions below.")
      )
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setFullScreenIntent(fullScreenPendingIntent, true)
      .setContentIntent(fullScreenPendingIntent)
      .setOngoing(true)
      .setAutoCancel(false)
      .addAction(android.R.drawable.ic_media_pause, "Snooze ($snoozeMinutes m)", snoozePendingIntent)
      .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
      .build()
  }

  /**
   * Shows a smart heads-up notification 30 minutes before the alarm rings.
   * Gives the user the power to dismiss early if already awake.
   */
  fun showUpcomingAlarmNotification(context: Context, alarm: AlarmEntity, triggerTime: Long) {
    initNotificationChannels(context)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val alarmTimeString = sdf.format(Date(triggerTime))

    val remainingMinutes = ((triggerTime - System.currentTimeMillis()) / (60 * 1000L)).coerceAtLeast(1L)

    // Action to dismiss before it rings
    val dismissEarlyIntent = Intent(context, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_DISMISS_UPCOMING
      putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm.id)
    }
    val dismissEarlyPendingIntent = PendingIntent.getBroadcast(
      context,
      (alarm.id + 400000).toInt(),
      dismissEarlyIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val openAppIntent = Intent(context, MainActivity::class.java)
    val openAppPendingIntent = PendingIntent.getActivity(
      context,
      (alarm.id + 500000).toInt(),
      openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val label = if (alarm.label.isNotBlank()) alarm.label else "Alarm"

    val notification = NotificationCompat.Builder(context, CHANNEL_UPCOMING_ALERTS)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle("⏰ Upcoming Alarm: $label")
      .setContentText("Rings in $remainingMinutes min at $alarmTimeString")
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText("Your $label alarm is scheduled for $alarmTimeString (in $remainingMinutes minutes).\nAlready awake? Dismiss now so it won't ring.")
      )
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setContentIntent(openAppPendingIntent)
      .setAutoCancel(true)
      .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss Now", dismissEarlyPendingIntent)
      .build()

    notificationManager.notify((BASE_UPCOMING_ID + alarm.id).toInt(), notification)
  }

  fun cancelUpcomingNotification(context: Context, alarmId: Long) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    notificationManager?.cancel((BASE_UPCOMING_ID + alarmId).toInt())
  }

  /**
   * Displays a smart morning briefing notification after alarm is dismissed.
   */
  fun showMorningSummaryNotification(
    context: Context,
    streakCount: Int,
    alarmLabel: String,
    goalName: String
  ) {
    initNotificationChannels(context)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

    val openAppIntent = Intent(context, MainActivity::class.java)
    val openAppPendingIntent = PendingIntent.getActivity(
      context,
      600001,
      openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val title = "🌅 Good Morning! Wake-up recorded"
    val content = if (streakCount > 1) {
      "🔥 $streakCount-day wake-up streak active! Keep the momentum."
    } else {
      "First step to building an unbreakable morning routine."
    }

    val goalSnippet = if (goalName.isNotBlank()) "\n🎯 Today's Goal: $goalName" else ""

    val notification = NotificationCompat.Builder(context, CHANNEL_SMART_BRIEFING)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle(title)
      .setContentText(content)
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText("$content$goalSnippet\nHave a productive and focused day!")
      )
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(openAppPendingIntent)
      .setAutoCancel(true)
      .build()

    notificationManager.notify(NOTIFICATION_ID_BRIEFING, notification)
  }

  /**
   * Displays a bedtime reminder notification.
   */
  fun showBedtimeReminderNotification(context: Context, targetBedtimeStr: String) {
    initNotificationChannels(context)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

    val openAppIntent = Intent(context, MainActivity::class.java)
    val openAppPendingIntent = PendingIntent.getActivity(
      context,
      600002,
      openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_SMART_BRIEFING)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle("🌙 Time to Wind Down")
      .setContentText("Target bedtime is $targetBedtimeStr. Dim lights for quality sleep.")
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(openAppPendingIntent)
      .setAutoCancel(true)
      .build()

    notificationManager.notify(NOTIFICATION_ID_BRIEFING + 1, notification)
  }
}
