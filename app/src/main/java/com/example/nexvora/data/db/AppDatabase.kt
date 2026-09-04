package com.example.nexvora.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nexvora.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    AlarmEntity::class,
    RoutineEntity::class,
    GoalEntity::class,
    SleepScheduleEntity::class,
    SleepLogEntity::class,
    StreakEntity::class,
    AchievementEntity::class,
    WorldClockEntity::class,
    StudySessionEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun alarmDao(): AlarmDao
  abstract fun routineDao(): RoutineDao
  abstract fun goalDao(): GoalDao
  abstract fun sleepDao(): SleepDao
  abstract fun streakDao(): StreakDao
  abstract fun worldClockDao(): WorldClockDao
  abstract fun studySessionDao(): StudySessionDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "nexvora_alarm_db"
        )
        .addCallback(DatabaseCallback())
        .fallbackToDestructiveMigration(false)
        .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          CoroutineScope(Dispatchers.IO).launch {
            populateInitialData(database)
          }
        }
      }

      private suspend fun populateInitialData(database: AppDatabase) {
        // Initial Streak & Achievements
        database.streakDao().insertOrUpdateStreak(
          StreakEntity(id = 1, currentStreak = 0, bestStreak = 0, lastWakeDate = "")
        )

        val defaultAchievements = listOf(
          AchievementEntity("first_wake", "First Dawn", "Completed your very first wake-up session", "EmojiEvents", false),
          AchievementEntity("streak_3", "Consistency Spark", "Maintained a 3-day wake streak", "LocalFireDepartment", false),
          AchievementEntity("streak_7", "Morning Warrior", "Maintained a 7-day wake streak", "Star", false),
          AchievementEntity("streak_14", "Habit Builder", "14 consecutive days on schedule", "WorkspacePremium", false),
          AchievementEntity("streak_30", "Iron Will", "30-day streak milestone achieved", "MilitaryTech", false),
          AchievementEntity("streak_50", "Sunrise Master", "50-day streak milestone reached", "Diamond", false),
          AchievementEntity("streak_100", "Centurion of Time", "100 legendary days of morning discipline", "AutoAwesome", false),
          AchievementEntity("math_whiz", "Sharp Mind", "Solved a Math Challenge to wake up", "Calculate", false),
          AchievementEntity("memory_ace", "Photographic", "Beat a Memory Challenge in under a minute", "Psychology", false)
        )
        database.streakDao().insertAchievements(defaultAchievements)

        // Initial Sleep Schedule
        database.sleepDao().insertOrUpdateSleepSchedule(
          SleepScheduleEntity(
            id = 1,
            targetBedtimeHour = 23,
            targetBedtimeMinute = 0,
            targetWakeHour = 7,
            targetWakeMinute = 0,
            reminderEnabled = true,
            reminderMinutesBefore = 30
          )
        )

        // Initial World Clocks
        val defaultClocks = listOf(
          WorldClockEntity(cityName = "Dhaka", countryName = "Bangladesh", timeZoneId = "Asia/Dhaka"),
          WorldClockEntity(cityName = "London", countryName = "United Kingdom", timeZoneId = "Europe/London"),
          WorldClockEntity(cityName = "New York", countryName = "United States", timeZoneId = "America/New_York"),
          WorldClockEntity(cityName = "Tokyo", countryName = "Japan", timeZoneId = "Asia/Tokyo")
        )
        database.worldClockDao().insertClocks(defaultClocks)

        // Initial Routines
        val defaultRoutines = listOf(
          RoutineEntity(title = "Morning Hydration & Wake Up", timeHour = 7, timeMinute = 0, category = "WAKE_UP", iconName = "WaterDrop"),
          RoutineEntity(title = "Morning Workout / Stretch", timeHour = 7, timeMinute = 30, category = "EXERCISE", iconName = "FitnessCenter"),
          RoutineEntity(title = "Deep Work / Study Block", timeHour = 9, timeMinute = 0, category = "STUDY", iconName = "MenuBook"),
          RoutineEntity(title = "Afternoon Break & Walk", timeHour = 13, timeMinute = 30, category = "BREAK", iconName = "DirectionsWalk"),
          RoutineEntity(title = "Evening Wind Down & Read", timeHour = 22, timeMinute = 30, category = "SLEEP", iconName = "Bedtime")
        )
        database.routineDao().insertRoutines(defaultRoutines)

        // Initial Goals
        database.goalDao().insertGoal(
          GoalEntity(title = "Wake up before 7:30 AM", description = "Build a consistent morning rhythm", targetDaysPerWeek = 7)
        )
        database.goalDao().insertGoal(
          GoalEntity(title = "Morning Study Block", description = "Complete 45 minutes focused session", targetDaysPerWeek = 5)
        )
      }
    }
  }
}
