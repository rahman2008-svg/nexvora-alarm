package com.example.nexvora.ui.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexvora.data.pref.AppSettings
import com.example.nexvora.data.pref.UserPreferencesRepository
import com.example.nexvora.voice.VoiceAnnouncementManager
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
  private val prefRepo = UserPreferencesRepository(application)
  private val voiceManager = VoiceAnnouncementManager(application)

  val appSettings: StateFlow<AppSettings> = prefRepo.appSettingsFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

  fun updateTheme(mode: AppThemeMode) {
    viewModelScope.launch {
      prefRepo.updateThemeMode(mode)
    }
  }

  fun update24Hour(use24: Boolean) {
    viewModelScope.launch {
      prefRepo.updateUse24HourFormat(use24)
    }
  }

  fun updateTts(enabled: Boolean) {
    viewModelScope.launch {
      prefRepo.updateTtsEnabled(enabled)
    }
  }

  fun updateTtsRate(rate: Float) {
    viewModelScope.launch {
      prefRepo.updateTtsRate(rate)
    }
  }

  fun testTts(rate: Float) {
    voiceManager.setSpeechRate(rate)
    voiceManager.announceAlarm(
      "Good morning!",
      "Mathematics Study",
      "Morning Routine",
      "Hydration"
    )
  }

  fun updateSnooze(minutes: Int, maxSnoozes: Int) {
    viewModelScope.launch {
      prefRepo.updateDefaultSnooze(minutes, maxSnoozes)
    }
  }

  fun updateDefaultChallenge(challenge: String) {
    viewModelScope.launch {
      prefRepo.updateDefaultChallenge(challenge)
    }
  }

  fun canScheduleExactAlarms(): Boolean {
    val context = getApplication<Application>()
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return true
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      alarmManager.canScheduleExactAlarms()
    } else {
      true
    }
  }

  fun areNotificationsEnabled(): Boolean {
    val context = getApplication<Application>()
    return NotificationManagerCompat.from(context).areNotificationsEnabled()
  }

  fun openExactAlarmSettings() {
    val context = getApplication<Application>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:${context.packageName}")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      try {
        context.startActivity(intent)
      } catch (e: Exception) {
        // Fallback to app info settings
        openAppSettings()
      }
    }
  }

  fun openAppSettings() {
    val context = getApplication<Application>()
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.parse("package:${context.packageName}")
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
  }

  fun updateUpcomingNotifications(enabled: Boolean) {
    viewModelScope.launch {
      prefRepo.updateUpcomingNotifications(enabled)
    }
  }

  fun updateMorningNotifications(enabled: Boolean) {
    viewModelScope.launch {
      prefRepo.updateMorningNotifications(enabled)
    }
  }

  fun updateBedtimeNotifications(enabled: Boolean) {
    viewModelScope.launch {
      prefRepo.updateBedtimeNotifications(enabled)
    }
  }

  fun updateVibrationPattern(pattern: String) {
    viewModelScope.launch {
      prefRepo.updateVibrationPattern(pattern)
    }
  }

  fun updateStrobeFlashlight(enabled: Boolean) {
    viewModelScope.launch {
      prefRepo.updateStrobeFlashlight(enabled)
    }
  }

  fun updateMaxRingingDuration(minutes: Int) {
    viewModelScope.launch {
      prefRepo.updateMaxRingingDuration(minutes)
    }
  }

  fun testSmartNotification() {
    val context = getApplication<Application>()
    com.example.nexvora.alarm.engine.SmartNotificationManager.initNotificationChannels(context)
    com.example.nexvora.alarm.engine.SmartNotificationManager.showMorningSummaryNotification(
      context = context,
      streakCount = 7,
      alarmLabel = "Morning Routine (Test)",
      goalName = "Deep Work & Consistency"
    )
  }

  override fun onCleared() {
    super.onCleared()
    voiceManager.shutdown()
  }
}
