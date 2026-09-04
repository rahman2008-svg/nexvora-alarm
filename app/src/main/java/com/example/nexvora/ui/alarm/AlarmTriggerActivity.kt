package com.example.nexvora.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexvora.alarm.engine.AlarmScheduler
import com.example.nexvora.alarm.engine.AlarmService
import com.example.nexvora.challenge.ChallengeEngine
import com.example.nexvora.challenge.WakeUpChallenge
import com.example.nexvora.data.db.AppDatabase
import com.example.nexvora.data.model.AlarmEntity
import com.example.nexvora.data.repository.StreakRepository
import com.example.ui.theme.NexVoraTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class AlarmTriggerActivity : ComponentActivity() {
  private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Ensure activity shows on lockscreen and turns on screen
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
      val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
      keyguardManager?.requestDismissKeyguard(this, null)
    } else {
      @Suppress("DEPRECATION")
      window.addFlags(
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
      )
    }

    val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
    val isSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false)
    val snoozeCount = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0)

    setContent {
      NexVoraTheme {
        var alarm by remember { mutableStateOf<AlarmEntity?>(null) }

        LaunchedEffect(alarmId) {
          if (alarmId != -1L) {
            val db = AppDatabase.getDatabase(applicationContext)
            alarm = db.alarmDao().getAlarmById(alarmId)
          }
        }

        AlarmTriggerContent(
          alarm = alarm,
          isSnooze = isSnooze,
          currentSnoozeCount = snoozeCount,
          onDismissAlarm = { challengeType ->
            dismissAlarm(alarm, challengeType)
          },
          onSnoozeAlarm = {
            snoozeAlarm(alarm, snoozeCount)
          }
        )
      }
    }
  }

  private fun dismissAlarm(alarm: AlarmEntity?, challengeType: String?) {
    // 1. Stop Alarm Service
    val stopIntent = Intent(this, AlarmService::class.java).apply {
      action = AlarmService.ACTION_STOP_ALARM
    }
    startService(stopIntent)

    // 2. Increment streak & unlock achievements
    activityScope.launch(Dispatchers.IO) {
      val db = AppDatabase.getDatabase(applicationContext)
      val streakRepo = StreakRepository(db)
      streakRepo.recordWakeUp(challengeType)
      val streakEntity = db.streakDao().getStreak().firstOrNull()
      val currentStreak = streakEntity?.currentStreak ?: 1

      com.example.nexvora.alarm.engine.SmartNotificationManager.showMorningSummaryNotification(
        context = applicationContext,
        streakCount = currentStreak,
        alarmLabel = alarm?.label ?: "Alarm",
        goalName = alarm?.goalName ?: ""
      )
    }

    Toast.makeText(this, "Good Morning! Have a great day.", Toast.LENGTH_SHORT).show()
    finish()
  }

  private fun snoozeAlarm(alarm: AlarmEntity?, currentSnoozeCount: Int) {
    if (alarm != null) {
      val snoozeMins = alarm.snoozeDurationMinutes
      AlarmScheduler.scheduleSnooze(this, alarm.id, snoozeMins, currentSnoozeCount)
    }

    val stopIntent = Intent(this, AlarmService::class.java).apply {
      action = AlarmService.ACTION_STOP_ALARM
    }
    startService(stopIntent)

    Toast.makeText(this, "Alarm snoozed", Toast.LENGTH_SHORT).show()
    finish()
  }

  override fun onDestroy() {
    super.onDestroy()
    activityScope.cancel()
  }
}

