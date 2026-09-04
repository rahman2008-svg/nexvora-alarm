package com.example.nexvora.data.repository

import android.content.Context
import com.example.nexvora.alarm.engine.AlarmScheduler
import com.example.nexvora.data.db.AppDatabase
import com.example.nexvora.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class AlarmRepository(private val context: Context, private val database: AppDatabase) {
  val allAlarms: Flow<List<AlarmEntity>> = database.alarmDao().getAllAlarms()
  val enabledAlarms: Flow<List<AlarmEntity>> = database.alarmDao().getEnabledAlarms()

  suspend fun getAlarmById(id: Long): AlarmEntity? = database.alarmDao().getAlarmById(id)

  suspend fun saveAlarm(alarm: AlarmEntity): Long {
    val id = database.alarmDao().insertAlarm(alarm)
    val savedAlarm = alarm.copy(id = if (alarm.id == 0L) id else alarm.id)
    if (savedAlarm.isEnabled) {
      AlarmScheduler.scheduleAlarm(context, savedAlarm)
    } else {
      AlarmScheduler.cancelAlarm(context, savedAlarm.id)
    }
    return id
  }

  suspend fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
    val updated = alarm.copy(isEnabled = isEnabled, snoozeCount = 0)
    database.alarmDao().updateAlarm(updated)
    if (isEnabled) {
      AlarmScheduler.scheduleAlarm(context, updated)
    } else {
      AlarmScheduler.cancelAlarm(context, updated.id)
    }
  }

  suspend fun deleteAlarm(alarm: AlarmEntity) {
    AlarmScheduler.cancelAlarm(context, alarm.id)
    database.alarmDao().deleteAlarm(alarm)
  }
}

class RoutineRepository(private val database: AppDatabase) {
  val allRoutines: Flow<List<RoutineEntity>> = database.routineDao().getAllRoutines()

  suspend fun saveRoutine(routine: RoutineEntity): Long = database.routineDao().insertRoutine(routine)

  suspend fun toggleRoutineCompletion(routine: RoutineEntity) {
    val today = getTodayDateString()
    val isCompleted = !routine.isCompletedToday
    val updated = routine.copy(
      isCompletedToday = isCompleted,
      lastCompletedDate = if (isCompleted) today else routine.lastCompletedDate
    )
    database.routineDao().updateRoutine(updated)
  }

  suspend fun deleteRoutine(routine: RoutineEntity) = database.routineDao().deleteRoutine(routine)

  suspend fun checkAndResetDay() {
    database.routineDao().resetCompletedForNewDay(getTodayDateString())
  }

  companion object {
    fun getTodayDateString(): String {
      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      return sdf.format(Date())
    }
  }
}

class GoalRepository(private val database: AppDatabase) {
  val allGoals: Flow<List<GoalEntity>> = database.goalDao().getAllGoals()

  suspend fun saveGoal(goal: GoalEntity): Long = database.goalDao().insertGoal(goal)

  suspend fun toggleGoalCompletion(goal: GoalEntity) {
    val updated = goal.copy(
      isCompletedToday = !goal.isCompletedToday,
      streakCount = if (!goal.isCompletedToday) goal.streakCount + 1 else (goal.streakCount - 1).coerceAtLeast(0)
    )
    database.goalDao().updateGoal(updated)
  }

  suspend fun deleteGoal(goal: GoalEntity) = database.goalDao().deleteGoal(goal)
}

class SleepRepository(private val database: AppDatabase) {
  val sleepSchedule: Flow<SleepScheduleEntity?> = database.sleepDao().getSleepSchedule()
  val recentSleepLogs: Flow<List<SleepLogEntity>> = database.sleepDao().getRecentSleepLogs()

  suspend fun updateSleepSchedule(schedule: SleepScheduleEntity) {
    database.sleepDao().insertOrUpdateSleepSchedule(schedule)
  }

  suspend fun logSleepSession(bedtimeMillis: Long, wakeMillis: Long, quality: Int): Long {
    val durationMins = ((wakeMillis - bedtimeMillis) / (60 * 1000L)).toInt().coerceAtLeast(0)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = sdf.format(Date(wakeMillis))
    val log = SleepLogEntity(
      date = date,
      bedtimeMillis = bedtimeMillis,
      wakeMillis = wakeMillis,
      durationMinutes = durationMins,
      qualityScore = quality
    )
    return database.sleepDao().insertSleepLog(log)
  }

  companion object {
    fun calculateTargetDurationHours(bedHour: Int, bedMin: Int, wakeHour: Int, wakeMin: Int): Double {
      var bedTotalMinutes = bedHour * 60 + bedMin
      var wakeTotalMinutes = wakeHour * 60 + wakeMin
      if (wakeTotalMinutes <= bedTotalMinutes) {
        wakeTotalMinutes += 24 * 60
      }
      return (wakeTotalMinutes - bedTotalMinutes) / 60.0
    }
  }
}

