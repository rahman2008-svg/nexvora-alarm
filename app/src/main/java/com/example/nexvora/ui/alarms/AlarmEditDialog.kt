package com.example.nexvora.ui.alarms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nexvora.data.model.AlarmEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditDialog(
  initialAlarm: AlarmEntity? = null,
  onDismiss: () -> Unit,
  onSave: (AlarmEntity) -> Unit
) {
  var hour by remember { mutableIntStateOf(initialAlarm?.timeHour ?: 7) }
  var minute by remember { mutableIntStateOf(initialAlarm?.timeMinute ?: 0) }
  var label by remember { mutableStateOf(initialAlarm?.label ?: "Wake Up") }
  var isVibrate by remember { mutableStateOf(initialAlarm?.isVibrate ?: true) }
  var ringtoneTitle by remember { mutableStateOf(initialAlarm?.ringtoneTitle ?: "Digital Sunrise") }
  var volume by remember { mutableFloatStateOf(initialAlarm?.volume ?: 0.85f) }
  var isGentleWake by remember { mutableStateOf(initialAlarm?.isGentleWake ?: true) }
  var gentleMinutes by remember { mutableIntStateOf(initialAlarm?.gentleWakeMinutes ?: 5) }
  var isVoice by remember { mutableStateOf(initialAlarm?.isVoiceEnabled ?: false) }
  var voiceGreeting by remember { mutableStateOf(initialAlarm?.voiceGreeting ?: "Good morning! Time to begin.") }
  var goalName by remember { mutableStateOf(initialAlarm?.goalName ?: "") }
  var routineName by remember { mutableStateOf(initialAlarm?.routineName ?: "") }
  var challengeType by remember { mutableStateOf(initialAlarm?.challengeType ?: "NONE") }
  var challengeDifficulty by remember { mutableStateOf(initialAlarm?.challengeDifficulty ?: "MEDIUM") }
  var snoozeMinutes by remember { mutableIntStateOf(initialAlarm?.snoozeDurationMinutes ?: 10) }
  var maxSnoozes by remember { mutableIntStateOf(initialAlarm?.maxSnoozeCount ?: 3) }
  var qrCode by remember { mutableStateOf(initialAlarm?.qrTargetCode ?: "") }

  // Repeat days: 1..7 (1=Mon..7=Sun)
  val initialDays = remember(initialAlarm) {
    if (initialAlarm == null || initialAlarm.repeatDays.isBlank()) {
      mutableStateSetOf<Int>()
    } else {
      initialAlarm.repeatDays.split(",").mapNotNull { it.toIntOrNull() }.toMutableStateSet()
    }
  }
  val selectedDays = remember { initialDays }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 24.dp),
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        // Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", style = MaterialTheme.typography.titleMedium)
          }
          Text(
            text = if (initialAlarm == null) "New Alarm" else "Edit Alarm",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Button(
            onClick = {
              val alarm = AlarmEntity(
                id = initialAlarm?.id ?: 0,
                timeHour = hour,
                timeMinute = minute,
                label = label.ifBlank { "Alarm" },
                isEnabled = true,
                repeatDays = selectedDays.sorted().joinToString(","),
                isVibrate = isVibrate,
                ringtoneUri = "",
                ringtoneTitle = ringtoneTitle,
                volume = volume,
                isGentleWake = isGentleWake,
                gentleWakeMinutes = gentleMinutes,
                isVoiceEnabled = isVoice,
                voiceGreeting = voiceGreeting,
                goalName = goalName,
                routineName = routineName,
                challengeType = challengeType,
                challengeDifficulty = challengeDifficulty,
                snoozeDurationMinutes = snoozeMinutes,
                maxSnoozeCount = maxSnoozes,
                qrTargetCode = qrCode
              )
              onSave(alarm)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("save_alarm_button")
          ) {
            Text("Save", fontWeight = FontWeight.Bold)
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
          // Time Pickers (Interactive Row with hours and minutes)
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "ALARM TIME",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
              )
              Spacer(modifier = Modifier.height(12.dp))

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
              ) {
                // Hours control
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  IconButton(onClick = { hour = (hour + 1) % 24 }) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Hour up")
                  }
                  Text(
                    text = hour.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  IconButton(onClick = { hour = if (hour == 0) 23 else hour - 1 }) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Hour down")
                  }
                }

                Text(
                  text = ":",
                  style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                  modifier = Modifier.padding(horizontal = 12.dp),
                  color = MaterialTheme.colorScheme.primary
                )

                // Minutes control
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  IconButton(onClick = { minute = (minute + 5) % 60 }) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Minute up")
                  }
                  Text(
                    text = minute.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  IconButton(onClick = { minute = if (minute < 5) 55 else minute - 5 }) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Minute down")
                  }
                }
              }
            }
          }

          // Label Input
          OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Alarm Label") },
            placeholder = { Text("e.g. Wake Up, Deep Study, Gym") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("alarm_label_input"),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(Icons.Filled.Label, contentDescription = null) }
          )

          // Repeat Days Selector
          Column {
            Text(
              text = "REPEAT SCHEDULE",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              val days = listOf("M" to 1, "T" to 2, "W" to 3, "T" to 4, "F" to 5, "S" to 6, "S" to 7)
              days.forEach { (label, dayNum) ->
                val isSelected = selectedDays.contains(dayNum)
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                      if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable {
                      if (isSelected) selectedDays.remove(dayNum) else selectedDays.add(dayNum)
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              AssistChip(
                onClick = {
                  selectedDays.clear()
                  selectedDays.addAll(listOf(1, 2, 3, 4, 5))
                },
                label = { Text("Weekdays") }
              )
              AssistChip(
                onClick = {
                  selectedDays.clear()
                  selectedDays.addAll(listOf(6, 7))
                },
                label = { Text("Weekends") }
              )
              AssistChip(
                onClick = {
                  selectedDays.clear()
                  selectedDays.addAll(listOf(1, 2, 3, 4, 5, 6, 7))
                },
                label = { Text("Daily") }
              )
              AssistChip(
                onClick = { selectedDays.clear() },
                label = { Text("Once") }
              )
            }
          }

          // Challenge Selection
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Wake-up Challenge",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.height(12.dp))

              val challenges = listOf(
                "NONE" to "None (Standard)",
                "MATH" to "Math Arithmetic",
                "MEMORY" to "Memory Sequence",
                "PATTERN" to "Number Pattern",
                "QR" to "QR Code Mission"
              )

              challenges.forEach { (type, desc) ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { challengeType = type }
                    .padding(vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  RadioButton(
                    selected = (challengeType == type),
                    onClick = { challengeType = type }
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(text = desc, style = MaterialTheme.typography.bodyMedium)
                }
              }

              if (challengeType == "MATH") {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Difficulty:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                    FilterChip(
                      selected = (challengeDifficulty == diff),
                      onClick = { challengeDifficulty = diff },
                      label = { Text(diff) }
                    )
                  }
                }
              }

              if (challengeType == "QR") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                  value = qrCode,
                  onValueChange = { qrCode = it },
                  label = { Text("Target Code to Dismiss") },
                  placeholder = { Text("e.g. MORNING2026") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }

          // Gentle Wake-up & Sound
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Gentle Wake-up", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Switch(checked = isGentleWake, onCheckedChange = { isGentleWake = it })
              }
              if (isGentleWake) {
                Text(
                  text = "Volume gradually increases over $gentleMinutes minutes",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                  value = gentleMinutes.toFloat(),
                  onValueChange = { gentleMinutes = it.toInt() },
                  valueRange = 1f..15f,
                  steps = 13
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Vibration", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = isVibrate, onCheckedChange = { isVibrate = it })
              }
            }
          }

          // Snooze Configuration
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Snooze, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Snooze Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.height(10.dp))
              Text("Duration: $snoozeMinutes minutes", style = MaterialTheme.typography.bodyMedium)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15).forEach { mins ->
                  FilterChip(
                    selected = (snoozeMinutes == mins),
                    onClick = { snoozeMinutes = mins },
                    label = { Text("$mins min") }
                  )
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text("Maximum Snoozes: $maxSnoozes", style = MaterialTheme.typography.bodyMedium)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3, 5).forEach { count ->
                  FilterChip(
                    selected = (maxSnoozes == count),
                    onClick = { maxSnoozes = count },
                    label = { Text("$count times") }
                  )
                }
              }
            }
          }

          // Voice Announcement & Associated Goal
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Voice Announcement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Switch(checked = isVoice, onCheckedChange = { isVoice = it })
              }

              if (isVoice) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                  value = voiceGreeting,
                  onValueChange = { voiceGreeting = it },
                  label = { Text("Voice Greeting") },
                  modifier = Modifier.fillMaxWidth()
                )
              }

              Spacer(modifier = Modifier.height(12.dp))
              OutlinedTextField(
                value = goalName,
                onValueChange = { goalName = it },
                label = { Text("Associated Goal (Optional)") },
                placeholder = { Text("e.g. Mathematics Exam Prep, Morning 5K") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }

          Spacer(modifier = Modifier.height(30.dp))
        }
      }
    }
  }
}

private fun <T> mutableStateSetOf(): MutableSet<T> = mutableSetOf()
private fun <T> Collection<T>.toMutableStateSet(): MutableSet<T> = this.toMutableSet()
