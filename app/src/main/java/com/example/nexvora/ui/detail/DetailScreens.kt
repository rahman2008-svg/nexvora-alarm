package com.example.nexvora.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexvora.data.model.AchievementEntity
import com.example.nexvora.data.model.SleepScheduleEntity
import com.example.nexvora.ui.viewmodel.SleepViewModel
import com.example.nexvora.ui.viewmodel.StreakViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakDetailScreen(
  viewModel: StreakViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val streak by viewModel.streak.collectAsState()
  val achievements by viewModel.achievements.collectAsState()

  val currentStreak = streak?.currentStreak ?: 0
  val bestStreak = streak?.bestStreak ?: 0

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Streaks & Achievements", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
    ) {
      // Hero Streak Card
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = CardDefaults.outlinedCardBorder(),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("🔥", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "$currentStreak Days",
              style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "CURRENT WAKE STREAK",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Best Streak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$bestStreak Days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Discipline Level", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val level = when {
                  currentStreak >= 30 -> "Master"
                  currentStreak >= 14 -> "Dedicated"
                  currentStreak >= 7 -> "Consistent"
                  currentStreak >= 3 -> "Building"
                  else -> "Novice"
                }
                Text(level, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
              }
            }
          }
        }
      }

      // Achievements Header
      item {
        Text(
          text = "DISCIPLINE ACHIEVEMENTS (${achievements.count { it.isUnlocked }}/${achievements.size})",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 1.sp
        )
      }

      // Achievement Cards
      items(achievements, key = { it.id }) { item ->
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (item.isUnlocked) MaterialTheme.colorScheme.surface
                             else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
          ),
          border = CardDefaults.outlinedCardBorder(),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                  if (item.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                  else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (item.isUnlocked) "🏆" else "🔒",
                fontSize = 22.sp
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
              )
              Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (item.isUnlocked && item.unlockedDate != null) {
                val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(item.unlockedDate))
                Text(
                  text = "Unlocked on $dateStr",
                  style = MaterialTheme.typography.labelSmall,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepDetailScreen(
  viewModel: SleepViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val schedule by viewModel.sleepSchedule.collectAsState()
  val logs by viewModel.recentLogs.collectAsState()

  var bedHour by remember(schedule) { mutableIntStateOf(schedule?.targetBedtimeHour ?: 23) }
  var bedMin by remember(schedule) { mutableIntStateOf(schedule?.targetBedtimeMinute ?: 0) }
  var wakeHour by remember(schedule) { mutableIntStateOf(schedule?.targetWakeHour ?: 7) }
  var wakeMin by remember(schedule) { mutableIntStateOf(schedule?.targetWakeMinute ?: 0) }

  val targetHours = remember(bedHour, bedMin, wakeHour, wakeMin) {
    val bedTotal = bedHour * 60 + bedMin
    var wakeTotal = wakeHour * 60 + wakeMin
    if (wakeTotal <= bedTotal) wakeTotal += 24 * 60
    (wakeTotal - bedTotal) / 60.0
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Sleep Schedule & Wellness", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
    ) {
      // Sleep Schedule Setup Card
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = CardDefaults.outlinedCardBorder(),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.Bedtime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(10.dp))
              Text("Target Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // Bedtime
              Column {
                Text("Bedtime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  "${bedHour.toString().padStart(2, '0')}:${bedMin.toString().padStart(2, '0')}",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold
                )
                Row {
                  IconButton(onClick = { bedHour = if (bedHour == 0) 23 else bedHour - 1 }) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                  }
                  IconButton(onClick = { bedHour = (bedHour + 1) % 24 }) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null)
                  }
                }
              }

              // Target hours indicator
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = String.format(Locale.getDefault(), "%.1f hrs", targetHours),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Goal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              // Wake time
              Column(horizontalAlignment = Alignment.End) {
                Text("Wake Up", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  "${wakeHour.toString().padStart(2, '0')}:${wakeMin.toString().padStart(2, '0')}",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold
                )
                Row {
                  IconButton(onClick = { wakeHour = if (wakeHour == 0) 23 else wakeHour - 1 }) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                  }
                  IconButton(onClick = { wakeHour = (wakeHour + 1) % 24 }) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null)
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                viewModel.updateSchedule(
                  SleepScheduleEntity(
                    id = 1,
                    targetBedtimeHour = bedHour,
                    targetBedtimeMinute = bedMin,
                    targetWakeHour = wakeHour,
                    targetWakeMinute = wakeMin
                  )
                )
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().testTag("save_sleep_schedule_button")
            ) {
              Text("Save Sleep Schedule")
            }
          }
        }
      }

      // Sleep Tips Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder(),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text("Sleep Hygiene Rules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("• Consistent wake-up times anchor your circadian rhythm faster than bedtimes.", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("• Avoid caffeine at least 6 hours before your target bedtime.", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("• Immediate light exposure upon waking triggers cortisol and shuts off melatonin.", style = MaterialTheme.typography.bodySmall)
          }
        }
      }

      // Recent Sleep Logs
      item {
        Text(
          text = "RECENT SLEEP LOGS (${logs.size})",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 1.sp
        )
      }

      if (logs.isEmpty()) {
        item {
          Text(
            text = "Your sleep logs will appear here as you dismiss morning alarms.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        items(logs, key = { it.id }) { log ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(log.date, fontWeight = FontWeight.Bold)
                Text(
                  "${log.durationMinutes / 60}h ${log.durationMinutes % 60}m recorded",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
              ) {
                Text(
                  text = "Score: ${log.qualityScore}/100",
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall
                )
              }
            }
          }
        }
      }
    }
  }
}
