package com.thekeeperofpie.artistalleydatabase.cds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.cds.generated.resources.Res
import artistalleydatabase.modules.cds.generated.resources.cd_section_title
import coil3.size.Dimension
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

fun LazyGridScope.cdsSection(
    cdEntries: List<CdEntryGridModel>,
    onClickEntry: (index: Int, entry: CdEntryGridModel) -> Unit = { _, _ -> },
) {
    if (cdEntries.isEmpty()) return

    item(key = "cdsHeader", span = GridUtils.maxSpanFunction, contentType = "cdsHeader") {
        DetailsSectionHeader(
            text = stringResource(Res.string.cd_section_title),
            modifier = Modifier.animateItem()
        )
    }

    item(key = "cdsSection", span = GridUtils.maxSpanFunction, contentType = "cdsSection") {
        val width = LocalDensity.current.run { Dimension.Pixels(200.dp.toPx().roundToInt()) }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.animateItem(),
        ) {
            itemsIndexed(cdEntries) { index, cdEntry ->
                // TODO: Animate corner size during transition
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                ) {
                    EntryGrid.Entry(
                        expectedWidth = width,
                        index = index,
                        entry = cdEntry,
                        onClickEntry = onClickEntry,
                    )
                }
            }
        }
    }
}
