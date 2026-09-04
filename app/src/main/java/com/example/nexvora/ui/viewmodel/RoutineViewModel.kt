package com.example.nexvora.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexvora.data.db.AppDatabase
import com.example.nexvora.data.model.GoalEntity
import com.example.nexvora.data.model.RoutineEntity
import com.example.nexvora.data.repository.GoalRepository
import com.example.nexvora.data.repository.RoutineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutineViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  private val routineRepo = RoutineRepository(database)
  private val goalRepo = GoalRepository(database)

  val allRoutines: StateFlow<List<RoutineEntity>> = routineRepo.allRoutines
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allGoals: StateFlow<List<GoalEntity>> = goalRepo.allGoals
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun toggleRoutine(routine: RoutineEntity) {
    viewModelScope.launch {
      routineRepo.toggleRoutineCompletion(routine)
    }
  }

  fun saveRoutine(routine: RoutineEntity, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      routineRepo.saveRoutine(routine)
      onComplete()
    }
  }

  fun deleteRoutine(routine: RoutineEntity) {
    viewModelScope.launch {
      routineRepo.deleteRoutine(routine)
    }
  }

  fun toggleGoal(goal: GoalEntity) {
    viewModelScope.launch {
      goalRepo.toggleGoalCompletion(goal)
    }
  }

  fun saveGoal(goal: GoalEntity, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      goalRepo.saveGoal(goal)
      onComplete()
    }
  }

  fun deleteGoal(goal: GoalEntity) {
    viewModelScope.launch {
      goalRepo.deleteGoal(goal)
    }
  }
}
