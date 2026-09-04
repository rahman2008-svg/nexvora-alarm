package com.example.nexvora.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timeHour: Int,
  val timeMinute: Int,
  val label: String = "Alarm",
  val isEnabled: Boolean = true,
  val repeatDays: String = "", // Comma separated: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun; empty = one-time
  val isVibrate: Boolean = true,
  val ringtoneUri: String = "",
  val ringtoneTitle: String = "Digital Sunrise",
  val volume: Float = 0.85f,
  val isGentleWake: Boolean = true,
  val gentleWakeMinutes: Int = 5,
  val isVoiceEnabled: Boolean = false,
  val voiceGreeting: String = "Good morning! Time to start your day.",
  val goalName: String = "",
  val routineName: String = "",
  val challengeType: String = "NONE", // NONE, MATH, MEMORY, PATTERN, QR
  val challengeDifficulty: String = "MEDIUM", // EASY, MEDIUM, HARD
  val snoozeDurationMinutes: Int = 10,
  val maxSnoozeCount: Int = 3,
  val snoozeCount: Int = 0,
  val qrTargetCode: String = ""
)

@Entity(tableName = "routines")
data class RoutineEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val timeHour: Int,
  val timeMinute: Int,
  val category: String = "CUSTOM", // WAKE_UP, EXERCISE, STUDY, COLLEGE, WORK, BREAK, SLEEP, CUSTOM
  val repeatDays: String = "1,2,3,4,5,6,7",
  val reminderEnabled: Boolean = true,
  val iconName: String = "TaskAlt",
  val isCompletedToday: Boolean = false,
  val lastCompletedDate: String = "" // YYYY-MM-DD
)

@Entity(tableName = "goals")
data class GoalEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val description: String = "",
  val targetDaysPerWeek: Int = 7,
  val isCompletedToday: Boolean = false,
  val streakCount: Int = 0,
  val createdDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "sleep_schedule")
data class SleepScheduleEntity(
  @PrimaryKey val id: Long = 1,
  val targetBedtimeHour: Int = 23,
  val targetBedtimeMinute: Int = 0,
  val targetWakeHour: Int = 7,
  val targetWakeMinute: Int = 0,
  val reminderEnabled: Boolean = true,
  val reminderMinutesBefore: Int = 30
)

@Entity(tableName = "sleep_logs")
data class SleepLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val date: String, // YYYY-MM-DD
  val bedtimeMillis: Long,
  val wakeMillis: Long,
  val durationMinutes: Int,
  val qualityScore: Int = 4 // 1 to 5
)

@Entity(tableName = "streaks")
data class StreakEntity(
  @PrimaryKey val id: Long = 1,
  val currentStreak: Int = 0,
  val bestStreak: Int = 0,
  val lastWakeDate: String = "" // YYYY-MM-DD
)

@Entity(tableName = "achievements")
data class AchievementEntity(
  @PrimaryKey val id: String,
  val title: String,
  val description: String,
  val icon: String,
  val isUnlocked: Boolean = false,
  val unlockedDate: Long? = null
)

@Entity(tableName = "world_clocks")
data class WorldClockEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val cityName: String,
  val countryName: String,
  val timeZoneId: String
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionDate: String, // YYYY-MM-DD
  val durationMinutes: Int,
  val tag: String = "Study"
)
