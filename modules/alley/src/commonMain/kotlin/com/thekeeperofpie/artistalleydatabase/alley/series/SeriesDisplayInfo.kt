package com.thekeeperofpie.artistalleydatabase.alley.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia

data class SeriesDisplayInfo(
    val id: String,
    val title: String,
)

@Composable
fun rememberSeriesDisplayInfo(series: List<SeriesWithUserData>): List<SeriesDisplayInfo> {
    val languageOptionMedia = LocalLanguageOptionMedia.current
    return remember(series, languageOptionMedia) {
        series.map { SeriesDisplayInfo(it.series.id, it.series.name(languageOptionMedia)) }
            .sortedWith { first, second ->
                String.CASE_INSENSITIVE_ORDER.compare(first.title, second.title)
            }
    }
}
