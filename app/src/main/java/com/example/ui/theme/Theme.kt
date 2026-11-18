package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = AgsCleanDarkAccent,
    onPrimary = Color.White,
    primaryContainer = AgsCleanDarkCardBg,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF22D3EE),
    onSecondary = Color.Black,
    secondaryContainer = AgsCleanDarkCardBg,
    onSecondaryContainer = Color.White,
    tertiary = NeonEmerald,
    onTertiary = Color.Black,
    background = AgsCleanDarkBg,
    onBackground = AgsCleanDarkText,
    surface = AgsCleanDarkCardBg,
    onSurface = AgsCleanDarkText,
    surfaceVariant = AgsCleanDarkCardBg,
    onSurfaceVariant = AgsCleanDarkSubtext,
    outline = AgsCleanDarkCardBorder,
    outlineVariant = Color(0xFF1F3A30),
    error = CoralAlert,
    onError = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AgsCleanLightAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = AgsCleanLightCardBg,
    onSecondaryContainer = AgsCleanLightText,
    tertiary = NeonEmerald,
    onTertiary = Color.White,
    background = AgsCleanLightBg,
    onBackground = AgsCleanLightText,
    surface = AgsCleanLightCardBg,
    onSurface = AgsCleanLightText,
    surfaceVariant = Color(0xFFE9ECEF),
    onSurfaceVariant = AgsCleanLightSubtext,
    outline = AgsCleanLightCardBorder,
    outlineVariant = Color(0xFFCED4DA),
    error = CoralAlert,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  remoteAccentColorHex: String? = null,
  content: @Composable () -> Unit,
) {
  val parsedAccent = androidx.compose.runtime.remember(remoteAccentColorHex) {
    remoteAccentColorHex?.let {
      runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
    }
  }
  val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val colorScheme = if (parsedAccent != null) {
    baseScheme.copy(primary = parsedAccent)
  } else {
    baseScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
