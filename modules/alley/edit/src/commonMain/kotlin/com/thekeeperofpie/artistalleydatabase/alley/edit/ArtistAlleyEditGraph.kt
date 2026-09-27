package com.thekeeperofpie.artistalleydatabase.alley.edit

import com.thekeeperofpie.artistalleydatabase.alley.navigation.AlleyNavStack
import com.thekeeperofpie.artistalleydatabase.alley.AlleyRootDestination
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistTag
import com.thekeeperofpie.artistalleydatabase.alley.edit.lastviewed.LastViewedConnection
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.AlleyEditNavStack
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow

@SingleIn(AppScope::class)
interface ArtistAlleyEditGraph : ArtistAlleyGraph {

    val appFileSystem: AppFileSystem
    val lastViewedConnection: LastViewedConnection

    val alleyEditCoilInit: AlleyEditCoilInit

    @Provides
    fun provideHttpClient(networkClient: NetworkClient): HttpClient = networkClient.httpClient

    @Provides
    fun provideArtistAlleySettings(): ArtistAlleySettings = object : ArtistAlleySettings {
        override val appTheme = MutableStateFlow(AppThemeSetting.AUTO)
        override val lastKnownArtistsCsvSize = MutableStateFlow(-1L)
        override val lastKnownStampRalliesCsvSize = MutableStateFlow(-1L)
        override val displayType = MutableStateFlow(SearchDisplayType.CARD)
        override val artistsSortOption = MutableStateFlow(ArtistSearchSortOption.RANDOM)
        override val artistsSortAscending = MutableStateFlow(true)
        override val stampRalliesSortOption = MutableStateFlow(StampRallySearchSortOption.RANDOM)
        override val stampRalliesSortAscending = MutableStateFlow(true)
        override val seriesSortOption = MutableStateFlow(SeriesSearchSortOption.RANDOM)
        override val seriesSortAscending = MutableStateFlow(true)
        override val showGridByDefault = MutableStateFlow(false)
        override val showRandomCatalogImage = MutableStateFlow(false)
        override val showOnlyConfirmedTags = MutableStateFlow(false)
        override val forceOneDisplayColumn = MutableStateFlow(false)
        override val dataYear = MutableStateFlow(DataYear.LATEST)
        override val languageOption = MutableStateFlow(AniListLanguageOption.DEFAULT)
        override val showOutdatedCatalogs = MutableStateFlow(false)
        override val easterEggEnabled = MutableStateFlow(false)

        override val artistTagsIn = MutableStateFlow(emptySet<ArtistTag>())
        override val artistTagsNotIn = MutableStateFlow(emptySet<ArtistTag>())
        override val rootDestination = MutableStateFlow(AlleyRootDestination.ARTISTS)
    }

    val editNavigatorGraphFactory: ArtistAlleyEditNavigatorGraph.Factory
}

@GraphExtension(NavigatorScope::class)
interface ArtistAlleyEditNavigatorGraph : ViewModelGraph {

    @GraphExtension.Factory
    interface Factory {
        fun create(
            @Provides editNavStack: AlleyEditNavStack,
            @Provides alleyNavStack: AlleyNavStack,
        ): ArtistAlleyEditNavigatorGraph
    }
}
