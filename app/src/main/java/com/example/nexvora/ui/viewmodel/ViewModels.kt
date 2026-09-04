package com.example.nexvora.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexvora.alarm.engine.AlarmScheduler
import com.example.nexvora.data.db.AppDatabase
import com.example.nexvora.data.model.*
import com.example.nexvora.data.pref.AppSettings
import com.example.nexvora.data.pref.UserPreferencesRepository
import com.example.nexvora.data.repository.*
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  private val alarmRepo = AlarmRepository(application, database)
  private val routineRepo = RoutineRepository(database)
  private val streakRepo = StreakRepository(database)
  private val sleepRepo = SleepRepository(database)
  private val prefRepo = UserPreferencesRepository(application)

  val appSettings: StateFlow<AppSettings> = prefRepo.appSettingsFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

  val enabledAlarms: StateFlow<List<AlarmEntity>> = alarmRepo.enabledAlarms
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allAlarms: StateFlow<List<AlarmEntity>> = alarmRepo.allAlarms
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allRoutines: StateFlow<List<RoutineEntity>> = routineRepo.allRoutines
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val currentStreak: StateFlow<StreakEntity?> = streakRepo.streakFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val sleepSchedule: StateFlow<SleepScheduleEntity?> = sleepRepo.sleepSchedule
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
  val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

  init {
    // Clock tick
    viewModelScope.launch {
      while (true) {
        _currentTimeMillis.value = System.currentTimeMillis()
        delay(1000L)
      }
    }
    // Check day reset
    viewModelScope.launch {
      routineRepo.checkAndResetDay()
    }
  }

  fun toggleRoutine(routine: RoutineEntity) {
    viewModelScope.launch {
      routineRepo.toggleRoutineCompletion(routine)
    }
  }

  fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
    viewModelScope.launch {
      alarmRepo.toggleAlarm(alarm, isEnabled)
    }
  }

  fun completeOnboarding() {
    viewModelScope.launch {
      prefRepo.setOnboardingCompleted(true)
    }
  }

  fun createDefaultMorningAlarm() {
    viewModelScope.launch {
      val defaultAlarm = AlarmEntity(
        timeHour = 6,
        timeMinute = 30,
        label = "Wake Up & Excel",
        isEnabled = true,
        repeatDays = "1,2,3,4,5",
        isGentleWake = true,
        gentleWakeMinutes = 5,
        isVoiceEnabled = true,
        voiceGreeting = "Good morning! Ready to conquer your day?",
        goalName = "Morning Routine",
        routineName = "Hydration & Stretch",
        challengeType = "MATH",
        challengeDifficulty = "EASY"
      )
      alarmRepo.saveAlarm(defaultAlarm)
    }
  }

  fun calculateNextAlarmInfo(alarms: List<AlarmEntity>, now: Long): Pair<AlarmEntity?, String> {
    if (alarms.isEmpty()) return Pair(null, "No upcoming alarms")
    var nextAlarm: AlarmEntity? = null
    var minDiff = Long.MAX_VALUE

    for (alarm in alarms) {
      if (!alarm.isEnabled) continue
      val trigger = AlarmScheduler.calculateNextTriggerTime(alarm, now)
      val diff = trigger - now
      if (diff in 1..<minDiff) {
        minDiff = diff
        nextAlarm = alarm
      }
    }

    if (nextAlarm == null) return Pair(null, "No upcoming alarms")

    val hours = (minDiff / (1000 * 60 * 60)).toInt()
    val minutes = ((minDiff % (1000 * 60 * 60)) / (1000 * 60)).toInt()
    val timeStr = when {
      hours > 0 && minutes > 0 -> "in ${hours}h ${minutes}m"
      hours > 0 -> "in ${hours}h"
      minutes > 0 -> "in ${minutes}m"
      else -> "in less than a minute"
    }

    return Pair(nextAlarm, timeStr)
  }
}