class StreakRepository(private val database: AppDatabase) {
  val streakFlow: Flow<StreakEntity?> = database.streakDao().getStreak()
  val achievementsFlow: Flow<List<AchievementEntity>> = database.streakDao().getAllAchievements()

  suspend fun recordWakeUp(challengeSolved: String? = null): StreakEntity {
    val current = database.streakDao().getStreakOnce() ?: StreakEntity(id = 1)
    val today = RoutineRepository.getTodayDateString()

    if (current.lastWakeDate == today) {
      return current // Already recorded today
    }

    val yesterday = Calendar.getInstance().apply {
      add(Calendar.DAY_OF_YEAR, -1)
    }
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val yesterdayStr = sdf.format(yesterday.time)

    val newCurrentStreak = if (current.lastWakeDate == yesterdayStr) {
      current.currentStreak + 1
    } else {
      1 // Reset to 1 if broken
    }
    val newBestStreak = maxOf(newCurrentStreak, current.bestStreak)

    val updatedStreak = current.copy(
      currentStreak = newCurrentStreak,
      bestStreak = newBestStreak,
      lastWakeDate = today
    )
    database.streakDao().insertOrUpdateStreak(updatedStreak)

    // Check & unlock achievements
    checkAchievements(newCurrentStreak, challengeSolved)

    return updatedStreak
  }

  private suspend fun checkAchievements(streak: Int, challengeSolved: String?) {
    if (streak >= 1) unlockAchievement("first_wake")
    if (streak >= 3) unlockAchievement("streak_3")
    if (streak >= 7) unlockAchievement("streak_7")
    if (streak >= 14) unlockAchievement("streak_14")
    if (streak >= 30) unlockAchievement("streak_30")
    if (streak >= 50) unlockAchievement("streak_50")
    if (streak >= 100) unlockAchievement("streak_100")
    if (challengeSolved == "MATH") unlockAchievement("math_whiz")
    if (challengeSolved == "MEMORY") unlockAchievement("memory_ace")
  }

  private suspend fun unlockAchievement(id: String) {
    database.streakDao().updateAchievement(
      AchievementEntity(
        id = id,
        title = getAchievementTitle(id),
        description = getAchievementDesc(id),
        icon = getAchievementIcon(id),
        isUnlocked = true,
        unlockedDate = System.currentTimeMillis()
      )
    )
  }

  private fun getAchievementTitle(id: String) = when (id) {
    "first_wake" -> "First Dawn"
    "streak_3" -> "Consistency Spark"
    "streak_7" -> "Morning Warrior"
    "streak_14" -> "Habit Builder"
    "streak_30" -> "Iron Will"
    "streak_50" -> "Sunrise Master"
    "streak_100" -> "Centurion of Time"
    "math_whiz" -> "Sharp Mind"
    "memory_ace" -> "Photographic"
    else -> "Accomplishment"
  }

  private fun getAchievementDesc(id: String) = when (id) {
    "first_wake" -> "Completed your very first wake-up session"
    "streak_3" -> "Maintained a 3-day wake streak"
    "streak_7" -> "Maintained a 7-day wake streak"
    "streak_14" -> "14 consecutive days on schedule"
    "streak_30" -> "30-day streak milestone achieved"
    "streak_50" -> "50-day streak milestone reached"
    "streak_100" -> "100 legendary days of morning discipline"
    "math_whiz" -> "Solved a Math Challenge to wake up"
    "memory_ace" -> "Beat a Memory Challenge in under a minute"
    else -> "Achievement unlocked"
  }

  private fun getAchievementIcon(id: String) = when (id) {
    "first_wake" -> "EmojiEvents"
    "streak_3" -> "LocalFireDepartment"
    "streak_7" -> "Star"
    "streak_14" -> "WorkspacePremium"
    "streak_30" -> "MilitaryTech"
    "streak_50" -> "Diamond"
    "streak_100" -> "AutoAwesome"
    "math_whiz" -> "Calculate"
    "memory_ace" -> "Psychology"
    else -> "MilitaryTech"
  }
}

class ClockRepository(private val database: AppDatabase) {
  val allWorldClocks: Flow<List<WorldClockEntity>> = database.worldClockDao().getAllClocks()
  val allStudySessions: Flow<List<StudySessionEntity>> = database.studySessionDao().getAllStudySessions()

  suspend fun addWorldClock(cityName: String, countryName: String, timeZoneId: String): Long {
    return database.worldClockDao().insertClock(
      WorldClockEntity(cityName = cityName, countryName = countryName, timeZoneId = timeZoneId)
    )
  }

  suspend fun deleteWorldClock(clock: WorldClockEntity) {
    database.worldClockDao().deleteClock(clock)
  }

  suspend fun recordStudySession(durationMinutes: Int, tag: String = "Study") {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = sdf.format(Date())
    database.studySessionDao().insertStudySession(
      StudySessionEntity(sessionDate = date, durationMinutes = durationMinutes, tag = tag)
    )
  }
}
