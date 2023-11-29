package com.thekeeperofpie.artistalleydatabase.anime.filter

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.OnChangeEffect
import com.thekeeperofpie.artistalleydatabase.compose.debounce
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration.Companion.milliseconds

abstract class SortFilterController<FilterParams>(
    scope: CoroutineScope,
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

    // Lazy is required here because the subclass fields are not initialized until after this
    val filterParams by lazy {
        scope.launchMolecule(RecompositionMode.Immediate) {
            debounce(currentValue = filterParams(), duration = 500.milliseconds)
        }
    }

    @Composable
    abstract fun filterParams(): FilterParams

    @Composable
    open fun collapseOnClose() = settings.collapseAnimeFiltersOnClose.collectAsState().value

    // TODO: Find a better way to do this
    @Composable
    open fun PromptDialog() {
    }

    @Composable
    fun ImmediateScrollResetEffect(lazyGridState: LazyGridState) {
        OnChangeEffect(currentValue = filterParams.collectAsState().value) {
            lazyGridState.scrollToItem(0)
        }
    }

    @Composable
    fun ImmediateScrollResetEffect(lazyListState: LazyListState) {
        OnChangeEffect(currentValue = filterParams.collectAsState().value) {
            lazyListState.scrollToItem(0)
        }
    }
}