@Composable
fun AlarmTriggerContent(
  alarm: AlarmEntity?,
  isSnooze: Boolean,
  currentSnoozeCount: Int,
  onDismissAlarm: (String?) -> Unit,
  onSnoozeAlarm: () -> Unit
) {
  var currentTimeStr by remember { mutableStateOf("") }
  var currentDateStr by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    while (true) {
      val now = Date()
      currentTimeStr = timeFormat.format(now)
      currentDateStr = dateFormat.format(now)
      delay(1000L)
    }
  }

  val challengeType = alarm?.challengeType ?: "NONE"
  val challengeDifficulty = alarm?.challengeDifficulty ?: "MEDIUM"
  val maxSnoozes = alarm?.maxSnoozeCount ?: 3
  val canSnooze = currentSnoozeCount < maxSnoozes

  // Challenge state
  var mathChallenge by remember { mutableStateOf<WakeUpChallenge.Math?>(null) }
  var memoryChallenge by remember { mutableStateOf<WakeUpChallenge.Memory?>(null) }
  var patternChallenge by remember { mutableStateOf<WakeUpChallenge.Pattern?>(null) }
  var memoryTapped by remember { mutableStateOf<List<Int>>(emptyList()) }
  var memoryPhaseMemorize by remember { mutableStateOf(true) }
  var qrEnteredCode by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(challengeType) {
    when (challengeType) {
      "MATH" -> mathChallenge = ChallengeEngine.generateMathChallenge(challengeDifficulty)
      "MEMORY" -> {
        val mem = ChallengeEngine.generateMemoryChallenge(challengeDifficulty)
        memoryChallenge = mem
        memoryPhaseMemorize = true
        delay(mem.displayDurationMs)
        memoryPhaseMemorize = false
      }
      "PATTERN" -> patternChallenge = ChallengeEngine.generatePatternChallenge()
      else -> Unit
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Clock Display
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 28.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) {
          Text(
            text = if (isSnooze) "SNOOZED ALARM ($currentSnoozeCount/$maxSnoozes)" else "SCHEDULED ALARM",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = currentTimeStr,
          style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1).sp
          ),
          color = MaterialTheme.colorScheme.onBackground
        )

        Text(
          text = currentDateStr,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = alarm?.label ?: "Wake Up!",
          style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )

        if (!alarm?.goalName.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "🎯 Goal: ${alarm?.goalName}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.tertiary
          )
        }

        if (!alarm?.routineName.isNullOrBlank()) {
          Text(
            text = "📋 Next: ${alarm?.routineName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Middle: Challenge Section
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 24.dp)
      ) {
        errorMessage?.let { err ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            modifier = Modifier.padding(bottom = 12.dp)
          ) {
            Text(
              text = err,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }

        when (challengeType) {
          "MATH" -> {
            mathChallenge?.let { challenge ->
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(20.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "Solve to Dismiss Alarm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = "${challenge.expression} = ?",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.height(20.dp))

                  // Options grid
                  challenge.options.chunked(2).forEach { rowOpts ->
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                      rowOpts.forEach { opt ->
                        Button(
                          onClick = {
                            if (opt == challenge.correctAnswer) {
                              onDismissAlarm("MATH")
                            } else {
                              errorMessage = "Incorrect answer! Try again."
                            }
                          },
                          shape = RoundedCornerShape(14.dp),
                          modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("math_option_$opt")
                        ) {
                          Text(opt.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                      }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                  }
                }
              }
            }
          }

          "MEMORY" -> {
            memoryChallenge?.let { challenge ->
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(20.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = if (memoryPhaseMemorize) "MEMORIZE THE SEQUENCE" else "TAP IN THE EXACT SEQUENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.height(14.dp))

                  if (memoryPhaseMemorize) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      challenge.sequence.forEach { num ->
                        Surface(
                          shape = CircleShape,
                          color = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(44.dp)
                        ) {
                          Box(contentAlignment = Alignment.Center) {
                            Text(num.toString(), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                          }
                        }
                      }
                    }
                  } else {
                    // Sequence input progress
                    Text(
                      text = "Tapped: ${memoryTapped.joinToString(" ")} (${memoryTapped.size}/${challenge.sequence.size})",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val buttons = (1..6).toList().chunked(3)
                    buttons.forEach { row ->
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                      ) {
                        row.forEach { num ->
                          OutlinedButton(
                            onClick = {
                              val newTapped = memoryTapped + num
                              val expected = challenge.sequence.take(newTapped.size)
                              if (newTapped == expected) {
                                if (newTapped.size == challenge.sequence.size) {
                                  onDismissAlarm("MEMORY")
                                } else {
                                  memoryTapped = newTapped
                                }
                              } else {
                                errorMessage = "Wrong number! Sequence reset."
                                memoryTapped = emptyList()
                              }
                            },
                            shape = CircleShape,
                            modifier = Modifier
                              .weight(1f)
                              .height(50.dp)
                              .testTag("memory_tap_$num")
                          ) {
                            Text(num.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                          }
                        }
                      }
                      Spacer(modifier = Modifier.height(8.dp))
                    }
                  }
                }
              }
            }
          }

          "PATTERN" -> {
            patternChallenge?.let { challenge ->
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(20.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text("COMPLETE THE PATTERN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(challenge.sequenceDisplay, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                  Spacer(modifier = Modifier.height(16.dp))

                  challenge.options.chunked(2).forEach { rowOpts ->
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      rowOpts.forEach { opt ->
                        Button(
                          onClick = {
                            if (opt == challenge.correctAnswer) {
                              onDismissAlarm("PATTERN")
                            } else {
                              errorMessage = "Incorrect pattern number! Try again."
                            }
                          },
                          shape = RoundedCornerShape(12.dp),
                          modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                          Text(opt.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                      }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                  }
                }
              }
            }
          }

          "QR" -> {
            Card(
              shape = RoundedCornerShape(24.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text("QR CODE / PASSCODE MISSION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                val targetCode = alarm?.qrTargetCode?.ifBlank { "WAKEUP" } ?: "WAKEUP"
                Text("Enter passkey: $targetCode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                  value = qrEnteredCode,
                  onValueChange = { qrEnteredCode = it },
                  label = { Text("Code") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().testTag("qr_input_field")
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                  onClick = {
                    if (qrEnteredCode.trim().equals(targetCode.trim(), ignoreCase = true)) {
                      onDismissAlarm("QR")
                    } else {
                      errorMessage = "Code does not match target!"
                    }
                  },
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("Verify & Dismiss")
                }
              }
            }
          }

          else -> {
            // Standard Dismiss
            Button(
              onClick = { onDismissAlarm(null) },
              modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("dismiss_alarm_button"),
              shape = RoundedCornerShape(20.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Icon(Icons.Filled.NotificationsOff, contentDescription = null, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(12.dp))
              Text("Dismiss Alarm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Bottom Snooze Row
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
      ) {
        if (canSnooze) {
          OutlinedButton(
            onClick = onSnoozeAlarm,
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("snooze_alarm_button"),
            shape = RoundedCornerShape(16.dp)
          ) {
            Icon(Icons.Filled.Snooze, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            val mins = alarm?.snoozeDurationMinutes ?: 10
            Text("Snooze ($mins min) • $currentSnoozeCount/$maxSnoozes used")
          }
        } else {
          Text(
            text = "Max snoozes reached ($maxSnoozes/$maxSnoozes). You must wake up!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
