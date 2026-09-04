package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode {
  SYSTEM,
  LIGHT,
  DARK,
  AMOLED
}

private val DarkColorScheme =
  darkColorScheme(
    primary = SleekLavender,
    onPrimary = SleekOnPurple,
    primaryContainer = SleekDeepPurple,
    onPrimaryContainer = SleekLavenderContainer,
    secondary = SleekLavender,
    onSecondary = SleekOnPurple,
    secondaryContainer = SleekSurfaceElevated,
    onSecondaryContainer = SleekLavender,
    tertiary = AmberGlow,
    onTertiary = SleekOnPurple,
    tertiaryContainer = SleekDeepPurple,
    onTertiaryContainer = SleekLavenderContainer,
    background = SleekBackground,
    onBackground = SleekTextSlate100,
    surface = SleekSurface,
    onSurface = SleekTextWhite,
    surfaceVariant = SleekSurfaceElevated,
    onSurfaceVariant = SleekTextSlate400,
    outline = SleekBorder,
    outlineVariant = SleekBorderSubtle
  )

private val AmoledColorScheme =
  darkColorScheme(
    primary = SleekLavender,
    onPrimary = SleekOnPurple,
    primaryContainer = SleekDeepPurple,
    onPrimaryContainer = SleekLavenderContainer,
    secondary = SleekLavender,
    onSecondary = SleekOnPurple,
    secondaryContainer = SleekSurfaceElevated,
    onSecondaryContainer = SleekLavender,
    tertiary = AmberGlow,
    onTertiary = SleekOnPurple,
    tertiaryContainer = SleekDeepPurple,
    onTertiaryContainer = SleekLavenderContainer,
    background = Color(0xFF000000),
    onBackground = SleekTextSlate100,
    surface = SleekSurface,
    onSurface = SleekTextWhite,
    surfaceVariant = SleekSurfaceElevated,
    onSurfaceVariant = SleekTextSlate400,
    outline = SleekBorder,
    outlineVariant = SleekBorderSubtle
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SleekDeepPurple,
    onPrimary = Color.White,
    primaryContainer = SleekLavenderContainer,
    onPrimaryContainer = SleekOnPurple,
    secondary = SleekDeepPurple,
    onSecondary = Color.White,
    secondaryContainer = SleekLavenderContainer,
    onSecondaryContainer = SleekOnPurple,
    tertiary = AmberGlow,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDF9E),
    onTertiaryContainer = Color(0xFF261A00),
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
  )

@Composable
fun NexVoraTheme(
  themeMode: AppThemeMode = AppThemeMode.DARK,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val isDark = when (themeMode) {
    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK, AppThemeMode.AMOLED -> true
  }

  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    themeMode == AppThemeMode.AMOLED -> AmoledColorScheme
    isDark -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  NexVoraTheme(
    themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
    dynamicColor = dynamicColor,
    content = content
  )
}
