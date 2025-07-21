package com.thekeeperofpie.artistalleydatabase.alley.database

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asSnapshot
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils.findName2023
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.DriverFactory
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFile
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFiles
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val EXPECTED_COUNT = 723

class AlleyDataValidationTest {

    /** Check that each catalog folder can be loaded by an artist in the 2023 database */
    @Test
    fun verify2023ArtistCatalogs() = runTest {
        val driver = DriverFactory().createDriver()
        val database = DaoUtils.createAlleySqlDatabase(driver)
        val dao = ArtistEntryDao({ driver }, { database }, Settings())
        val pager = Pager(PagingConfig(100)) {
            dao.searchPagingSource(
                year = DataYear.ANIME_EXPO_2023,
                query = "",
                searchQuery = ArtistSearchQuery(
                    ArtistSortFilterController.FilterParams(
                        sortOption = ArtistSearchSortOption.BOOTH,
                        sortAscending = true,
                        seriesIn = emptySet(),
                        merchIn = emptySet(),
                        commissionsIn = emptySet(),
                        linkTypesIn = emptySet(),
                        showOnlyWithCatalog = false,
                        showOnlyConfirmedTags = false,
                        hideIgnored = false,
                        hideFavorited = false,
                    ),
                    randomSeed = 0,
                )
            )
        }
        val artists = pager.flow.asSnapshot { scrollTo(EXPECTED_COUNT) }
        assertEquals(EXPECTED_COUNT, artists.size)
        val failingImages = ComposeFiles.catalogs2023.files
            .filterIsInstance<ComposeFile.Folder>()
            .filter { folder ->
                artists.none { findName2023(listOf(folder), it.artist.name) != null }
            }

        assertTrue(
            failingImages.isEmpty(),
            "Catalogs without matching artists\n" + failingImages.joinToString(separator = "\n") { it.name },
        )
    }

    class Settings() : ArtistAlleySettings {
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
        override val dataYear = MutableStateFlow(DataYear.ANIME_EXPO_2025)
        override val languageOption = MutableStateFlow(AniListLanguageOption.DEFAULT)
    }
}
