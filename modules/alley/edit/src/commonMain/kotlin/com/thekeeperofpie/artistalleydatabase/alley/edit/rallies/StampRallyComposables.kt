package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .wrapContentWidth(align = Alignment.Start, unbounded = true)
                    .padding(vertical = 8.dp)
            ) {
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
}
