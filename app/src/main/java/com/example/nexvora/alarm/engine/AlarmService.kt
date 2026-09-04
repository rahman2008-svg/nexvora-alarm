package com.example.nexvora.alarm.engine

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.nexvora.data.db.AppDatabase
import com.example.nexvora.data.pref.UserPreferencesRepository
import com.example.nexvora.voice.VoiceAnnouncementManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class AlarmService : Service() {
  private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
  private var audioAndVibManager: AudioAndVibrationManager? = null
  private var voiceManager: VoiceAnnouncementManager? = null
  private var autoSnoozeJob: Job? = null

  companion object {
    const val NOTIFICATION_ID = 1001
    const val ACTION_STOP_ALARM = "com.example.nexvora.ACTION_STOP_ALARM"

    @Volatile
    var isRunning = false
      private set
  }

  override fun onCreate() {
    super.onCreate()
    isRunning = true
    SmartNotificationManager.initNotificationChannels(this)
    audioAndVibManager = AudioAndVibrationManager(this)
    voiceManager = VoiceAnnouncementManager(this)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == ACTION_STOP_ALARM) {
      stopSelf()
      return START_NOT_STICKY
    }

    val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L) ?: -1L
    val isSnooze = intent?.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false) ?: false
    val snoozeCount = intent?.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0) ?: 0

    serviceScope.launch {
      val database = AppDatabase.getDatabase(applicationContext)
      val prefsRepo = UserPreferencesRepository(applicationContext)
      val appSettings = prefsRepo.appSettingsFlow.first()

      val alarm = if (alarmId != -1L) database.alarmDao().getAlarmById(alarmId) else null

      val label = alarm?.label?.ifBlank { "Wake Up" } ?: "Wake Up"
      val ringtoneUri = alarm?.ringtoneUri ?: ""
      val volume = alarm?.volume ?: appSettings.defaultVolume
      val isGentle = alarm?.isGentleWake ?: appSettings.defaultGentleWake
      val gentleMins = alarm?.gentleWakeMinutes ?: appSettings.defaultGentleDuration
      val shouldVibrate = alarm?.isVibrate ?: appSettings.defaultVibration
      val isVoice = alarm?.isVoiceEnabled ?: appSettings.isTtsEnabled
      val greeting = alarm?.voiceGreeting?.ifBlank { "Good morning! Time to wake up." } ?: "Good morning! Time to wake up."
      val goal = alarm?.goalName ?: ""
      val routine = alarm?.routineName ?: ""
      val snoozeMins = alarm?.snoozeDurationMinutes ?: appSettings.defaultSnoozeMinutes

      // Format time string
      val timeFormat = SimpleDateFormat(if (appSettings.use24HourFormat) "HH:mm" else "hh:mm a", Locale.getDefault())
      val timeStr = timeFormat.format(Date())

      // 1. Start Rich Foreground Smart Notification
      val notification = SmartNotificationManager.buildRingingNotification(
        context = this@AlarmService,
        alarmId = alarmId,
        label = label,
        timeStr = timeStr,
        isSnooze = isSnooze,
        snoozeCount = snoozeCount,
        snoozeMinutes = snoozeMins
      )
      startForeground(NOTIFICATION_ID, notification)

      // 2. Start Audio, Vibration & Strobe
      audioAndVibManager?.startAlarmSoundAndVibration(
        ringtoneUriStr = ringtoneUri,
        targetVolume = volume,
        isGentleWake = isGentle,
        gentleWakeMinutes = gentleMins,
        shouldVibrate = shouldVibrate,
        vibrationPatternType = "STANDARD",
        strobeFlash = false
      )

      // 3. Optional TTS announcement
      if (isVoice) {
        delay(2200L) // Allow ringtone to ramp before voice greeting
        voiceManager?.announceAlarm(greeting, label, goal, routine)
      }

      // 4. Auto-Snooze Safety Timer: 10 minutes max ringing if unattended
      autoSnoozeJob?.cancel()
      autoSnoozeJob = serviceScope.launch {
        delay(10 * 60 * 1000L)
        if (isRunning) {
          if (alarmId != -1L) {
            AlarmScheduler.scheduleSnooze(applicationContext, alarmId, snoozeMins, snoozeCount)
          }
          stopSelf()
        }
      }
    }

    return START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()
    isRunning = false
    autoSnoozeJob?.cancel()
    serviceScope.cancel()
    audioAndVibManager?.stop()
    voiceManager?.shutdown()
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
