package com.example.nexvora.ui.viewmodel

import android.app.Application
import android.media.RingtoneManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexvora.data.db.AppDatabase
import com.example.nexvora.data.model.StudySessionEntity
import com.example.nexvora.data.model.WorldClockEntity
import com.example.nexvora.data.repository.ClockRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class CityOption(val city: String, val country: String, val zoneId: String)

enum class PomodoroState {
  STUDY,
  BREAK,
  IDLE
}

data class StopwatchLap(val lapNumber: Int, val lapTimeMillis: Long, val totalTimeMillis: Long)

class ClockViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  private val clockRepo = ClockRepository(database)

  val worldClocks: StateFlow<List<WorldClockEntity>> = clockRepo.allWorldClocks
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val studySessions: StateFlow<List<StudySessionEntity>> = clockRepo.allStudySessions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Current time ticker
  private val _clockTicker = MutableStateFlow(System.currentTimeMillis())
  val clockTicker: StateFlow<Long> = _clockTicker.asStateFlow()

  // Predefined cities list for searching
  val availableCities = listOf(
    CityOption("Dhaka", "Bangladesh", "Asia/Dhaka"),
    CityOption("London", "United Kingdom", "Europe/London"),
    CityOption("New York", "United States", "America/New_York"),
    CityOption("Tokyo", "Japan", "Asia/Tokyo"),
    CityOption("Paris", "France", "Europe/Paris"),
    CityOption("Sydney", "Australia", "Australia/Sydney"),
    CityOption("Dubai", "United Arab Emirates", "Asia/Dubai"),
    CityOption("Singapore", "Singapore", "Asia/Singapore"),
    CityOption("Los Angeles", "United States", "America/Los_Angeles"),
    CityOption("Berlin", "Germany", "Europe/Berlin"),
    CityOption("Toronto", "Canada", "America/Toronto"),
    CityOption("Mumbai", "India", "Asia/Kolkata"),
    CityOption("Cairo", "Egypt", "Africa/Cairo"),
    CityOption("São Paulo", "Brazil", "America/Sao_Paulo"),
    CityOption("Hong Kong", "Hong Kong", "Asia/Hong_Kong"),
    CityOption("Seoul", "South Korea", "Asia/Seoul"),
    CityOption("Amsterdam", "Netherlands", "Europe/Amsterdam"),
    CityOption("Zurich", "Switzerland", "Europe/Zurich"),
    CityOption("San Francisco", "United States", "America/Los_Angeles")
  )

  // --- Pomodoro / Study Mode State ---
  private val _pomodoroState = MutableStateFlow(PomodoroState.IDLE)
  val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()

  private val _pomodoroIsRunning = MutableStateFlow(false)
  val pomodoroIsRunning: StateFlow<Boolean> = _pomodoroIsRunning.asStateFlow()

  private val _pomodoroSecondsLeft = MutableStateFlow(25 * 60)
  val pomodoroSecondsLeft: StateFlow<Int> = _pomodoroSecondsLeft.asStateFlow()

  var studyDurationMinutes: Int = 25
  var breakDurationMinutes: Int = 5
  private var pomodoroJob: Job? = null

  // --- Timer State ---
  private val _timerIsRunning = MutableStateFlow(false)
  val timerIsRunning: StateFlow<Boolean> = _timerIsRunning.asStateFlow()

  private val _timerTotalSeconds = MutableStateFlow(5 * 60)
  val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

  private val _timerSecondsLeft = MutableStateFlow(5 * 60)
  val timerSecondsLeft: StateFlow<Int> = _timerSecondsLeft.asStateFlow()

  private var timerJob: Job? = null

  // --- Stopwatch State ---
  private val _stopwatchIsRunning = MutableStateFlow(false)
  val stopwatchIsRunning: StateFlow<Boolean> = _stopwatchIsRunning.asStateFlow()

  private val _stopwatchElapsedMillis = MutableStateFlow(0L)
  val stopwatchElapsedMillis: StateFlow<Long> = _stopwatchElapsedMillis.asStateFlow()

  private val _stopwatchLaps = MutableStateFlow<List<StopwatchLap>>(emptyList())
  val stopwatchLaps: StateFlow<List<StopwatchLap>> = _stopwatchLaps.asStateFlow()

  private var stopwatchJob: Job? = null
  private var stopwatchStartTime = 0L
  private var stopwatchBaseTime = 0L

  init {
    viewModelScope.launch {
      while (true) {
        _clockTicker.value = System.currentTimeMillis()
        delay(1000L)
      }
    }

    viewModelScope.launch {
      val existing = clockRepo.allWorldClocks.first()
      if (existing.none { it.cityName.equals("Dhaka", ignoreCase = true) }) {
        clockRepo.addWorldClock("Dhaka", "Bangladesh", "Asia/Dhaka")
      }
    }
  }

  // --- World Clock Actions ---
  fun addCity(option: CityOption) {
    viewModelScope.launch {
      clockRepo.addWorldClock(option.city, option.country, option.zoneId)
    }
  }

  fun deleteCity(clock: WorldClockEntity) {
    viewModelScope.launch {
      clockRepo.deleteWorldClock(clock)
    }
  }

  // --- Study / Pomodoro Actions ---
  fun startPomodoro(isStudy: Boolean = true) {
    pomodoroJob?.cancel()
    _pomodoroState.value = if (isStudy) PomodoroState.STUDY else PomodoroState.BREAK
    val targetSeconds = if (isStudy) studyDurationMinutes * 60 else breakDurationMinutes * 60
    _pomodoroSecondsLeft.value = targetSeconds
    _pomodoroIsRunning.value = true

    pomodoroJob = viewModelScope.launch {
      while (_pomodoroSecondsLeft.value > 0 && _pomodoroIsRunning.value) {
        delay(1000L)
        _pomodoroSecondsLeft.value -= 1
      }
      if (_pomodoroSecondsLeft.value <= 0) {
        _pomodoroIsRunning.value = false
        if (_pomodoroState.value == PomodoroState.STUDY) {
          clockRepo.recordStudySession(studyDurationMinutes)
        }
        playCompletionBeep()
      }
    }
  }

  fun pausePomodoro() {
    _pomodoroIsRunning.value = false
    pomodoroJob?.cancel()
  }

  fun resumePomodoro() {
    if (_pomodoroSecondsLeft.value <= 0) return
    _pomodoroIsRunning.value = true
    pomodoroJob = viewModelScope.launch {
      while (_pomodoroSecondsLeft.value > 0 && _pomodoroIsRunning.value) {
        delay(1000L)
        _pomodoroSecondsLeft.value -= 1
      }
      if (_pomodoroSecondsLeft.value <= 0) {
        _pomodoroIsRunning.value = false
        playCompletionBeep()
      }
    }
  }

  fun resetPomodoro() {
    pomodoroJob?.cancel()
    _pomodoroIsRunning.value = false
    _pomodoroState.value = PomodoroState.IDLE
    _pomodoroSecondsLeft.value = studyDurationMinutes * 60
  }

  // --- Timer Actions ---
  fun setTimerDuration(seconds: Int) {
    _timerTotalSeconds.value = seconds
    _timerSecondsLeft.value = seconds
  }

  fun startTimer() {
    if (_timerSecondsLeft.value <= 0) return
    _timerIsRunning.value = true
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (_timerSecondsLeft.value > 0 && _timerIsRunning.value) {
        delay(1000L)
        _timerSecondsLeft.value -= 1
      }
      if (_timerSecondsLeft.value <= 0) {
        _timerIsRunning.value = false
        playCompletionBeep()
      }
    }
  }

  fun pauseTimer() {
    _timerIsRunning.value = false
    timerJob?.cancel()
  }

  fun resetTimer() {
    timerJob?.cancel()
    _timerIsRunning.value = false
    _timerSecondsLeft.value = _timerTotalSeconds.value
  }

  // --- Stopwatch Actions ---
  fun startStopwatch() {
    if (_stopwatchIsRunning.value) return
    _stopwatchIsRunning.value = true
    stopwatchStartTime = System.currentTimeMillis()

    stopwatchJob = viewModelScope.launch {
      while (_stopwatchIsRunning.value) {
        val now = System.currentTimeMillis()
        _stopwatchElapsedMillis.value = stopwatchBaseTime + (now - stopwatchStartTime)
        delay(16L) // ~60fps smooth render
      }
    }
  }

  fun pauseStopwatch() {
    if (!_stopwatchIsRunning.value) return
    _stopwatchIsRunning.value = false
    stopwatchJob?.cancel()
    stopwatchBaseTime = _stopwatchElapsedMillis.value
  }

  fun resetStopwatch() {
    stopwatchJob?.cancel()
    _stopwatchIsRunning.value = false
    _stopwatchElapsedMillis.value = 0L
    stopwatchBaseTime = 0L
    _stopwatchLaps.value = emptyList()
  }

  fun recordLap() {
    val totalTime = _stopwatchElapsedMillis.value
    val laps = _stopwatchLaps.value
    val previousTotal = laps.firstOrNull()?.totalTimeMillis ?: 0L
    val lapTime = totalTime - previousTotal
    val newLap = StopwatchLap(
      lapNumber = laps.size + 1,
      lapTimeMillis = lapTime,
      totalTimeMillis = totalTime
    )
    _stopwatchLaps.value = listOf(newLap) + laps
  }

  private fun playCompletionBeep() {
    try {
      val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      val r = RingtoneManager.getRingtone(getApplication(), notification)
      r.play()
    } catch (e: Exception) {
      // Ignored
    }
  }
}
