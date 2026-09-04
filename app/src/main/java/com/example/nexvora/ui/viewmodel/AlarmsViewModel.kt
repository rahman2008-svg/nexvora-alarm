package com.example.nexvora.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexvora.data.db.AppDatabase
import com.example.nexvora.data.model.AlarmEntity
import com.example.nexvora.data.repository.AlarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmsViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  private val repository = AlarmRepository(application, database)

  val allAlarms: StateFlow<List<AlarmEntity>> = repository.allAlarms
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
    viewModelScope.launch {
      repository.toggleAlarm(alarm, isEnabled)
    }
  }

  fun saveAlarm(alarm: AlarmEntity, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.saveAlarm(alarm)
      onComplete()
    }
  }

  fun deleteAlarm(alarm: AlarmEntity) {
    viewModelScope.launch {
      repository.deleteAlarm(alarm)
    }
  }
}
