package com.tracker.subscription.ui.theme

import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ============================================================================
// THEME-AWARE COLOR PROVIDER
// ============================================================================

object ThemeColors {
    fun getBackgroundColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkBackground else LightBackground
    }

    fun getTextColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    }

    fun getCardBackgroundColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkCardBackground else LightCardBackground
    }

    fun getPrimaryColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkPrimary else LightPrimary
    }

    fun getSecondaryColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkSecondary else LightSecondary
    }

    fun getGreenColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) AccentGreen else AccentGreen
    }

    fun getBlueBgColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkPrime else LightWhite
    }

    fun getLightGreyColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkLightGrey else LightLightGrey
    }

    fun getLightWhiteBothColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) LightLightGrey else LightLightGrey
    }

    fun getCalenderBorder(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkLightGrey else LightLightGrey
    }
    fun getLightOrangeColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkOrangeBG else LightOrangeBg
    }

    fun getFreeTrailTextColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkOrangeDark else DarkOrangeBG
    }

    fun getGreyColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkGrey else DarkGrey
    }

    fun getDarkGreyColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkDarkGrey else LightDarkGrey
    }

    fun getTextGreyColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkTextGrey else LightTextGrey
    }

    fun getBlueBgLightColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkBlueBgLight else LightBlueBgLight
    }

    fun getBlueLightColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) LightSecondary else LightBlueLight
    }

    fun getDarkBlueColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkDarkBlue else LightPrimary
    }

    fun getHeaderColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) LightTextGrey else DarkDarkBlue
    }

    fun getHeaderBlueColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) BlueLight else DarkBlueBg
    }

    fun getOrangeColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkOrange else LightOrange
    }

    fun getRedColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) DarkRed else LightRed
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
)

data class ThemeState(
    val isDarkTheme: Boolean = false,
    val toggleTheme: () -> Unit = {}
)

@Composable
fun SubscriptionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    themeState: ThemeState = ThemeState(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeState.isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

