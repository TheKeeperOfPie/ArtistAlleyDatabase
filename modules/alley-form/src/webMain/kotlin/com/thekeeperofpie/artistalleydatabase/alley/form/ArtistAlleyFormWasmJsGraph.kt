package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUploader
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow

@SingleIn(AppScope::class)
@DependencyGraph
internal interface ArtistAlleyFormWasmJsGraph : ArtistAlleyFormGraph {

    @Provides
    fun provideHttpClient(networkClient: NetworkClient): HttpClient = networkClient.httpClient

    @Binds
    val FormImageUploader.bindImageUploader: ImageUploader

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

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyFormWasmJsGraph
    }
}
