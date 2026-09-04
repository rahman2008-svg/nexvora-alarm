package com.example.nexvora.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nexvora_settings")

data class AppSettings(
  val themeMode: AppThemeMode = AppThemeMode.DARK,
  val use24HourFormat: Boolean = false,
  val isTtsEnabled: Boolean = true,
  val ttsRate: Float = 1.0f,
  val defaultSnoozeMinutes: Int = 10,
  val defaultMaxSnoozes: Int = 3,
  val defaultChallenge: String = "NONE",
  val hasCompletedOnboarding: Boolean = false,
  val defaultGentleWake: Boolean = true,
  val defaultGentleDuration: Int = 5,
  val defaultVibration: Boolean = true,
  val defaultVolume: Float = 0.85f,
  val upcomingAlarmNotifications: Boolean = true,
  val morningSummaryNotifications: Boolean = true,
  val bedtimeReminderNotifications: Boolean = true,
  val vibrationPattern: String = "STANDARD",
  val strobeFlashlight: Boolean = false,
  val maxRingingDurationMinutes: Int = 10
)

class UserPreferencesRepository(private val context: Context) {

  private object Keys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
    val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
    val TTS_RATE = floatPreferencesKey("tts_rate")
    val DEFAULT_SNOOZE_MINUTES = intPreferencesKey("default_snooze_minutes")
    val DEFAULT_MAX_SNOOZES = intPreferencesKey("default_max_snoozes")
    val DEFAULT_CHALLENGE = stringPreferencesKey("default_challenge")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val DEFAULT_GENTLE_WAKE = booleanPreferencesKey("default_gentle_wake")
    val DEFAULT_GENTLE_DURATION = intPreferencesKey("default_gentle_duration")
    val DEFAULT_VIBRATION = booleanPreferencesKey("default_vibration")
    val DEFAULT_VOLUME = floatPreferencesKey("default_volume")
    val UPCOMING_NOTIFICATIONS = booleanPreferencesKey("upcoming_notifications")
    val MORNING_NOTIFICATIONS = booleanPreferencesKey("morning_notifications")
    val BEDTIME_NOTIFICATIONS = booleanPreferencesKey("bedtime_notifications")
    val VIBRATION_PATTERN = stringPreferencesKey("vibration_pattern")
    val STROBE_FLASHLIGHT = booleanPreferencesKey("strobe_flashlight")
    val MAX_RINGING_DURATION = intPreferencesKey("max_ringing_duration")
  }

  val appSettingsFlow: Flow<AppSettings> = context.dataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { preferences ->
      val themeStr = preferences[Keys.THEME_MODE] ?: AppThemeMode.DARK.name
      val themeMode = try {
        AppThemeMode.valueOf(themeStr)
      } catch (e: Exception) {
        AppThemeMode.DARK
      }

      AppSettings(
        themeMode = themeMode,
        use24HourFormat = preferences[Keys.USE_24_HOUR] ?: false,
        isTtsEnabled = preferences[Keys.TTS_ENABLED] ?: true,
        ttsRate = preferences[Keys.TTS_RATE] ?: 1.0f,
        defaultSnoozeMinutes = preferences[Keys.DEFAULT_SNOOZE_MINUTES] ?: 10,
        defaultMaxSnoozes = preferences[Keys.DEFAULT_MAX_SNOOZES] ?: 3,
        defaultChallenge = preferences[Keys.DEFAULT_CHALLENGE] ?: "NONE",
        hasCompletedOnboarding = preferences[Keys.ONBOARDING_COMPLETED] ?: false,
        defaultGentleWake = preferences[Keys.DEFAULT_GENTLE_WAKE] ?: true,
        defaultGentleDuration = preferences[Keys.DEFAULT_GENTLE_DURATION] ?: 5,
        defaultVibration = preferences[Keys.DEFAULT_VIBRATION] ?: true,
        defaultVolume = preferences[Keys.DEFAULT_VOLUME] ?: 0.85f,
        upcomingAlarmNotifications = preferences[Keys.UPCOMING_NOTIFICATIONS] ?: true,
        morningSummaryNotifications = preferences[Keys.MORNING_NOTIFICATIONS] ?: true,
        bedtimeReminderNotifications = preferences[Keys.BEDTIME_NOTIFICATIONS] ?: true,
        vibrationPattern = preferences[Keys.VIBRATION_PATTERN] ?: "STANDARD",
        strobeFlashlight = preferences[Keys.STROBE_FLASHLIGHT] ?: false,
        maxRingingDurationMinutes = preferences[Keys.MAX_RINGING_DURATION] ?: 10
      )
    }

  suspend fun updateThemeMode(mode: AppThemeMode) {
    context.dataStore.edit { preferences ->
      preferences[Keys.THEME_MODE] = mode.name
    }
  }

  suspend fun updateUse24HourFormat(use24Hour: Boolean) {
    context.dataStore.edit { preferences ->
      preferences[Keys.USE_24_HOUR] = use24Hour
    }
  }

  suspend fun updateTtsEnabled(enabled: Boolean) {
    context.dataStore.edit { preferences ->
      preferences[Keys.TTS_ENABLED] = enabled
    }
  }

  suspend fun updateTtsRate(rate: Float) {
    context.dataStore.edit { preferences ->
      preferences[Keys.TTS_RATE] = rate
    }
  }

  suspend fun updateDefaultSnooze(minutes: Int, maxSnoozes: Int) {
    context.dataStore.edit { preferences ->
      preferences[Keys.DEFAULT_SNOOZE_MINUTES] = minutes
      preferences[Keys.DEFAULT_MAX_SNOOZES] = maxSnoozes
    }
  }

  suspend fun updateDefaultChallenge(challenge: String) {
    context.dataStore.edit { preferences ->
      preferences[Keys.DEFAULT_CHALLENGE] = challenge
    }
  }

  suspend fun setOnboardingCompleted(completed: Boolean = true) {
    context.dataStore.edit { preferences ->
      preferences[Keys.ONBOARDING_COMPLETED] = completed
    }
  }

  suspend fun updateDefaultGentleWake(enabled: Boolean, minutes: Int) {
    context.dataStore.edit { preferences ->
      preferences[Keys.DEFAULT_GENTLE_WAKE] = enabled
      preferences[Keys.DEFAULT_GENTLE_DURATION] = minutes
    }
  }

  suspend fun updateDefaultVibration(enabled: Boolean) {
    context.dataStore.edit { preferences ->
      preferences[Keys.DEFAULT_VIBRATION] = enabled
    }
  }

  suspend fun updateDefaultVolume(volume: Float) {
    context.dataStore.edit { preferences ->
      preferences[Keys.DEFAULT_VOLUME] = volume
    }
  }

  suspend fun updateUpcomingNotifications(enabled: Boolean) {
    context.dataStore.edit { preferences ->
      preferences[Keys.UPCOMING_NOTIFICATIONS] = enabled
    }
  }

  suspend fun updateMorningNotifications(enabled: Boolean) {
    context.dataStore.edit { preferences ->
      preferences[Keys.MORNING_NOTIFICATIONS] = enabled
    }
  }

  suspend fun updateBedtimeNotifications(enabled: Boolean) {
    context.dataStore.edit { preferences ->
      preferences[Keys.BEDTIME_NOTIFICATIONS] = enabled
    }
  }

  suspend fun updateVibrationPattern(pattern: String) {
    context.dataStore.edit { preferences ->
      preferences[Keys.VIBRATION_PATTERN] = pattern
    }
  }

  suspend fun updateStrobeFlashlight(enabled: Boolean) {
    context.dataStore.edit { preferences ->
      preferences[Keys.STROBE_FLASHLIGHT] = enabled
    }
  }

  suspend fun updateMaxRingingDuration(minutes: Int) {
    context.dataStore.edit { preferences ->
      preferences[Keys.MAX_RINGING_DURATION] = minutes
    }
  }
}
