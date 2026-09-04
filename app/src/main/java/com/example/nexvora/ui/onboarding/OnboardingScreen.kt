package com.example.nexvora.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingStep(
  val icon: ImageVector,
  val title: String,
  val subtitle: String,
  val highlight: String
)

@Composable
fun OnboardingScreen(
  onComplete: () -> Unit,
  onCreateDefaultAlarm: () -> Unit,
  modifier: Modifier = Modifier
) {
  val steps = listOf(
    OnboardingStep(
      icon = Icons.Filled.Alarm,
      title = "Welcome to NexVora",
      subtitle = "Not just an alarm clock. A complete system engineered to build morning discipline, daily focus, and sleep wellness.",
      highlight = "Wake • Focus • Achieve"
    ),
    OnboardingStep(
      icon = Icons.Filled.Psychology,
      title = "Wake-up Challenges",
      subtitle = "Conquer sleep inertia immediately with engaging Math problems, Memory tests, Number patterns, or QR Code missions.",
      highlight = "No More Snoozing Away Life"
    ),
    OnboardingStep(
      icon = Icons.Filled.Checklist,
      title = "Routines & Goals",
      subtitle = "Link your morning alarms directly to essential habits like hydration, workouts, study sessions, and daily goals.",
      highlight = "Seamless Morning Execution"
    ),
    OnboardingStep(
      icon = Icons.Filled.School,
      title = "Study Mode & Pomodoro",
      subtitle = "Integrated Pomodoro timer, precision stopwatch, countdown timers, and world clocks for global productivity.",
      highlight = "Built for Focused Work"
    ),
    OnboardingStep(
      icon = Icons.Filled.LocalFireDepartment,
      title = "Streaks & Achievements",
      subtitle = "Track consecutive wake-up streaks and unlock badges from 'First Dawn' to 'Centurion of Time'.",
      highlight = "Daily Momentum"
    ),
    OnboardingStep(
      icon = Icons.Filled.DoneAll,
      title = "You Are Ready",
      subtitle = "Let's configure your primary morning wake-up alarm (06:30 AM with Math Challenge & Gentle Wake).",
      highlight = "Start Your Journey"
    )
  )

  var currentStepIndex by remember { mutableIntStateOf(0) }
  val currentStep = steps[currentStepIndex]

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 28.dp, vertical = 40.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Skip Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        if (currentStepIndex < steps.size - 1) {
          TextButton(
            onClick = {
              onCreateDefaultAlarm()
              onComplete()
            }
          ) {
            Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        } else {
          Spacer(modifier = Modifier.height(48.dp))
        }
      }

      // Middle Content with Smooth Animation
      AnimatedContent(
        targetState = currentStep,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "onboarding_step"
      ) { step ->
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(110.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = step.icon,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(56.dp)
            )
          }

          Spacer(modifier = Modifier.height(28.dp))

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
          ) {
            Text(
              text = step.highlight.uppercase(),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = step.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = step.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
          )
        }
      }

      // Bottom Progress & Action Row
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Step Indicator Dots
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          steps.forEachIndexed { index, _ ->
            val isCurrent = index == currentStepIndex
            Box(
              modifier = Modifier
                .height(8.dp)
                .width(if (isCurrent) 24.dp else 8.dp)
                .clip(CircleShape)
                .background(
                  if (isCurrent) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (currentStepIndex < steps.size - 1) {
          Button(
            onClick = { currentStepIndex++ },
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("onboarding_next_button"),
            shape = RoundedCornerShape(16.dp)
          ) {
            Text("Next", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
        } else {
          Button(
            onClick = {
              onCreateDefaultAlarm()
              onComplete()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("onboarding_finish_button"),
            shape = RoundedCornerShape(16.dp)
          ) {
            Text("Set Wake-Up & Begin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
