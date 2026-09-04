package com.example.nexvora.ui.alarms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexvora.alarm.engine.AlarmScheduler
import com.example.nexvora.data.model.AlarmEntity
import com.example.nexvora.ui.viewmodel.AlarmsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlarmsScreen(
  viewModel: AlarmsViewModel,
  use24Hour: Boolean = false,
  modifier: Modifier = Modifier
) {
  val alarms by viewModel.allAlarms.collectAsState()
  var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
  var showCreateDialog by remember { mutableStateOf(false) }
  var alarmToDelete by remember { mutableStateOf<AlarmEntity?>(null) }

  val timeFormatter = remember(use24Hour) {
    SimpleDateFormat(if (use24Hour) "HH:mm" else "hh:mm a", Locale.getDefault())
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showCreateDialog = true },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
          .padding(bottom = 80.dp)
          .testTag("add_alarm_fab")
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Add Alarm", modifier = Modifier.size(28.dp))
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (alarms.isEmpty()) {
        // Empty State
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(90.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Alarm,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(48.dp)
            )
          }
          Spacer(modifier = Modifier.height(20.dp))
          Text(
            text = "No Alarms Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Wake up with confidence, solve smart challenges, and never oversleep again.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(modifier = Modifier.height(24.dp))
          Button(
            onClick = { showCreateDialog = true },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("empty_state_add_alarm_button")
          ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create First Alarm")
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
          contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
          item {
            Text(
              text = "Alarms",
              style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              text = "${alarms.count { it.isEnabled }} active",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          items(alarms, key = { it.id }) { alarm ->
            AlarmItemCard(
              alarm = alarm,
              timeFormatter = timeFormatter,
              onToggle = { isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
              onClick = { editingAlarm = alarm },
              onDelete = { alarmToDelete = alarm }
            )
          }
        }
      }

      // Create Dialog
      if (showCreateDialog) {
        AlarmEditDialog(
          initialAlarm = null,
          onDismiss = { showCreateDialog = false },
          onSave = { newAlarm ->
            viewModel.saveAlarm(newAlarm) {
              showCreateDialog = false
            }
          }
        )
      }

      // Edit Dialog
      editingAlarm?.let { alarm ->
        AlarmEditDialog(
          initialAlarm = alarm,
          onDismiss = { editingAlarm = null },
          onSave = { updatedAlarm ->
            viewModel.saveAlarm(updatedAlarm) {
              editingAlarm = null
            }
          }
        )
      }

      // Delete confirmation dialog
      alarmToDelete?.let { alarm ->
        AlertDialog(
          onDismissRequest = { alarmToDelete = null },
          title = { Text("Delete Alarm") },
          text = { Text("Are you sure you want to delete '${alarm.label}'?") },
          confirmButton = {
            TextButton(
              onClick = {
                viewModel.deleteAlarm(alarm)
                alarmToDelete = null
              }
            ) {
              Text("Delete", color = MaterialTheme.colorScheme.error)
            }
          },
          dismissButton = {
            TextButton(onClick = { alarmToDelete = null }) {
              Text("Cancel")
            }
          }
        )
      }
    }
  }
}

@Composable
fun AlarmItemCard(
  alarm: AlarmEntity,
  timeFormatter: SimpleDateFormat,
  onToggle: (Boolean) -> Unit,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  val alarmCal = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, alarm.timeHour)
    set(Calendar.MINUTE, alarm.timeMinute)
  }
  val formattedTime = timeFormatter.format(alarmCal.time)

  val repeatText = remember(alarm.repeatDays) {
    if (alarm.repeatDays.isBlank()) {
      "Once"
    } else {
      val days = alarm.repeatDays.split(",").mapNotNull { it.toIntOrNull() }.toSet()
      when {
        days.size == 7 -> "Every day"
        days == setOf(1, 2, 3, 4, 5) -> "Weekdays"
        days == setOf(6, 7) -> "Weekends"
        else -> {
          val dayNames = mapOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")
          days.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
        }
      }
    }
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("alarm_card_${alarm.id}")
      .clickable { onClick() },
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (alarm.isEnabled) MaterialTheme.colorScheme.surface
                       else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
  ) {
    Column(
      modifier = Modifier.padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = formattedTime,
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.5).sp
            ),
            color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = alarm.label,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
            Text(
              text = " • $repeatText",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Switch(
          checked = alarm.isEnabled,
          onCheckedChange = onToggle,
          modifier = Modifier.testTag("alarm_switch_${alarm.id}")
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Badges row: Challenge, Voice, Gentle wake, and Delete button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (alarm.challengeType != "NONE") {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
              Text(
                text = "${alarm.challengeType}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          if (alarm.isGentleWake) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            ) {
              Text(
                text = "Gentle",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          if (alarm.isVoiceEnabled) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
            ) {
              Text(
                text = "Voice",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Delete alarm",
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
