package com.example.nexvora.ui.routine

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexvora.data.model.GoalEntity
import com.example.nexvora.data.model.RoutineEntity
import com.example.nexvora.ui.viewmodel.RoutineViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RoutineScreen(
  viewModel: RoutineViewModel,
  use24Hour: Boolean = false,
  modifier: Modifier = Modifier
) {
  val routines by viewModel.allRoutines.collectAsState()
  val goals by viewModel.allGoals.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Routine, 1: Goals
  var showCreateRoutineDialog by remember { mutableStateOf(false) }
  var showCreateGoalDialog by remember { mutableStateOf(false) }
  var routineToDelete by remember { mutableStateOf<RoutineEntity?>(null) }
  var goalToDelete by remember { mutableStateOf<GoalEntity?>(null) }

  val timeFormatter = remember(use24Hour) {
    SimpleDateFormat(if (use24Hour) "HH:mm" else "hh:mm a", Locale.getDefault())
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          if (selectedTab == 0) showCreateRoutineDialog = true else showCreateGoalDialog = true
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
          .padding(bottom = 80.dp)
          .testTag("add_routine_or_goal_fab")
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Add Item", modifier = Modifier.size(28.dp))
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 20.dp)
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Daily Routine & Goals",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(14.dp))

      // Tab selector: Daily Routine vs Goals
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Daily Routine (${routines.size})", fontWeight = FontWeight.SemiBold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Goals & Habits (${goals.size})", fontWeight = FontWeight.SemiBold) }
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (selectedTab == 0) {
        // Routines Tab
        if (routines.isEmpty()) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Checklist,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "No Daily Routines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Build positive habits like Wake Up, Study, and Exercise.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
          ) {
            items(routines, key = { it.id }) { routine ->
              RoutineCard(
                routine = routine,
                timeFormatter = timeFormatter,
                onToggle = { viewModel.toggleRoutine(routine) },
                onDelete = { routineToDelete = routine }
              )
            }
          }
        }
      } else {
        // Goals Tab
        if (goals.isEmpty()) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "No Goals Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Attach goals to alarms to achieve higher focus each morning.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
          ) {
            items(goals, key = { it.id }) { goal ->
              GoalCard(
                goal = goal,
                onToggle = { viewModel.toggleGoal(goal) },
                onDelete = { goalToDelete = goal }
              )
            }
          }
        }
      }
    }

    // Create Routine Dialog
    if (showCreateRoutineDialog) {
      CreateRoutineDialog(
        onDismiss = { showCreateRoutineDialog = false },
        onSave = { newRoutine ->
          viewModel.saveRoutine(newRoutine) {
            showCreateRoutineDialog = false
          }
        }
      )
    }

    // Create Goal Dialog
    if (showCreateGoalDialog) {
      CreateGoalDialog(
        onDismiss = { showCreateGoalDialog = false },
        onSave = { newGoal ->
          viewModel.saveGoal(newGoal) {
            showCreateGoalDialog = false
          }
        }
      )
    }

    // Delete Routine Dialog
    routineToDelete?.let { r ->
      AlertDialog(
        onDismissRequest = { routineToDelete = null },
        title = { Text("Delete Routine") },
        text = { Text("Delete '${r.title}'?") },
        confirmButton = {
          TextButton(onClick = {
            viewModel.deleteRoutine(r)
            routineToDelete = null
          }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
          TextButton(onClick = { routineToDelete = null }) { Text("Cancel") }
        }
      )
    }

    // Delete Goal Dialog
    goalToDelete?.let { g ->
      AlertDialog(
        onDismissRequest = { goalToDelete = null },
        title = { Text("Delete Goal") },
        text = { Text("Delete '${g.title}'?") },
        confirmButton = {
          TextButton(onClick = {
            viewModel.deleteGoal(g)
            goalToDelete = null
          }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
          TextButton(onClick = { goalToDelete = null }) { Text("Cancel") }
        }
      )
    }
  }
}

