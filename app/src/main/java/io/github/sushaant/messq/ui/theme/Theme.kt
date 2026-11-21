package io.github.sushaant.messq.ui.theme

import android.app.Activity
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

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimaryGreen,
    primaryContainer = PrimaryContainerGreen,
    onPrimaryContainer = OnPrimaryContainerGreen,
    secondary = SecondaryAmber,
    onSecondary = OnSecondaryAmber,
    secondaryContainer = PrimaryContainerGreen, // Fixes SegmentedButton & Chip active fill
    onSecondaryContainer = OnPrimaryContainerGreen,
    background = LightBackground,
    surface = LightSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryContainerGreen,
    onPrimary = OnPrimaryContainerGreen,
    primaryContainer = PrimaryGreen,
    onPrimaryContainer = Color.White,
    secondary = SecondaryAmber,
    onSecondary = OnSecondaryAmber,
    secondaryContainer = PrimaryGreen, // Fixes SegmentedButton & Chip active fill in dark mode
    onSecondaryContainer = Color.White,
    background = DarkBackground,
    surface = DarkSurface
)

@Composable
fun MessQTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false so it enforces your custom brand colors instead of wallpaper colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Your existing typography
        content = content
    )
}