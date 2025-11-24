package com.thekeeperofpie.artistalleydatabase.alley.series

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_search_clear_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_search_placeholder
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.CustomFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterExpandedState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalCoroutinesApi::class
)
class SeriesAutocompleteSection(
    scope: CoroutineScope,
    dispatchers: CustomDispatchers,
    lockedSeriesEntry: StateFlow<SeriesInfo?>,
    seriesEntryDao: SeriesEntryDao,
    seriesImagesStore: SeriesImagesStore,
    savedStateHandle: SavedStateHandle,
) {

    private var query by mutableStateOf("")
    val seriesIn =
        savedStateHandle.getMutableStateFlow(
            scope = scope,
            json = Json,
            key = "seriesIn",
            initialValue = { emptyList<SeriesFilterEntry>() },
        )


    private val results = snapshotFlow { query }
        .debounce(500.milliseconds)
        .mapLatest(seriesEntryDao::searchSeriesForAutocomplete)
        .flowOn(dispatchers.io)
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val imageLoader = SeriesImageLoader(dispatchers, scope, seriesImagesStore)
    val section = object : SortFilterSectionState.Custom("series") {
        override fun clear() {
            seriesIn.value = emptyList()
        }

        @Composable
        override fun isDefault(): Boolean {
            val seriesIn by seriesIn.collectAsStateWithLifecycle()
            return remember { derivedStateOf { seriesIn.isEmpty() } }.value
        }

        @Composable
        override fun Content(
            state: SortFilterExpandedState,
            showDivider: Boolean,
        ) {
            val expanded = state.expandedState[id] == true
            CustomFilterSection(
                expanded = expanded,
                onExpandedChange = { state.expandedState[id] = it },
                titleRes = Res.string.alley_series_filter_label,
                titleDropdownContentDescriptionRes = Res.string.alley_series_filter_content_description,
                showDivider = showDivider,
            ) {
                Column(modifier = Modifier.animateContentSize()) {
                    if (expanded) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = it },
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            val interactionSource = remember { MutableInteractionSource() }
                            TextField(
                                value = query,
                                placeholder = {
                                    Text(text = stringResource(Res.string.alley_series_filter_search_placeholder))
                                },
                                trailingIcon = {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = stringResource(
                                                Res.string.alley_series_filter_search_clear_content_description
                                            ),
                                        )
                                    }
                                },
                                onValueChange = { query = it },
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

                            val results by results.collectAsStateWithLifecycle()
                            ExposedDropdownMenu(
                                expanded = focused && results.isNotEmpty(),
                                onDismissRequest = {
                                    // This callback is invoked whenever the query changes,
                                    // which makes it unusable if the user is typing
                                    if (!isImeVisible) {
                                        focusManager.clearFocus()
                                    }
                                },
                            ) {
                                var seriesIn by seriesIn.collectAsMutableStateWithLifecycle()
                                results.forEach {
                                    DropdownMenuItem(
                                        onClick = {
                                            seriesIn += SeriesFilterEntry(it)
                                            focusManager.clearFocus(true)
                                        },
                                        text = {
                                            SeriesRow(
                                                series = it,
                                                image = { imageLoader.getSeriesImage(it) },
                                            )
                                        },
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
                        val lockedSeries by lockedSeriesEntry.collectAsStateWithLifecycle()
                        lockedSeries?.let {
                            FilterChip(
                                selected = true,
                                enabled = false,
                                label = { AutoHeightText(it.name(LocalLanguageOptionMedia.current)) },
                                onClick = {},
                                modifier = Modifier.animateContentSize()
                            )
                        }
                        var seriesIn by seriesIn.collectAsMutableStateWithLifecycle()
                        seriesIn.forEach {
                            FilterChip(
                                selected = true,
                                enabled = true,
                                label = { AutoHeightText(it.name(LocalLanguageOptionMedia.current)) },
                                onClick = { seriesIn -= it },
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }
                }
            }
        }
    }

    @Serializable
    data class SeriesFilterEntry(
        val id: String,
        val titlePreferred: String,
        val titleEnglish: String,
        val titleRomaji: String,
        val titleNative: String,
    ) {
        constructor(entry: SeriesInfo) : this(
            id = entry.id,
            titlePreferred = entry.titlePreferred,
            titleEnglish = entry.titleEnglish,
            titleRomaji = entry.titleRomaji,
            titleNative = entry.titleNative,
        )

        fun name(languageOption: AniListLanguageOption) = when (languageOption) {
            AniListLanguageOption.DEFAULT -> titlePreferred
            AniListLanguageOption.ENGLISH -> titleEnglish
            AniListLanguageOption.NATIVE -> titleNative
            AniListLanguageOption.ROMAJI -> titleRomaji
        }
    }
}
