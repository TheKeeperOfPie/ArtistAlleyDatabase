@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.cds

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import coil.size.Dimension
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import kotlin.math.roundToInt

fun LazyListScope.cdsSection(
    screenKey: String,
    cdEntries: List<CdEntryGridModel>,
    onClickEntry: (index: Int, entry: CdEntryGridModel) -> Unit = { _, _ -> },
) {
    if (cdEntries.isEmpty()) return

    item("cdsHeader") {
        DetailsSectionHeader(
            stringResource(R.string.cd_section_title),
            modifier = Modifier.animateItemPlacement()
        )
    }

    item("cdsSection") {
        val width = LocalDensity.current.run { Dimension.Pixels(200.dp.toPx().roundToInt()) }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.animateItemPlacement(),
        ) {
            itemsIndexed(cdEntries) { index, cdEntry ->
                var transitionProgress by remember { mutableFloatStateOf(0f) }
                val cornerDp = lerp(12.dp, 0.dp, transitionProgress)
                ElevatedCard(
                    shape = RoundedCornerShape(cornerDp),
                ) {
                    EntryGrid.Entry(
                        imageScreenKey = screenKey,
                        expectedWidth = width,
                        index = index,
                        entry = cdEntry,
                        onClickEntry = onClickEntry,
//                        onClickEntry = { _, entry ->
//                            navigationCallback.onCdEntryClick(
//                                model = entry,
//                                imageCornerDp = 12.dp,
//                            )
//                        },
                        onSharedElementFractionChanged = { transitionProgress = it }
                    )
                }
            }
        }
    }
}
