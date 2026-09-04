package com.example.nexvora.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexvora.ui.viewmodel.SettingsViewModel
import com.example.ui.theme.AppThemeMode

@Composable
fun SettingsScreen(
  viewModel: SettingsViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.appSettings.collectAsState()
  var showPrivacyDialog by remember { mutableStateOf(false) }

  val canExactAlarm = remember { viewModel.canScheduleExactAlarms() }
  val canNotifications = remember { viewModel.areNotificationsEnabled() }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Settings",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )

    // Appearance / Theme Card
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(10.dp))
          Text("Appearance & Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(14.dp))

        val themeModes = listOf(
          AppThemeMode.AMOLED to "AMOLED (Pure Black)",
          AppThemeMode.DARK to "Dark Theme",
          AppThemeMode.LIGHT to "Light Theme",
          AppThemeMode.SYSTEM to "System Default"
        )

        themeModes.forEach { (mode, label) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.updateTheme(mode) }
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = (settings.themeMode == mode),
              onClick = { viewModel.updateTheme(mode) }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("24-Hour Time Format", fontWeight = FontWeight.SemiBold)
            Text("e.g. 14:30 instead of 02:30 PM", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Switch(
            checked = settings.use24HourFormat,
            onCheckedChange = { viewModel.update24Hour(it) },
            modifier = Modifier.testTag("24h_format_switch")
          )
        }
      }
    }

    // Voice / TTS Card
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Voice Morning Briefing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Switch(
            checked = settings.isTtsEnabled,
            onCheckedChange = { viewModel.updateTts(it) },
            modifier = Modifier.testTag("tts_enabled_switch")
          )
        }

        if (settings.isTtsEnabled) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Speech Rate: ${String.format(java.util.Locale.getDefault(), "%.2fx", settings.ttsRate)}",
            style = MaterialTheme.typography.bodyMedium
          )
          Slider(
            value = settings.ttsRate,
            onValueChange = { viewModel.updateTtsRate(it) },
            valueRange = 0.7f..1.5f,
            steps = 7
          )
          Button(
            onClick = { viewModel.testTts(settings.ttsRate) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("test_voice_button")
          ) {
            Icon(Icons.Filled.VolumeUp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Voice Briefing")
          }
        }
      }
    }

    // Alarm Defaults
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(10.dp))
          Text("Alarm Defaults", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text("Default Snooze Duration", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(5, 10, 15).forEach { mins ->
            FilterChip(
              selected = (settings.defaultSnoozeMinutes == mins),
              onClick = { viewModel.updateSnooze(mins, settings.defaultMaxSnoozes) },
              label = { Text("$mins min") }
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Default Max Snoozes", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(1, 2, 3, 5).forEach { count ->
            FilterChip(
              selected = (settings.defaultMaxSnoozes == count),
              onClick = { viewModel.updateSnooze(settings.defaultSnoozeMinutes, count) },
              label = { Text("$count times") }
            )
          }
        }
      }
    }

    // Smart Notifications & Ringing System Card
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth().testTag("smart_notifications_card")
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(10.dp))
          Text("Smart Notifications & Ringing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Upcoming Alarm Notice (30m before)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Upcoming Alarm Notice", fontWeight = FontWeight.SemiBold)
            Text(
              "Notifies 30 mins before alarm with quick 'Dismiss Early' action",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = settings.upcomingAlarmNotifications,
            onCheckedChange = { viewModel.updateUpcomingNotifications(it) }
          )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        // Morning Briefing
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Post-Wake Morning Briefing", fontWeight = FontWeight.SemiBold)
            Text(
              "Shows streak count and motivation upon waking up",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = settings.morningSummaryNotifications,
            onCheckedChange = { viewModel.updateMorningNotifications(it) }
          )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        // Bedtime Notice
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Bedtime Wind-Down Reminder", fontWeight = FontWeight.SemiBold)
            Text(
              "Alerts before target bedtime to preserve sleep routine",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = settings.bedtimeReminderNotifications,
            onCheckedChange = { viewModel.updateBedtimeNotifications(it) }
          )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        // Ringing Vibration Pattern
        Text("Ringing Vibration Pattern", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          listOf("STANDARD" to "Standard", "GENTLE" to "Gentle", "ENERGETIC" to "Rapid", "HEARTBEAT" to "Heartbeat").forEach { (pattern, name) ->
            FilterChip(
              selected = (settings.vibrationPattern == pattern),
              onClick = { viewModel.updateVibrationPattern(pattern) },
              label = { Text(name) }
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Test Smart Notification button
        Button(
          onClick = { viewModel.testSmartNotification() },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("test_smart_notification_btn")
        ) {
          Icon(Icons.Filled.Notifications, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Test Smart Notification")
        }
      }
    }

    // System Permissions & Reliability Card
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(10.dp))
          Text("Alarm Reliability & Permissions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Exact Alarm
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Exact Alarm Schedule", fontWeight = FontWeight.SemiBold)
            Text(
              text = if (canExactAlarm) "Granted (Ready for wake-up)" else "Permission recommended for exact timing",
              style = MaterialTheme.typography.bodySmall,
              color = if (canExactAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
          }
          if (!canExactAlarm) {
            OutlinedButton(onClick = { viewModel.openExactAlarmSettings() }) {
              Text("Fix")
            }
          } else {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        // Notifications
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Notifications", fontWeight = FontWeight.SemiBold)
            Text(
              text = if (canNotifications) "Enabled" else "Disabled (May not ring in background)",
              style = MaterialTheme.typography.bodySmall,
              color = if (canNotifications) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
          }
          if (!canNotifications) {
            OutlinedButton(onClick = { viewModel.openAppSettings() }) {
              Text("Enable")
            }
          } else {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }

    // Privacy Card
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { showPrivacyDialog = true }
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Privacy & Offline Guarantee", fontWeight = FontWeight.Bold)
            Text("100% Offline • Zero Trackers • Zero Telemetry", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null)
      }
    }

    // About Developer & Company Card
    AboutDeveloperCard()

    Spacer(modifier = Modifier.height(100.dp))
  }

  if (showPrivacyDialog) {
    AlertDialog(
      onDismissRequest = { showPrivacyDialog = false },
      title = { Text("NexVora Privacy Promise", fontWeight = FontWeight.Bold) },
      text = {
        Text(
          "NexVora Alarm is designed with privacy-first principles.\n\n" +
          "• All alarm schedules, routines, goals, and sleep logs are stored 100% locally on your device in a secure SQLite database.\n" +
          "• No personal information is ever collected, uploaded, or transmitted.\n" +
          "• Voice announcements use Android's on-device Text-to-Speech engine without internet connection.\n" +
          "• Completely offline, reliable, and respectful of your data."
        )
      },
      confirmButton = {
        Button(onClick = { showPrivacyDialog = false }) { Text("Understood") }
      }
    )
  }
}
