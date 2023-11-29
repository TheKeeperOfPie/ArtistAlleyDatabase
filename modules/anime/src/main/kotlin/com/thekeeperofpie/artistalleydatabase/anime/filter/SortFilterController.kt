package com.thekeeperofpie.artistalleydatabase.anime.filter

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

abstract class SortFilterController<FilterParams>(
    protected val settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) {
    abstract val sections: List<SortFilterSection>
    val state = SortFilterSection.ExpandedState()

    protected val showAdultSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_generic_filter_show_adult_content,
        settings = settings,
        property = { it.showAdult },
    ).takeUnless { featureOverrideProvider.isReleaseBuild }

    protected val collapseOnCloseSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_generic_filter_collapse_on_close,
        settings = settings,
        property = { it.collapseAnimeFiltersOnClose },
    )

    protected val hideIgnoredSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_media_filter_hide_ignored,
        settings = settings,
        property = { it.mediaIgnoreHide },
    )

    // TODO: Actually de-dupe advanced section across controllers
    protected val advancedSection = SortFilterSection.Group<SortFilterSection>(
        titleRes = R.string.anime_generic_filter_advanced_group,
        titleDropdownContentDescriptionRes = R.string.anime_generic_filter_advanced_group_expand_content_description,
        children = listOfNotNull(showAdultSection, collapseOnCloseSection, hideIgnoredSection)
    )

    abstract val filterParams: Flow<FilterParams>
    open val filterParamsStateFlow: StateFlow<FilterParams>? = null

    @Composable
    open fun collapseOnClose() = settings.collapseAnimeFiltersOnClose.collectAsState().value

    // TODO: Find a better way to do this
    @Composable
    open fun PromptDialog() {
    }

    @Composable
    fun ImmediateScrollResetEffect(lazyGridState: LazyGridState) {
        val flow = filterParamsStateFlow
        if (flow != null) {
            val currentValue = flow.collectAsState().value
            var previousValue by remember { mutableStateOf(currentValue) }
            LaunchedEffect(currentValue) {
                if (previousValue != currentValue) {
                    previousValue = currentValue
                    lazyGridState.scrollToItem(0)
                }
            }
        }
    }

    @Composable
    fun ImmediateScrollResetEffect(lazyListState: LazyListState) {
        val flow = filterParamsStateFlow
        if (flow != null) {
            val currentValue = flow.collectAsState().value
            var previousValue by remember { mutableStateOf(currentValue) }
            LaunchedEffect(currentValue) {
                if (previousValue != currentValue) {
                    previousValue = currentValue
                    lazyListState.scrollToItem(0)
                }
            }
        }
    }
}
