package com.thekeeperofpie.artistalleydatabase.anime.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.BuildConfig
import com.thekeeperofpie.artistalleydatabase.anime.R

abstract class SortFilterController(protected val settings: AnimeSettings) {

    abstract val sections: List<SortFilterSection>
    val state = SortFilterSection.ExpandedState()

    @Suppress("KotlinConstantConditions")
    protected val showAdultSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_generic_filter_show_adult_content,
        settings = settings,
        property = { it.showAdult },
    ).takeIf { BuildConfig.BUILD_TYPE != "release" }

    protected val collapseOnCloseSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_generic_filter_collapse_on_close,
        settings = settings,
        property = { it.collapseAnimeFiltersOnClose },
    )

    @Suppress("KotlinConstantConditions")
    protected val showIgnoredSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_generic_filter_show_ignored,
        settings = settings,
        property = { it.showIgnored },
    ).takeIf { BuildConfig.BUILD_TYPE != "release" }

    // TODO: Actually de-dupe advanced section across controllers
    protected val advancedSection = SortFilterSection.Group(
        titleRes = R.string.anime_generic_filter_advanced_group,
        titleDropdownContentDescriptionRes = R.string.anime_generic_filter_advanced_group_expand_content_description,
        children = listOfNotNull(showAdultSection, collapseOnCloseSection, showIgnoredSection)
    )

    @Composable
    open fun collapseOnClose() = settings.collapseAnimeFiltersOnClose.collectAsState().value

    // TODO: Find a better way to do this
    @Composable
    open fun PromptDialog() {
    }
}
