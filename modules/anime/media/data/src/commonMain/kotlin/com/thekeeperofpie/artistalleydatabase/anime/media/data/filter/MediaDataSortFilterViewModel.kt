package com.thekeeperofpie.artistalleydatabase.anime.media.data.filter

import androidx.lifecycle.ViewModel
import artistalleydatabase.modules.anime.media.data.generated.resources.Res
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_generic_filter_advanced_group
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_generic_filter_advanced_group_expand_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_generic_filter_collapse_on_close
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_generic_filter_show_adult_content
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_hide_ignored
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import kotlinx.coroutines.flow.MutableStateFlow

abstract class MediaDataSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    settings: MediaDataSettings,
    showHideIgnored: Boolean = true,
) : ViewModel() {

    protected val showAdultSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.anime_generic_filter_show_adult_content,
        property = settings.showAdult,
    ).takeUnless { featureOverrideProvider.isReleaseBuild }

    protected val collapseOnCloseSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.anime_generic_filter_collapse_on_close,
        property = settings.collapseAnimeFiltersOnClose,
    )

    protected val hideIgnoredSection = if (!showHideIgnored) null else {
        SortFilterSectionState.SwitchBySetting(
            title = Res.string.anime_media_filter_hide_ignored,
            property = settings.mediaIgnoreHide,
        )
    }

    // TODO: Actually de-dupe advanced section across controllers
    protected fun makeAdvancedSection(vararg additionalSections: SortFilterSectionState) =
        SortFilterSectionState.Group(
            title = Res.string.anime_generic_filter_advanced_group,
            titleDropdownContentDescription = Res.string.anime_generic_filter_advanced_group_expand_content_description,
            children = MutableStateFlow(
                listOfNotNull(
                    showAdultSection,
                    collapseOnCloseSection,
                    hideIgnoredSection,
                    *additionalSections,
                )
            )
        )
}
