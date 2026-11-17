package edu.ap.dakaniktochniwegsmijten.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = LightGreen,
    onPrimaryContainer = DeepGreen,
    inversePrimary = LightGreen,

    secondary = BurntOrange,
    onSecondary = Color.White,
    secondaryContainer = LightOrange,
    onSecondaryContainer = DeepOrange,

    tertiary = ForestGreen,
    onTertiary = Color.White,
    tertiaryContainer = LightGreen,
    onTertiaryContainer = DeepGreen,

    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = SoftCream,
    onSurfaceVariant = MutedInk,
    surfaceTint = ForestGreen,

    inverseSurface = Ink,
    inverseOnSurface = Cream,

    outline = Border,
    outlineVariant = LightBorder,
    scrim = Color.Black,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = LightRed,
    onErrorContainer = DeepRed,

    surfaceBright = Cream,
    surfaceDim = WarmBeige,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Cream,
    surfaceContainer = SoftCream,
    surfaceContainerHigh = WarmBeige,
    surfaceContainerHighest = WarmBeige,

    primaryFixed = LightGreen,
    primaryFixedDim = LightGreen,
    onPrimaryFixed = DeepGreen,
    onPrimaryFixedVariant = ForestGreen,

    secondaryFixed = LightOrange,
    secondaryFixedDim = LightOrange,
    onSecondaryFixed = DeepOrange,
    onSecondaryFixedVariant = BurntOrange,

    tertiaryFixed = LightGreen,
    tertiaryFixedDim = LightGreen,
    onTertiaryFixed = DeepGreen,
    onTertiaryFixedVariant = ForestGreen
)

@Composable
fun DaKanIkTochNiWegsmijtenTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}