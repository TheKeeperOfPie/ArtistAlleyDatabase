package com.thekeeperofpie.artistalleydatabase.alley.edit

import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.edit.admin.AdminViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistAddViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistHistoryViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistListViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormHistoryViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormMergeViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormQueueViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.merch.MerchEditViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.merch.MerchListViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.merch.MerchResolutionViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesEditViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesListViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesResolutionViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagResolutionViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow

@SingleIn(AppScope::class)
interface ArtistAlleyEditGraph : ArtistAlleyGraph {

    val appFileSystem: AppFileSystem
    val adminViewModelFactory: AdminViewModel.Factory
    val artistAddViewModelFactory: ArtistAddViewModel.Factory
    val artistEditViewModelFactory: ArtistEditViewModel.Factory
    val artistFormHistoryViewModelFactory: ArtistFormHistoryViewModel.Factory
    val artistFormMergeViewModelFactory: ArtistFormMergeViewModel.Factory
    val artistFormQueueViewModelFactory: ArtistFormQueueViewModel.Factory
    val artistHistoryViewModelFactory: ArtistHistoryViewModel.Factory
    val artistListViewModelFactory: ArtistListViewModel.Factory
    val imagesEditViewModelFactory: ImagesEditViewModel.Factory
    val merchEditViewModelFactory: MerchEditViewModel.Factory
    val merchListViewModelFactory: MerchListViewModel.Factory
    val seriesEditViewModelFactory: SeriesEditViewModel.Factory
    val seriesListViewModelFactory: SeriesListViewModel.Factory
    val tagResolutionViewModelFactory: TagResolutionViewModel.Factory
    val seriesResolutionViewModelFactory: SeriesResolutionViewModel.Factory
    val merchResolutionViewModelFactory: MerchResolutionViewModel.Factory

    @Provides
    fun provideHttpClient(networkClient: NetworkClient): HttpClient = networkClient.httpClient

    @Provides
    fun provideArtistAlleySettings(): ArtistAlleySettings = object : ArtistAlleySettings {
        override val appTheme = MutableStateFlow(AppThemeSetting.AUTO)
        override val lastKnownArtistsCsvSize = MutableStateFlow(-1L)
        override val lastKnownStampRalliesCsvSize = MutableStateFlow(-1L)
        override val displayType = MutableStateFlow(SearchScreen.DisplayType.CARD)
        override val artistsSortOption = MutableStateFlow(ArtistSearchSortOption.RANDOM)
        override val artistsSortAscending = MutableStateFlow(true)
        override val stampRalliesSortOption = MutableStateFlow(StampRallySearchSortOption.RANDOM)
        override val stampRalliesSortAscending = MutableStateFlow(true)
        override val seriesSortOption = MutableStateFlow(SeriesSearchSortOption.RANDOM)
        override val seriesSortAscending = MutableStateFlow(true)
        override val showGridByDefault = MutableStateFlow(false)
        override val showRandomCatalogImage = MutableStateFlow(false)
        override val showOnlyConfirmedTags = MutableStateFlow(false)
        override val showOnlyWithCatalog = MutableStateFlow(false)
        override val forceOneDisplayColumn = MutableStateFlow(false)
        override val dataYear = MutableStateFlow(DataYear.LATEST)
        override val languageOption = MutableStateFlow(AniListLanguageOption.DEFAULT)
        override val showOutdatedCatalogs = MutableStateFlow(false)
    }
}
