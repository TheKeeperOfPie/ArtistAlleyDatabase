package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallySummary
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia

@Composable
fun StampRallySummaryRow(
    stampRally: StampRallySummary,
    seriesById: () -> Map<String, SeriesInfo>,
    modifier: Modifier = Modifier,
) = StampRallySummaryRow(
    stampRallyId = stampRally.id,
    fandom = stampRally.fandom,
    hostTable = stampRally.hostTable,
    series = stampRally.series,
    seriesById = seriesById,
    modifier = modifier,
)

@Composable
fun StampRallySummaryRow(
    stampRallyId: String,
    fandom: String?,
    hostTable: String?,
    series: List<String>,
    seriesById: () -> Map<String, SeriesInfo>,
    modifier: Modifier = Modifier,
) {
    @OptIn(ExperimentalFlexBoxApi::class)
    FlexBox(
        config = FlexBoxConfig {
            wrap(FlexWrap.Wrap)
            columnGap(12.dp)
            alignItems(FlexAlignItems.Center)
        },
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Text(
            text = hostTable?.ifEmpty { null } ?: "   ",
            style = MaterialTheme.typography.titleLarge
                .copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        val text = fandom?.takeUnless { it.isBlank() } ?: stampRallyId
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        if (series.isNotEmpty()) {
            val seriesById = seriesById()
            val series = remember(series) {
                series.map { seriesById[it] ?: SeriesInfo.fake(it) }
            }
            series.forEach {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(it.name(LocalLanguageOptionMedia.current))
                    },
                )
            }
        }
    }
}
