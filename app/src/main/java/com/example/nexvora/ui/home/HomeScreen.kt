package com.example.nexvora.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexvora.data.model.AlarmEntity
import com.example.nexvora.data.model.RoutineEntity
import com.example.nexvora.data.model.StreakEntity
import com.example.nexvora.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
  viewModel: MainViewModel,
  onNavigateToAlarms: () -> Unit,
  onNavigateToRoutine: () -> Unit,
  onNavigateToClockTab: (Int) -> Unit, // 0: World Clock, 1: Study, 2: Timer, 3: Stopwatch
  onOpenStreakDetail: () -> Unit,
  onOpenSleepDetail: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentTimeMillis by viewModel.currentTimeMillis.collectAsState()
  val appSettings by viewModel.appSettings.collectAsState()
  val allAlarms by viewModel.allAlarms.collectAsState()
  val allRoutines by viewModel.allRoutines.collectAsState()
  val streak by viewModel.currentStreak.collectAsState()
  val sleepSchedule by viewModel.sleepSchedule.collectAsState()

  val (nextAlarm, countdownStr) = remember(allAlarms, currentTimeMillis) {
    viewModel.calculateNextAlarmInfo(allAlarms, currentTimeMillis)
  }

  val timeFormatter = remember(appSettings.use24HourFormat) {
    SimpleDateFormat(if (appSettings.use24HourFormat) "HH:mm" else "hh:mm a", Locale.getDefault())
  }
  val dateFormatter = remember {
    SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
  }

  val dhakaTimeZone = remember { TimeZone.getTimeZone("Asia/Dhaka") }
  val dhakaTimeFormatter = remember(appSettings.use24HourFormat) {
    SimpleDateFormat(if (appSettings.use24HourFormat) "HH:mm" else "hh:mm a", Locale.getDefault()).apply {
      timeZone = dhakaTimeZone
    }
  }
  val dhakaDateFormatter = remember {
    SimpleDateFormat("EEE, d MMM", Locale.getDefault()).apply {
      timeZone = dhakaTimeZone
    }
  }

  val calendar = remember(currentTimeMillis) {
    Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
  }
  val hour = calendar.get(Calendar.HOUR_OF_DAY)
  val greeting = when (hour) {
    in 4..11 -> "Good Morning"
    in 12..16 -> "Good Afternoon"
    in 17..21 -> "Good Evening"
    else -> "Night Owls"
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
  ) {
    // Top Hero Clock Section (Sleek Interface)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = dateFormatter.format(Date(currentTimeMillis)).uppercase(),
          style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 2.sp,
            fontWeight = FontWeight.SemiBold
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = timeFormatter.format(Date(currentTimeMillis)),
          style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 62.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1.5).sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (nextAlarm != null) "Next alarm $countdownStr" else "No active alarm scheduled",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium
          ),
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(100.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.testTag("dhaka_time_chip")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Dhaka, Bangladesh: ${dhakaTimeFormatter.format(Date(currentTimeMillis))} (${dhakaDateFormatter.format(Date(currentTimeMillis))})",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    // Active Alarm Card (Sleek Interface)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("next_alarm_card")
          .clickable { onNavigateToAlarms() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Text("⏰", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
              Text(
                text = "ACTIVE ALARM",
                style = MaterialTheme.typography.labelSmall.copy(
                  letterSpacing = 1.2.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (nextAlarm != null) {
                val nextAlarmCal = Calendar.getInstance().apply {
                  set(Calendar.HOUR_OF_DAY, nextAlarm.timeHour)
                  set(Calendar.MINUTE, nextAlarm.timeMinute)
                }
                Text(
                  text = timeFormatter.format(nextAlarmCal.time),
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                  ),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = nextAlarm.label.ifBlank { "Morning Alarm" },
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              } else {
                Text(
                  text = "None Set",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Tap to set an alarm",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          if (nextAlarm != null) {
            Surface(
              shape = RoundedCornerShape(100.dp),
              color = if (nextAlarm.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clickable { viewModel.toggleAlarm(nextAlarm, !nextAlarm.isEnabled) }
                .testTag("next_alarm_switch")
            ) {
              Text(
                text = if (nextAlarm.isEnabled) "ON" else "OFF",
                color = if (nextAlarm.isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
              )
            }
          } else {
            IconButton(
              onClick = onNavigateToAlarms,
              modifier = Modifier.testTag("add_first_alarm_button")
            ) {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Set alarm",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }

    // 2-Column Stats Grid: Wake Streak & Sleep Score (Sleek Interface)
    item {
      val currentStreakDays = streak?.currentStreak ?: 0
      val currentSchedule = sleepSchedule
      val sleepScore = remember(currentSchedule) {
        if (currentSchedule != null && currentSchedule.targetBedtimeHour > 0) 88 else 84
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Wake Streak Card
        Card(
          modifier = Modifier
            .weight(1f)
            .testTag("wake_streak_card")
            .clickable { onOpenStreakDetail() },
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text("🔥", fontSize = 22.sp)
            Text(
              text = "Wake Streak",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "$currentStreakDays Days",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        // Sleep Score Card
        Card(
          modifier = Modifier
            .weight(1f)
            .testTag("sleep_summary_card")
            .clickable { onOpenSleepDetail() },
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text("📊", fontSize = 22.sp)
            Text(
              text = "Sleep Score",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "$sleepScore%",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    // Quick Action Chips Row
    item {
      Text(
        text = "QUICK ACTIONS",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.2.sp
      )
      Spacer(modifier = Modifier.height(10.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          QuickActionChip(
            icon = Icons.Filled.AddAlarm,
            label = "Add Alarm",
            onClick = onNavigateToAlarms,
            tag = "quick_add_alarm"
          )
        }
        item {
          QuickActionChip(
            icon = Icons.Filled.HourglassBottom,
            label = "Start Timer",
            onClick = { onNavigateToClockTab(2) },
            tag = "quick_timer"
          )
        }
        item {
          QuickActionChip(
            icon = Icons.Filled.Timer,
            label = "Stopwatch",
            onClick = { onNavigateToClockTab(3) },
            tag = "quick_stopwatch"
          )
        }
        item {
          QuickActionChip(
            icon = Icons.Filled.School,
            label = "Study Mode",
            onClick = { onNavigateToClockTab(1) },
            tag = "quick_study"
          )
        }
        item {
          QuickActionChip(
            icon = Icons.Filled.Bedtime,
            label = "Sleep Schedule",
            onClick = onOpenSleepDetail,
            tag = "quick_sleep"
          )
        }
      }
    }

    // Today's Routine Checklist (Sleek Interface)
    item {
      val completedCount = remember(allRoutines) { allRoutines.count { it.isCompletedToday } }

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Today's Routine",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "${completedCount} of ${allRoutines.size} done",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(4.dp))
              TextButton(
                onClick = onNavigateToRoutine,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
              ) {
                Text(
                  "Manage",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }

          if (allRoutines.isEmpty()) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "No routines created yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedButton(
                onClick = onNavigateToRoutine,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
              ) {
                Text("Create Daily Routine", style = MaterialTheme.typography.labelMedium)
              }
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              val routinesToShow = allRoutines.take(5)
              routinesToShow.forEach { routine ->
                val timeStr = remember(routine.timeHour, routine.timeMinute, appSettings.use24HourFormat) {
                  val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, routine.timeHour)
                    set(Calendar.MINUTE, routine.timeMinute)
                  }
                  timeFormatter.format(cal.time)
                }

                Surface(
                  onClick = { viewModel.toggleRoutine(routine) },
                  shape = RoundedCornerShape(16.dp),
                  color = if (routine.isCompletedToday) MaterialTheme.colorScheme.surfaceVariant
                         else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                  border = if (!routine.isCompletedToday) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)) else null,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (routine.isCompletedToday) {
                      Box(
                        modifier = Modifier
                          .size(24.dp)
                          .clip(RoundedCornerShape(6.dp))
                          .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = "✓",
                          fontSize = 13.sp,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onPrimary
                        )
                      }
                    } else {
                      Box(
                        modifier = Modifier
                          .size(24.dp)
                          .clip(RoundedCornerShape(6.dp))
                          .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                      )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                      text = routine.title,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (routine.isCompletedToday) FontWeight.Normal else FontWeight.Medium
                      ),
                      color = if (routine.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant
                             else MaterialTheme.colorScheme.onSurface,
                      textDecoration = if (routine.isCompletedToday) TextDecoration.LineThrough else null,
                      modifier = Modifier.weight(1f)
                    )
                    Text(
                      text = timeStr,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Sleep Schedule Summary (Sleek Interface)
    item {
      sleepSchedule?.let { schedule ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("sleep_summary_card")
            .clickable { onOpenSleepDetail() },
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.NightsStay,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = "Sleep Schedule",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                val targetHours = remember(schedule) {
                  val diff = (schedule.targetWakeHour * 60 + schedule.targetWakeMinute) -
                             (schedule.targetBedtimeHour * 60 + schedule.targetBedtimeMinute)
                  val total = if (diff <= 0) diff + 24 * 60 else diff
                  String.format(Locale.getDefault(), "%.1f hrs target", total / 60.0)
                }
                Text(
                  text = "Bed ${schedule.targetBedtimeHour.toString().padStart(2, '0')}:${schedule.targetBedtimeMinute.toString().padStart(2, '0')} • Wake ${schedule.targetWakeHour.toString().padStart(2, '0')}:${schedule.targetWakeMinute.toString().padStart(2, '0')} ($targetHours)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Icon(
              imageVector = Icons.Filled.ChevronRight,
              contentDescription = "Details",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@Composable
fun QuickActionChip(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  onClick: () -> Unit,
  tag: String
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.testTag(tag)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
