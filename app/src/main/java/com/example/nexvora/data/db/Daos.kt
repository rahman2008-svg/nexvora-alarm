package com.example.nexvora.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nexvora.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
  @Query("SELECT * FROM alarms ORDER BY timeHour ASC, timeMinute ASC")
  fun getAllAlarms(): Flow<List<AlarmEntity>>

  @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY timeHour ASC, timeMinute ASC")
  fun getEnabledAlarms(): Flow<List<AlarmEntity>>

  @Query("SELECT * FROM alarms WHERE id = :id")
  suspend fun getAlarmById(id: Long): AlarmEntity?

  @Query("SELECT * FROM alarms WHERE isEnabled = 1")
  suspend fun getEnabledAlarmsList(): List<AlarmEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlarm(alarm: AlarmEntity): Long

  @Update
  suspend fun updateAlarm(alarm: AlarmEntity)

  @Delete
  suspend fun deleteAlarm(alarm: AlarmEntity)

  @Query("DELETE FROM alarms WHERE id = :id")
  suspend fun deleteAlarmById(id: Long)
}

@Dao
interface RoutineDao {
  @Query("SELECT * FROM routines ORDER BY timeHour ASC, timeMinute ASC")
  fun getAllRoutines(): Flow<List<RoutineEntity>>

  @Query("SELECT * FROM routines WHERE id = :id")
  suspend fun getRoutineById(id: Long): RoutineEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRoutine(routine: RoutineEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRoutines(routines: List<RoutineEntity>)

  @Update
  suspend fun updateRoutine(routine: RoutineEntity)

  @Delete
  suspend fun deleteRoutine(routine: RoutineEntity)

  @Query("UPDATE routines SET isCompletedToday = 0 WHERE lastCompletedDate != :todayDate")
  suspend fun resetCompletedForNewDay(todayDate: String)
}

@Dao
interface GoalDao {
  @Query("SELECT * FROM goals ORDER BY id DESC")
  fun getAllGoals(): Flow<List<GoalEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGoal(goal: GoalEntity): Long

  @Update
  suspend fun updateGoal(goal: GoalEntity)

  @Delete
  suspend fun deleteGoal(goal: GoalEntity)
}

@Dao
interface SleepDao {
  @Query("SELECT * FROM sleep_schedule WHERE id = 1")
  fun getSleepSchedule(): Flow<SleepScheduleEntity?>

  @Query("SELECT * FROM sleep_schedule WHERE id = 1")
  suspend fun getSleepScheduleOnce(): SleepScheduleEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateSleepSchedule(schedule: SleepScheduleEntity)

  @Query("SELECT * FROM sleep_logs ORDER BY wakeMillis DESC LIMIT 30")
  fun getRecentSleepLogs(): Flow<List<SleepLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSleepLog(log: SleepLogEntity): Long
}

@Dao
interface StreakDao {
  @Query("SELECT * FROM streaks WHERE id = 1")
  fun getStreak(): Flow<StreakEntity?>

  @Query("SELECT * FROM streaks WHERE id = 1")
  suspend fun getStreakOnce(): StreakEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateStreak(streak: StreakEntity)

  @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, id ASC")
  fun getAllAchievements(): Flow<List<AchievementEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAchievements(achievements: List<AchievementEntity>)

  @Update
  suspend fun updateAchievement(achievement: AchievementEntity)
}

@Dao
interface WorldClockDao {
  @Query("SELECT * FROM world_clocks ORDER BY cityName ASC")
  fun getAllClocks(): Flow<List<WorldClockEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertClock(clock: WorldClockEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertClocks(clocks: List<WorldClockEntity>)

  @Delete
  suspend fun deleteClock(clock: WorldClockEntity)
}

@Dao
interface StudySessionDao {
  @Query("SELECT * FROM study_sessions ORDER BY id DESC")
  fun getAllStudySessions(): Flow<List<StudySessionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStudySession(session: StudySessionEntity): Long
}
