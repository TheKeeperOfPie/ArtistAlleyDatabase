package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.platform.Font
import artistalleydatabase.modules.alley.generated.resources.Res
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import kotlinx.coroutines.flow.first

@Composable
fun VariableFontEffect(
    seriesEntryCache: SeriesEntryCache,
    onLoaded: (FontFamily.Resolver) -> Unit,
) {
    val updatedOnLoaded by rememberUpdatedState(onLoaded)
    LaunchedEffect(Unit) {
        try {
            // Wait for the series cache to fill as a proxy for the database being available,
            // to avoid using network for low priority unnecessary font files
            seriesEntryCache.series.first { it.isNotEmpty() }
            val fonts = listOf(
                "NotoSansJP.ttf--611190",
                "NotoSansKR.ttf--1802257382",
                "NotoSansSC.ttf--1638299189",
            ).map {
                Font(it, Res.readBytes("font/$it"))
            }
            val fontFamilyResolver = createFontFamilyResolver()
            fontFamilyResolver.preload(FontFamily(fonts))
            updatedOnLoaded(fontFamilyResolver)
        } catch (_: Throwable) {
        }
    }
}
