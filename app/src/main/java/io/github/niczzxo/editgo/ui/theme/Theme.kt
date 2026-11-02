package io.github.niczzxo.editgo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EditGoColorScheme = darkColorScheme(
    primary = EditGoPrimary,
    onPrimary = EditGoOnBackground,
    primaryContainer = EditGoPrimaryContainer,
    onPrimaryContainer = EditGoOnBackground,
    background = EditGoBackground,
    onBackground = EditGoOnBackground,
    surface = EditGoSurface,
    onSurface = EditGoOnSurface,
    surfaceVariant = EditGoSurfaceVariant,
    onSurfaceVariant = EditGoOnSurfaceVariant,
    outline = EditGoOutline,
    error = EditGoError,
)

@Composable
fun EditGoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EditGoColorScheme,
        typography = EditGoTypography,
        content = content,
    )
}
