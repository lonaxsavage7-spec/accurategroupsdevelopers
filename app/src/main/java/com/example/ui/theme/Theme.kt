package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = CyanAccent,
  onPrimary = Color(0xFF001F28),
  primaryContainer = Color(0xFF004D5A),
  onPrimaryContainer = Color(0xFFA6EEFF),
  
  secondary = PurpleAccent,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF381E72),
  onSecondaryContainer = Color(0xFFE8DDFF),
  
  tertiary = EmeraldHealth,
  onTertiary = Color(0xFF003919),
  tertiaryContainer = Color(0xFF005327),
  onTertiaryContainer = Color(0xFF6CF89B),
  
  background = SpaceDarkBg,
  onBackground = TextWhitePrimary,
  surface = SpaceDarkSurface,
  onSurface = TextWhitePrimary,
  surfaceVariant = SpaceDarkSurfaceVariant,
  onSurfaceVariant = TextMuted,
  
  error = CoralAlert,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF00687A),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFA6EEFF),
  onPrimaryContainer = Color(0xFF001F26),
  
  secondary = Color(0xFF6750A4),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFE8DDFF),
  onSecondaryContainer = Color(0xFF22005D),
  
  tertiary = Color(0xFF006D34),
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFF98F7AF),
  onTertiaryContainer = Color(0xFF00210A),
  
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF0F172A),
  surface = Color.White,
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF475569),
  
  error = Color(0xFFBA1A1A),
  onError = Color.White
)

@Composable
fun SpaceLensTheme(
  darkTheme: Boolean = true, // Default to sleek futuristic dark mode for SpaceLens
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
