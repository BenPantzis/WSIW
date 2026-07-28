package com.bsp.wsiw.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

private val CinemaColorScheme = darkColorScheme(
    primary = GoldDefault,
    onPrimary = GoldDeep,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,

    secondary = GoldMuted,
    onSecondary = GoldMutedContainer,
    secondaryContainer = GoldMutedContainer,
    onSecondaryContainer = GoldMutedLight,

    tertiary = ProjectorBlue,
    onTertiary = ProjectorBlueDark,
    tertiaryContainer = ProjectorBlueDark,
    onTertiaryContainer = ProjectorBlueLight,

    error = ErrorRed,
    onError = ErrorRedDark,
    errorContainer = ErrorRedDark,
    onErrorContainer = ErrorRed,

    background = CinemaBlack,
    onBackground = IvoryWhite,

    surface = CinemaDark,
    onSurface = IvoryWhite,
    surfaceVariant = CinemaSurface,
    onSurfaceVariant = IvoryMuted,
    surfaceTint = GoldDefault,

    outline = CinemaOutline,
    outlineVariant = CinemaOutlineVariant,

    inverseSurface = IvoryWhite,
    inverseOnSurface = CinemaDark,
    inversePrimary = GoldDark,
)

private val CinemaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun WSIWTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = CinemaColorScheme,
            typography = CinemaTypography,
            shapes = CinemaShapes,
            content = content,
        )
    }
}

object AppTheme {
    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current
}
