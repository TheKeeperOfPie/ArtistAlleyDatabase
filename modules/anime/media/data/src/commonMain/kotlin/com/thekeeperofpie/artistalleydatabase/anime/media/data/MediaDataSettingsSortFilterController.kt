package com.thekeeperofpie.artistalleydatabase.anime.media.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import artistalleydatabase.modules.anime.media.data.generated.resources.Res
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_generic_filter_advanced_group
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_generic_filter_advanced_group_expand_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_generic_filter_collapse_on_close
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_generic_filter_show_adult_content
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_hide_ignored
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope

abstract class MediaDataSettingsSortFilterController<FilterParams : Any>(
    scope: CoroutineScope,
    protected val settings: MediaDataSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : SortFilterController<FilterParams>(scope) {
    protected val showAdultSection = SortFilterSection.SwitchBySetting(
        titleRes = Res.string.anime_generic_filter_show_adult_content,
        property = settings.showAdult,
    ).takeUnless { featureOverrideProvider.isReleaseBuild }

    protected val collapseOnCloseSection = SortFilterSection.SwitchBySetting(
        titleRes = Res.string.anime_generic_filter_collapse_on_close,
        property = settings.collapseAnimeFiltersOnClose,
    )

    protected val hideIgnoredSection = SortFilterSection.SwitchBySetting(
        titleRes = Res.string.anime_media_filter_hide_ignored,
        property = settings.mediaIgnoreHide,
    )

    // TODO: Actually de-dupe advanced section across controllers
    protected val advancedSection = SortFilterSection.Group<SortFilterSection>(
        titleRes = Res.string.anime_generic_filter_advanced_group,
        titleDropdownContentDescriptionRes = Res.string.anime_generic_filter_advanced_group_expand_content_description,
        children = listOfNotNull(showAdultSection, collapseOnCloseSection, hideIgnoredSection)
    )

    @Composable
    override fun collapseOnClose() = settings.collapseAnimeFiltersOnClose.collectAsState().value
}
