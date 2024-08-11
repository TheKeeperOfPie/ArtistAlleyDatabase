package com.thekeeperofpie.artistalleydatabase.anime.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.FeatureOverrideProvider
import kotlinx.coroutines.CoroutineScope

abstract class AnimeSettingsSortFilterController<FilterParams>(
    scope: CoroutineScope,
    protected val settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : SortFilterController<FilterParams>(scope) {
    protected val showAdultSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_generic_filter_show_adult_content,
        property = settings.showAdult,
    ).takeUnless { featureOverrideProvider.isReleaseBuild }

    protected val collapseOnCloseSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_generic_filter_collapse_on_close,
        property = settings.collapseAnimeFiltersOnClose,
    )

    protected val hideIgnoredSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_media_filter_hide_ignored,
        property = settings.mediaIgnoreHide,
    )

    // TODO: Actually de-dupe advanced section across controllers
    protected val advancedSection = SortFilterSection.Group<SortFilterSection>(
        titleRes = R.string.anime_generic_filter_advanced_group,
        titleDropdownContentDescriptionRes = R.string.anime_generic_filter_advanced_group_expand_content_description,
        children = listOfNotNull(showAdultSection, collapseOnCloseSection, hideIgnoredSection)
    )

    @Composable
    open fun collapseOnClose() = settings.collapseAnimeFiltersOnClose.collectAsState().value
}