@Composable
fun RoutineCard(
  routine: RoutineEntity,
  timeFormatter: SimpleDateFormat,
  onToggle: () -> Unit,
  onDelete: () -> Unit
) {
  val cal = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, routine.timeHour)
    set(Calendar.MINUTE, routine.timeMinute)
  }
  val timeStr = timeFormatter.format(cal.time)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("routine_card_${routine.id}"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = if (routine.isCompletedToday) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
          contentDescription = "Toggle completion",
          tint = if (routine.isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(28.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = routine.title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = if (routine.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = timeStr,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
          ) {
            Text(
              text = routine.category,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
        Icon(
          imageVector = Icons.Outlined.Delete,
          contentDescription = "Delete routine",
          tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun GoalCard(
  goal: GoalEntity,
  onToggle: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("goal_card_${goal.id}"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
        Icon(
          imageVector = if (goal.isCompletedToday) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
          contentDescription = "Toggle goal completion",
          tint = if (goal.isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(28.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = goal.title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (goal.description.isNotBlank()) {
          Text(
            text = goal.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "🔥 ${goal.streakCount} day streak • Target: ${goal.targetDaysPerWeek} days/week",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.tertiary,
          fontWeight = FontWeight.Bold
        )
      }

      IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
        Icon(
          imageVector = Icons.Outlined.Delete,
          contentDescription = "Delete goal",
          tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun CreateRoutineDialog(
  onDismiss: () -> Unit,
  onSave: (RoutineEntity) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var hour by remember { mutableIntStateOf(8) }
  var minute by remember { mutableIntStateOf(0) }
  var category by remember { mutableStateOf("STUDY") }

  val categories = listOf("WAKE_UP", "EXERCISE", "STUDY", "COLLEGE", "WORK", "BREAK", "SLEEP", "CUSTOM")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("New Routine Item", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          placeholder = { Text("e.g. Physics Revision, Morning Jog") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("routine_title_input")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Time: ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}", fontWeight = FontWeight.SemiBold)
          Row {
            IconButton(onClick = { hour = (hour + 1) % 24 }) {
              Icon(Icons.Filled.Add, contentDescription = "+ hour")
            }
            IconButton(onClick = { minute = (minute + 15) % 60 }) {
              Icon(Icons.Filled.AccessTime, contentDescription = "+ 15 min")
            }
          }
        }

        Text("Category:", style = MaterialTheme.typography.labelMedium)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          categories.take(4).forEach { cat ->
            FilterChip(
              selected = (category == cat),
              onClick = { category = cat },
              label = { Text(cat.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          categories.drop(4).forEach { cat ->
            FilterChip(
              selected = (category == cat),
              onClick = { category = cat },
              label = { Text(cat.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onSave(
              RoutineEntity(
                title = title.trim(),
                timeHour = hour,
                timeMinute = minute,
                category = category
              )
            )
          }
        },
        modifier = Modifier.testTag("save_routine_button")
      ) { Text("Add") }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun CreateGoalDialog(
  onDismiss: () -> Unit,
  onSave: (GoalEntity) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var targetDays by remember { mutableIntStateOf(7) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("New Goal", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Goal Title") },
          placeholder = { Text("e.g. Solve 3 LeetCode Problems") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("goal_title_input")
        )
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description (Optional)") },
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Target Days / Week: $targetDays")
          Row {
            IconButton(onClick = { if (targetDays > 1) targetDays-- }) {
              Icon(Icons.Filled.Remove, contentDescription = "-")
            }
            IconButton(onClick = { if (targetDays < 7) targetDays++ }) {
              Icon(Icons.Filled.Add, contentDescription = "+")
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onSave(GoalEntity(title = title.trim(), description = description.trim(), targetDaysPerWeek = targetDays))
          }
        },
        modifier = Modifier.testTag("save_goal_button")
      ) { Text("Add Goal") }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
