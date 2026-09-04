package com.example.nexvora.ui.clock

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
import com.example.nexvora.data.model.WorldClockEntity
import com.example.nexvora.ui.viewmodel.CityOption
import com.example.nexvora.ui.viewmodel.ClockViewModel
import com.example.nexvora.ui.viewmodel.PomodoroState
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun ClockScreen(
  viewModel: ClockViewModel,
  initialTab: Int = 0,
  use24Hour: Boolean = false,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(initialTab) }
  val tabs = listOf("World Clock", "Study Mode", "Timer", "Stopwatch")

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp)
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Clock & Focus",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(14.dp))

    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      edgePadding = 0.dp,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontWeight = FontWeight.SemiBold) }
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    when (selectedTab) {
      0 -> WorldClockTab(viewModel, use24Hour)
      1 -> StudyPomodoroTab(viewModel)
      2 -> TimerTab(viewModel)
      3 -> StopwatchTab(viewModel)
    }
  }
}

@Composable
fun WorldClockTab(viewModel: ClockViewModel, use24Hour: Boolean) {
  val clocks by viewModel.worldClocks.collectAsState()
  val ticker by viewModel.clockTicker.collectAsState()
  var showAddCityDialog by remember { mutableStateOf(false) }

  val timeFormatter = remember(use24Hour) {
    SimpleDateFormat(if (use24Hour) "HH:mm" else "hh:mm a", Locale.getDefault())
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "CITIES (${clocks.size})",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Button(
        onClick = { showAddCityDialog = true },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("add_city_button")
      ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add City")
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(bottom = 100.dp)
    ) {
      items(clocks, key = { it.id }) { clock ->
        val zone = remember(clock.timeZoneId) { TimeZone.getTimeZone(clock.timeZoneId) }
        val zoneCal = remember(ticker, zone) {
          Calendar.getInstance(zone).apply { timeInMillis = ticker }
        }
        val cityTime = remember(ticker, zoneCal) {
          timeFormatter.apply { timeZone = zone }.format(zoneCal.time)
        }
        val hourOfDay = zoneCal.get(Calendar.HOUR_OF_DAY)
        val isDay = hourOfDay in 6..18

        val localZone = TimeZone.getDefault()
        val diffHours = (zone.getOffset(ticker) - localZone.getOffset(ticker)) / (1000 * 60 * 60)
        val diffStr = when {
          diffHours > 0 -> "+${diffHours} hrs ahead"
          diffHours < 0 -> "${diffHours} hrs behind"
          else -> "Same time"
        }

        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(
                    if (isDay) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (isDay) "☀️" else "🌙",
                  fontSize = 20.sp
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = clock.cityName,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${clock.countryName} • $diffStr",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = cityTime,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.width(4.dp))
              IconButton(onClick = { viewModel.deleteCity(clock) }) {
                Icon(
                  Icons.Outlined.Delete,
                  contentDescription = "Delete city",
                  tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }

    if (showAddCityDialog) {
      AddCityDialog(
        availableCities = viewModel.availableCities,
        onDismiss = { showAddCityDialog = false },
        onSelectCity = { option ->
          viewModel.addCity(option)
          showAddCityDialog = false
        }
      )
    }
  }
}

@Composable
fun AddCityDialog(
  availableCities: List<CityOption>,
  onDismiss: () -> Unit,
  onSelectCity: (CityOption) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val filtered = remember(searchQuery) {
    if (searchQuery.isBlank()) availableCities
    else availableCities.filter {
      it.city.contains(searchQuery, ignoreCase = true) ||
      it.country.contains(searchQuery, ignoreCase = true)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Select World City", fontWeight = FontWeight.Bold) },
    text = {
      Column(modifier = Modifier.height(350.dp)) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          label = { Text("Search City or Country") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("search_city_input")
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          items(filtered) { opt ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectCity(opt) }
                .padding(vertical = 10.dp, horizontal = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(opt.city, fontWeight = FontWeight.SemiBold)
                Text(opt.country, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Icon(Icons.Filled.AddCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider()
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun StudyPomodoroTab(viewModel: ClockViewModel) {
  val pomodoroState by viewModel.pomodoroState.collectAsState()
  val isRunning by viewModel.pomodoroIsRunning.collectAsState()
  val secondsLeft by viewModel.pomodoroSecondsLeft.collectAsState()
  val sessions by viewModel.studySessions.collectAsState()

  val mins = secondsLeft / 60
  val secs = secondsLeft % 60
  val timeDisplay = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
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
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = when (pomodoroState) {
            PomodoroState.STUDY -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            PomodoroState.BREAK -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
            PomodoroState.IDLE -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
          }
        ) {
          Text(
            text = when (pomodoroState) {
              PomodoroState.STUDY -> "FOCUS / STUDY SESSION"
              PomodoroState.BREAK -> "RECHARGE / BREAK"
              PomodoroState.IDLE -> "READY TO FOCUS"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = when (pomodoroState) {
              PomodoroState.STUDY -> MaterialTheme.colorScheme.primary
              PomodoroState.BREAK -> MaterialTheme.colorScheme.tertiary
              PomodoroState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = timeDisplay,
          style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-2).sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!isRunning && pomodoroState == PomodoroState.IDLE) {
            Button(
              onClick = { viewModel.startPomodoro(isStudy = true) },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.testTag("start_pomodoro_button")
            ) {
              Icon(Icons.Filled.PlayArrow, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Start Study (25m)")
            }
            OutlinedButton(
              onClick = { viewModel.startPomodoro(isStudy = false) },
              shape = RoundedCornerShape(14.dp)
            ) {
              Text("Break (5m)")
            }
          } else if (isRunning) {
            Button(
              onClick = { viewModel.pausePomodoro() },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.testTag("pause_pomodoro_button")
            ) {
              Icon(Icons.Filled.Pause, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Pause")
            }
            OutlinedButton(
              onClick = { viewModel.resetPomodoro() },
              shape = RoundedCornerShape(14.dp)
            ) {
              Text("Reset")
            }
          } else {
            Button(
              onClick = { viewModel.resumePomodoro() },
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(Icons.Filled.PlayArrow, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Resume")
            }
            OutlinedButton(
              onClick = { viewModel.resetPomodoro() },
              shape = RoundedCornerShape(14.dp)
            ) {
              Text("Reset")
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "RECENT STUDY SESSIONS (${sessions.size})",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.align(Alignment.Start)
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (sessions.isEmpty()) {
      Text(
        text = "Complete your first pomodoro study session to start tracking focus time.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sessions) { session ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📚", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(session.tag, fontWeight = FontWeight.SemiBold)
                  Text(session.sessionDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              Text(
                "${session.durationMinutes} min completed",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun TimerTab(viewModel: ClockViewModel) {
  val isRunning by viewModel.timerIsRunning.collectAsState()
  val secondsLeft by viewModel.timerSecondsLeft.collectAsState()

  val hours = secondsLeft / 3600
  val mins = (secondsLeft % 3600) / 60
  val secs = secondsLeft % 60
  val display = if (hours > 0) {
    String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, mins, secs)
  } else {
    String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
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
        Text(
          text = "COUNTDOWN TIMER",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = display,
          style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1.5).sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!isRunning) {
            Button(
              onClick = { viewModel.startTimer() },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.testTag("start_timer_button")
            ) {
              Icon(Icons.Filled.PlayArrow, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Start")
            }
          } else {
            Button(
              onClick = { viewModel.pauseTimer() },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.testTag("pause_timer_button")
            ) {
              Icon(Icons.Filled.Pause, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Pause")
            }
          }

          OutlinedButton(
            onClick = { viewModel.resetTimer() },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("reset_timer_button")
          ) {
            Text("Reset")
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = "TIMER PRESETS",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.align(Alignment.Start)
    )
    Spacer(modifier = Modifier.height(10.dp))

    val presets = listOf(
      "1 min" to 60,
      "3 min" to 180,
      "5 min" to 300,
      "10 min" to 600,
      "15 min" to 900,
      "30 min" to 1800,
      "45 min" to 2700,
      "1 hour" to 3600
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      presets.chunked(4).forEach { row ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          row.forEach { (label, secsVal) ->
            Surface(
              onClick = { viewModel.setTimerDuration(secsVal) },
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface,
              border = CardDefaults.outlinedCardBorder(),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun StopwatchTab(viewModel: ClockViewModel) {
  val isRunning by viewModel.stopwatchIsRunning.collectAsState()
  val elapsedMillis by viewModel.stopwatchElapsedMillis.collectAsState()
  val laps by viewModel.stopwatchLaps.collectAsState()

  val mins = (elapsedMillis / (1000 * 60)) % 60
  val secs = (elapsedMillis / 1000) % 60
  val hundredths = (elapsedMillis % 1000) / 10
  val timeDisplay = String.format(Locale.getDefault(), "%02d:%02d.%02d", mins, secs, hundredths)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
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
        Text(
          text = "STOPWATCH",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = timeDisplay,
          style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1.5).sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!isRunning) {
            Button(
              onClick = { viewModel.startStopwatch() },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.testTag("start_stopwatch_button")
            ) {
              Icon(Icons.Filled.PlayArrow, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Start")
            }
          } else {
            Button(
              onClick = { viewModel.pauseStopwatch() },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.testTag("pause_stopwatch_button")
            ) {
              Icon(Icons.Filled.Pause, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Pause")
            }
            Button(
              onClick = { viewModel.recordLap() },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.testTag("lap_stopwatch_button")
            ) {
              Icon(Icons.Filled.Flag, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Lap")
            }
          }

          OutlinedButton(
            onClick = { viewModel.resetStopwatch() },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("reset_stopwatch_button")
          ) {
            Text("Reset")
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "LAPS (${laps.size})",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.align(Alignment.Start)
    )
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(bottom = 100.dp)
    ) {
      items(laps) { lap ->
        val lapMins = (lap.lapTimeMillis / (1000 * 60)) % 60
        val lapSecs = (lap.lapTimeMillis / 1000) % 60
        val lapHund = (lap.lapTimeMillis % 1000) / 10
        val lapStr = String.format(Locale.getDefault(), "%02d:%02d.%02d", lapMins, lapSecs, lapHund)

        val totalMins = (lap.totalTimeMillis / (1000 * 60)) % 60
        val totalSecs = (lap.totalTimeMillis / 1000) % 60
        val totalHund = (lap.totalTimeMillis % 1000) / 10
        val totalStr = String.format(Locale.getDefault(), "%02d:%02d.%02d", totalMins, totalSecs, totalHund)

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Lap ${lap.lapNumber}", fontWeight = FontWeight.Bold)
            Text("+$lapStr", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Text(totalStr, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}
