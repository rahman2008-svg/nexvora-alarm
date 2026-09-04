package com.example.nexvora.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexvora.ui.alarms.AlarmsScreen
import com.example.nexvora.ui.clock.ClockScreen
import com.example.nexvora.ui.detail.SleepDetailScreen
import com.example.nexvora.ui.detail.StreakDetailScreen
import com.example.nexvora.ui.home.HomeScreen
import com.example.nexvora.ui.onboarding.OnboardingScreen
import com.example.nexvora.ui.routine.RoutineScreen
import com.example.nexvora.ui.settings.SettingsScreen
import com.example.nexvora.ui.viewmodel.*
import com.example.ui.theme.NexVoraTheme

sealed class AppDestination(val route: String) {
  data object Home : AppDestination("home")
  data object Alarms : AppDestination("alarms")
  data object Routine : AppDestination("routine")
  data object Clock : AppDestination("clock")
  data object Settings : AppDestination("settings")
  data object StreakDetail : AppDestination("streak_detail")
  data object SleepDetail : AppDestination("sleep_detail")
}

data class NavigationTabItem(
  val destination: AppDestination,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val tag: String
)

@Composable
fun NexVoraApp(
  mainViewModel: MainViewModel = viewModel(),
  alarmsViewModel: AlarmsViewModel = viewModel(),
  routineViewModel: RoutineViewModel = viewModel(),
  clockViewModel: ClockViewModel = viewModel(),
  settingsViewModel: SettingsViewModel = viewModel(),
  streakViewModel: StreakViewModel = viewModel(),
  sleepViewModel: SleepViewModel = viewModel()
) {
  val settings by mainViewModel.appSettings.collectAsState()

  NexVoraTheme(themeMode = settings.themeMode) {
    if (!settings.hasCompletedOnboarding) {
      OnboardingScreen(
        onComplete = { mainViewModel.completeOnboarding() },
        onCreateDefaultAlarm = { mainViewModel.createDefaultMorningAlarm() }
      )
    } else {
      var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Home) }
      var targetClockTab by remember { mutableIntStateOf(0) }

      val navigationItems = listOf(
        NavigationTabItem(
          destination = AppDestination.Home,
          label = "Home",
          selectedIcon = Icons.Filled.Home,
          unselectedIcon = Icons.Outlined.Home,
          tag = "nav_tab_home"
        ),
        NavigationTabItem(
          destination = AppDestination.Alarms,
          label = "Alarms",
          selectedIcon = Icons.Filled.Alarm,
          unselectedIcon = Icons.Outlined.Alarm,
          tag = "nav_tab_alarms"
        ),
        NavigationTabItem(
          destination = AppDestination.Routine,
          label = "Routine",
          selectedIcon = Icons.Filled.Checklist,
          unselectedIcon = Icons.Outlined.Checklist,
          tag = "nav_tab_routine"
        ),
        NavigationTabItem(
          destination = AppDestination.Clock,
          label = "Clock",
          selectedIcon = Icons.Filled.Schedule,
          unselectedIcon = Icons.Outlined.Schedule,
          tag = "nav_tab_clock"
        ),
        NavigationTabItem(
          destination = AppDestination.Settings,
          label = "Settings",
          selectedIcon = Icons.Filled.Settings,
          unselectedIcon = Icons.Outlined.Settings,
          tag = "nav_tab_settings"
        )
      )

      val showBottomBar = currentDestination !in listOf(
        AppDestination.StreakDetail,
        AppDestination.SleepDetail
      )

      Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
          if (showBottomBar) {
            Column {
              HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                thickness = 1.dp
              )
              NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.height(72.dp)
              ) {
                navigationItems.forEach { item ->
                  val isSelected = currentDestination == item.destination
                  NavigationBarItem(
                    selected = isSelected,
                    onClick = { currentDestination = item.destination },
                    icon = {
                      Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                      )
                    },
                    label = {
                      Text(
                        text = item.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.labelSmall
                      )
                    },
                    colors = NavigationBarItemDefaults.colors(
                      selectedIconColor = MaterialTheme.colorScheme.primary,
                      selectedTextColor = MaterialTheme.colorScheme.primary,
                      indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                      unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                      unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.testTag(item.tag)
                  )
                }
              }
            }
          }
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          when (currentDestination) {
            AppDestination.Home -> HomeScreen(
              viewModel = mainViewModel,
              onNavigateToAlarms = { currentDestination = AppDestination.Alarms },
              onNavigateToRoutine = { currentDestination = AppDestination.Routine },
              onNavigateToClockTab = { tabIndex ->
                targetClockTab = tabIndex
                currentDestination = AppDestination.Clock
              },
              onOpenStreakDetail = { currentDestination = AppDestination.StreakDetail },
              onOpenSleepDetail = { currentDestination = AppDestination.SleepDetail }
            )

            AppDestination.Alarms -> AlarmsScreen(
              viewModel = alarmsViewModel,
              use24Hour = settings.use24HourFormat
            )

            AppDestination.Routine -> RoutineScreen(
              viewModel = routineViewModel,
              use24Hour = settings.use24HourFormat
            )

            AppDestination.Clock -> ClockScreen(
              viewModel = clockViewModel,
              initialTab = targetClockTab,
              use24Hour = settings.use24HourFormat
            )

            AppDestination.Settings -> SettingsScreen(
              viewModel = settingsViewModel
            )

            AppDestination.StreakDetail -> StreakDetailScreen(
              viewModel = streakViewModel,
              onBack = { currentDestination = AppDestination.Home }
            )

            AppDestination.SleepDetail -> SleepDetailScreen(
              viewModel = sleepViewModel,
              onBack = { currentDestination = AppDestination.Home }
            )
          }
        }
      }
    }
  }
}
