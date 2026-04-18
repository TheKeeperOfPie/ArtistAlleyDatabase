package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.theme_auto
import artistalleydatabase.modules.utils_compose.generated.resources.theme_black
import artistalleydatabase.modules.utils_compose.generated.resources.theme_dark
import artistalleydatabase.modules.utils_compose.generated.resources.theme_light
import artistalleydatabase.modules.utils_compose.generated.resources.theme_miku
import org.jetbrains.compose.resources.StringResource

enum class AppThemeSetting(val textRes: StringResource) {
    AUTO(Res.string.theme_auto),
    LIGHT(Res.string.theme_light),
    DARK(Res.string.theme_dark),
    BLACK(Res.string.theme_black),
    MIKU(Res.string.theme_miku),
    ;
}

val LocalAppTheme = compositionLocalOf { AppThemeSetting.AUTO }

@Composable
expect fun AppTheme(appTheme: () -> AppThemeSetting, content: @Composable () -> Unit)

object AppTheme {
    val mikuTheme = darkColorScheme(
        primary = primaryMiku,
        onPrimary = onPrimaryMiku,
        primaryContainer = primaryContainerMiku,
        onPrimaryContainer = onPrimaryContainerMiku,
        secondary = secondaryMiku,
        onSecondary = onSecondaryMiku,
        secondaryContainer = secondaryContainerMiku,
        onSecondaryContainer = onSecondaryContainerMiku,
        tertiary = tertiaryMiku,
        onTertiary = onTertiaryMiku,
        tertiaryContainer = tertiaryContainerMiku,
        onTertiaryContainer = onTertiaryContainerMiku,
        error = errorMiku,
        onError = onErrorMiku,
        errorContainer = errorContainerMiku,
        onErrorContainer = onErrorContainerMiku,
        background = backgroundMiku,
        onBackground = onBackgroundMiku,
        surface = surfaceMiku,
        onSurface = onSurfaceMiku,
        surfaceVariant = surfaceVariantMiku,
        onSurfaceVariant = onSurfaceVariantMiku,
        outline = outlineMiku,
        outlineVariant = outlineVariantMiku,
        scrim = scrimMiku,
        inverseSurface = inverseSurfaceMiku,
        inverseOnSurface = inverseOnSurfaceMiku,
        inversePrimary = inversePrimaryMiku,
        surfaceDim = surfaceDimMiku,
        surfaceBright = surfaceBrightMiku,
        surfaceContainerLowest = surfaceContainerLowestMiku,
        surfaceContainerLow = surfaceContainerLowMiku,
        surfaceContainer = surfaceContainerMiku,
        surfaceContainerHigh = surfaceContainerHighMiku,
        surfaceContainerHighest = surfaceContainerHighestMiku,
    )
}

internal val primaryMiku = Color(0xFF00DBE4)
internal val onPrimaryMiku = Color(0xFF003739)
internal val primaryContainerMiku = Color(0xFF00DBE4)
internal val onPrimaryContainerMiku = Color(0xFF005C60)
internal val secondaryMiku = Color(0xFFADBDCF)
internal val onSecondaryMiku = Color(0xFF3A3F45)
internal val secondaryContainerMiku = Color(0xFF52BFC2)
internal val onSecondaryContainerMiku = Color(0xFF3D4C5C)
internal val tertiaryMiku = Color(0xffef17ad)
internal val onTertiaryMiku = Color(0xFF002F35)
internal val tertiaryContainerMiku = Color(0xFF006A77)
internal val onTertiaryContainerMiku = Color(0xFF99E7F6)
internal val errorMiku = Color(0xfffd2b32)
internal val onErrorMiku = Color(0xFF35001F)
internal val errorContainerMiku = Color(0xfffb497e)
internal val onErrorContainerMiku = Color(0xFF420929)
internal val backgroundMiku = Color(0xFF1E1F23)
internal val onBackgroundMiku = Color(0xFFDCE4E4)
internal val surfaceMiku = Color(0xFF0D1515)
internal val onSurfaceMiku = Color(0xFFDCE4E4)
internal val surfaceVariantMiku = Color(0xFF3B494A)
internal val onSurfaceVariantMiku = Color(0xFFBAC9CA)
internal val outlineMiku = Color(0xFF849494)
internal val outlineVariantMiku = Color(0xFF3B494A)
internal val scrimMiku = Color(0xFF000000)
internal val inverseSurfaceMiku = Color(0xFFDCE4E4)
internal val inverseOnSurfaceMiku = Color(0xFF2A3232)
internal val inversePrimaryMiku = Color(0xFF00696E)
internal val surfaceDimMiku = Color(0xFF0D1515)
internal val surfaceBrightMiku = Color(0xFF333B3B)
internal val surfaceContainerLowestMiku = Color(0xFF080F10)
internal val surfaceContainerLowMiku = Color(0xFF161D1D)
internal val surfaceContainerMiku = Color(0xFF1A2121)
internal val surfaceContainerHighMiku = Color(0xFF242B2C)
internal val surfaceContainerHighestMiku = Color(0xFF2F3636)
