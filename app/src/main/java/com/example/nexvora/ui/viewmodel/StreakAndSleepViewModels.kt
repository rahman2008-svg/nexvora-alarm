package com.example.nexvora.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexvora.data.db.AppDatabase
import com.example.nexvora.data.model.AchievementEntity
import com.example.nexvora.data.model.SleepLogEntity
import com.example.nexvora.data.model.SleepScheduleEntity
import com.example.nexvora.data.model.StreakEntity
import com.example.nexvora.data.repository.SleepRepository
import com.example.nexvora.data.repository.StreakRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StreakViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  private val streakRepo = StreakRepository(database)

  val streak: StateFlow<StreakEntity?> = streakRepo.streakFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val achievements: StateFlow<List<AchievementEntity>> = streakRepo.achievementsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class SleepViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  private val sleepRepo = SleepRepository(database)

  val sleepSchedule: StateFlow<SleepScheduleEntity?> = sleepRepo.sleepSchedule
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val recentLogs: StateFlow<List<SleepLogEntity>> = sleepRepo.recentSleepLogs
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun updateSchedule(schedule: SleepScheduleEntity) {
    viewModelScope.launch {
      sleepRepo.updateSleepSchedule(schedule)
    }
  }

  fun logSleep(bedtimeMillis: Long, wakeMillis: Long, quality: Int) {
    viewModelScope.launch {
      sleepRepo.logSleepSession(bedtimeMillis, wakeMillis, quality)
    }
  }
}
