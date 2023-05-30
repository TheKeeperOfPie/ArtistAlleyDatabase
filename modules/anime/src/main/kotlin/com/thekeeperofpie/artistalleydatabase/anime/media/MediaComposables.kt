package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState

fun <T> LazyListScope.mediaListSection(
    screenKey: String,
    @StringRes titleRes: Int,
    values: Collection<T>,
    valueToEntry: (T) -> AnimeMediaListRow.Entry,
    aboveFold: Int,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    colorCalculationState: ColorCalculationState,
    navigationCallback: AnimeNavigator.NavigationCallback,
    onTagLongClick: (String) -> Unit,
    label: (@Composable (T) -> Unit)? = null,
) = listSection(
    titleRes = titleRes,
    values = values,
    aboveFold = aboveFold,
    expanded = expanded,
    onExpandedChange = onExpandedChange,
) { item, paddingBottom ->
    val entry = valueToEntry(item)
    AnimeMediaListRow(
        screenKey = screenKey,
        entry = entry,
        label = if (label == null) null else {
            { label(item) }
        },
        onTagLongClick = onTagLongClick,
        colorCalculationState = colorCalculationState,
        navigationCallback = navigationCallback,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
    )
}
