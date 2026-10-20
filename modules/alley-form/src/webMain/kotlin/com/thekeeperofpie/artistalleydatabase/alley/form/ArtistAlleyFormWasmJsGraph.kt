package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.alley.AlleyRootDestination
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistTag
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUploader
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
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
@DependencyGraph(AppScope::class)
internal interface ArtistAlleyFormWasmJsGraph : ArtistAlleyFormGraph {

    @Binds
    val FormImageUploader.bindImageUploader: ImageUploader

    @Provides
    fun provideHttpClient(networkClient: NetworkClient): HttpClient = networkClient.httpClient

    @Provides
    fun provideArtistAlleySettings(): ArtistAlleySettings = object : ArtistAlleySettings {
        override var appTheme by mutableStateOf(AppThemeSetting.AUTO)
        override var displayType by mutableStateOf(SearchDisplayType.CARD)

        override var artistsSortOption by mutableStateOf(ArtistSearchSortOption.RANDOM)
        override var artistsSortAscending by mutableStateOf(true)
        override var artistsTagsIn by mutableStateOf(emptySet<ArtistTag>())
        override var artistsTagsNotIn by mutableStateOf(emptySet<ArtistTag>())
        override var showOutdatedCatalogs by mutableStateOf(false)

        override var stampRalliesSortOption by mutableStateOf(StampRallySearchSortOption.RANDOM)
        override var stampRalliesSortAscending by mutableStateOf(true)

        override var showGridByDefault by mutableStateOf(false)
        override var showRandomCatalogImage by mutableStateOf(false)
        override var showOnlyConfirmedTags by mutableStateOf(false)
        override var forceOneDisplayColumn by mutableStateOf(false)

        override var seriesSortOption by mutableStateOf(SeriesSearchSortOption.RANDOM)
        override var seriesSortAscending by mutableStateOf(true)

        override var dataYear by mutableStateOf(DataYear.LATEST)
        override val languageOption = MutableStateFlow(AniListLanguageOption.DEFAULT)
        override val easterEggEnabled = MutableStateFlow(false)

        override val rootDestination = MutableStateFlow(AlleyRootDestination.ARTISTS)
    }

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyFormWasmJsGraph
    }
}
