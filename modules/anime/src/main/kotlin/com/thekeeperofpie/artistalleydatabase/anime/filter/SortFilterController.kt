package com.thekeeperofpie.artistalleydatabase.anime.filter

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import kotlinx.coroutines.flow.Flow

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

    abstract fun filterParams() : Flow<FilterParams>

    @Composable
    open fun collapseOnClose() = settings.collapseAnimeFiltersOnClose.collectAsState().value

    // TODO: Find a better way to do this
    @Composable
    open fun PromptDialog() {
    }

    @Composable
    fun AttachResetScroll(lazyListState: LazyListState) {
        // TODO: Disabled as this breaks returning from details screen
//        LaunchedEffect(filterParams().collectAsState(null).value) {
//            lazyListState.scrollToItem(0)
//        }
    }

    @Composable
    fun AttachResetScroll(lazyGridState: LazyGridState) {
//        LaunchedEffect(filterParams().collectAsState(null).value) {
//            lazyGridState.scrollToItem(0)
//        }
    }
}
