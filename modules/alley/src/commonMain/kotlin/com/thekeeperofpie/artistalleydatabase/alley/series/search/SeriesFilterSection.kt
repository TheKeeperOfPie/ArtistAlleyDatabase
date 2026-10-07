package com.thekeeperofpie.artistalleydatabase.alley.series.search

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_search_clear_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_search_placeholder
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.series.ui.SeriesRow
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Clear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.CustomFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.TextFieldStateSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Serializable
class SeriesFilterState @RememberInComposition constructor(
    @Serializable(TextFieldStateSerializer::class)
    val query: TextFieldState = TextFieldState(),
    @Serializable(SnapshotStateListSerializer::class)
    val seriesIn: SnapshotStateList<SeriesFilterEntry> = mutableStateListOf(),
) {
    val isDefault by derivedStateOf { seriesIn.isEmpty() }

    fun clear() {
        Snapshot.withMutableSnapshot {
            query.clearText()
            seriesIn.clear()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun SeriesFilterSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    state: SeriesFilterState,
    lockedSeries: () -> SeriesInfo?,
    autocompleteResults: () -> List<SeriesInfo>,
    showOnlyConfirmedTagsSection: (@Composable () -> Unit)? = null,
) {
    val expanded = expanded()
    CustomFilterSection(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        header = { Text(stringResource(Res.string.alley_series_filter_label)) },
        headerDropdownContentDescriptionRes = Res.string.alley_series_filter_content_description,
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            if (expanded) {
                if (showOnlyConfirmedTagsSection != null) {
                    showOnlyConfirmedTagsSection()
                }
                var dropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    TextField(
                        state = state.query,
                        placeholder = {
                            Text(text = stringResource(Res.string.alley_series_filter_search_placeholder))
                        },
                        trailingIcon = {
                            IconButton(onClick = { state.query.clearText() }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(
                                        Res.string.alley_series_filter_search_clear_content_description
                                    ),
                                )
                            }
                        },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 16.dp)
                    )

                    val focused by interactionSource.collectIsFocusedAsState()
                    val focusManager = LocalFocusManager.current
                    val isImeVisible = WindowInsets.isImeVisibleKmp
                    BackHandler(enabled = focused && !isImeVisible) {
                        focusManager.clearFocus()
                    }

                    val autocompleteResults = autocompleteResults()
                    ExposedDropdownMenu(
                        expanded = focused && autocompleteResults.isNotEmpty(),
                        onDismissRequest = {
                            // This callback is invoked whenever the query changes,
                            // which makes it unusable if the user is typing
                            if (!isImeVisible) {
                                focusManager.clearFocus()
                            }
                        },
                    ) {
                        autocompleteResults.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    state.seriesIn += SeriesFilterEntry(it)
                                    focusManager.clearFocus(true)
                                },
                                text = { SeriesRow(series = it) },
                                contentPadding = PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 4.dp,
                                ),
                            )
                        }
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 16.dp)
                    .animateContentSize(),
            ) {
                lockedSeries()?.let {
                    FilterChip(
                        selected = true,
                        enabled = false,
                        label = { AutoHeightText(it.name(LocalLanguageOptionMedia.current)) },
                        onClick = {},
                        modifier = Modifier.animateContentSize()
                    )
                }
                state.seriesIn.forEach {
                    FilterChip(
                        selected = true,
                        enabled = true,
                        label = { AutoHeightText(it.name(LocalLanguageOptionMedia.current)) },
                        onClick = { state.seriesIn -= it },
                        modifier = Modifier.animateContentSize()
                    )
                }
            }
        }
    }
}
