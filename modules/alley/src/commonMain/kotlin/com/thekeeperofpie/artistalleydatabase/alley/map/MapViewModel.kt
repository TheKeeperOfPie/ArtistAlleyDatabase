package com.thekeeperofpie.artistalleydatabase.alley.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.BoothWithFavorite
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random

@AssistedInject
class MapViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val seriesEntryCache: SeriesEntryCache,
    private val userEntryDao: UserEntryDao,
    private val settings: ArtistAlleySettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var gridData by mutableStateOf(LoadingResult.loading<GridData>())
    private val randomSeed =
        savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }
    private val mutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            // TODO: This is very inefficient to respond to favorites updates
            userEntryDao.getBoothsWithFavorites()
                .map { (dataYear, booths) ->
                    when (dataYear) {
                        DataYear.ANIME_EXPO_2023,
                        DataYear.ANIME_EXPO_2024,
                        DataYear.ANIME_EXPO_2025,
                        DataYear.ANIME_EXPO_2026,
                            -> mapBooths(booths)
                        DataYear.ANIME_NYC_2024 -> mapAnimeNyc2024Booths(booths)
                        DataYear.ANIME_NYC_2025 -> mapAnimeNyc2025Booths(booths)
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { tables ->
                    gridData = LoadingResult.success(
                        GridData(
                            maxX = tables.maxOfOrNull { it.gridX } ?: 0,
                            maxY = tables.maxOfOrNull { it.gridY } ?: 0,
                            tables = tables,
                        )
                    )
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                userEntryDao.insertArtistUserEntry(it)
            }
        }
    }

    private fun mapBooths(booths: List<BoothWithFavorite>): List<Table> {
        @Suppress("UNCHECKED_CAST")
        val letterToBooths = (booths.groupBy { it.booth?.take(1) }
            .filterKeys { it != null } as Map<String, List<BoothWithFavorite>>)
            .toList()
            .sortedBy { it.first }
        var currentIndex = 0
        val showRandomCatalogImage = settings.showRandomCatalogImage.value
        return letterToBooths.mapIndexed { letterIndex, pair ->
            pair.second.groupBy { it.booth }
                .mapNotNull { (booth, artists) ->
                    booth?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    val tableNumber = booth.filter { it.isDigit() }.toInt()
                    val artist = artists.singleOrNull()
                    if (artist != null) {
                        val images = AlleyImageUtils.getArtistImages(
                            year = artist.year,
                            images = artist.images,
                        )
                        val imageIndex = if (showRandomCatalogImage) {
                            images.indices.randomOrNull()
                        } else {
                            0
                        }
                        Table.Single(
                            year = artist.year,
                            artistId = artist.id,
                            booth = booth,
                            section = Table.AnimeExpoSection.fromTableNumber(tableNumber),
                            image = imageIndex?.let(images::getOrNull),
                            imageIndex = imageIndex,
                            favorite = artist.favorite,
                            gridX = currentIndex,
                            // There's a physical gap not accounted for in the numbers between 41 and 42
                            gridY = if (tableNumber >= 42) tableNumber + 1 else tableNumber,
                        )
                    } else {
                        val primaryArtist = artists.first()
                        val images = AlleyImageUtils.getArtistImages(
                            year = primaryArtist.year,
                            images = primaryArtist.images,
                        )
                        val imageIndex = if (showRandomCatalogImage) {
                            images.indices.randomOrNull()
                        } else {
                            0
                        }
                        Table.Shared(
                            year = primaryArtist.year,
                            artistIds = artists.map { it.id },
                            booth = booth,
                            section = Table.AnimeExpoSection.fromTableNumber(tableNumber),
                            image = imageIndex?.let(images::getOrNull),
                            imageIndex = imageIndex,
                            favorite = artists.any { it.favorite },
                            gridX = currentIndex,
                            // There's a physical gap not accounted for in the numbers between 41 and 42
                            gridY = if (tableNumber >= 42) tableNumber + 1 else tableNumber,
                        )
                    }
                }.also {
                    currentIndex++
                    if (letterIndex == 11) {
                        // There's a large gap between the two halves of the map
                        currentIndex += 4
                    } else if (letterIndex % 2 == 0) {
                        // Skip an extra between every 2 tables
                        currentIndex++
                    }
                }
        }.flatten()
    }

    private fun animeNyc2024IndexX(letter: Char, booth: Int): Int {
        val isEven = (booth % 2) == 0
        if (booth <= 4) {
            // These are irregular enough that they're just hardcoded
            return when (letter) {
                'A' -> -1 // This is okay because there are no odd As so this becomes 0 further down
                'B' -> 1
                'C' -> 5
                'D' -> 8
                'E' -> if (isEven) 11 else 10
                'F' -> 14
                'G' -> 16
                'H' -> 20
                'J' -> 25
                'K' -> 29
                'L' -> 31
                'M' -> 35
                'O' -> 40
                'P' -> 44
                'Q' -> 46
                'R' -> 50
                'T' -> 55
                'U' -> 59
                'V' -> 61
                else -> 0
            } + (if (isEven) 1 else 0)
        }

        val offset = when {
            booth <= 4 -> if (isEven) 0 else -1
            isEven -> 1
            else -> -1
        }
        return (letter - 'A') * 3 + offset - 1
    }

    private fun mapAnimeNyc2024Booths(booths: List<BoothWithFavorite>): List<Table> {
        @Suppress("UNCHECKED_CAST")
        val letterToBooths = (booths.groupBy { it.booth?.take(1) }
            .filterKeys { it != null } as Map<String, List<BoothWithFavorite>>)
            .toList()
            .sortedBy { it.first }
        val showRandomCatalogImage = settings.showRandomCatalogImage.value
        return letterToBooths.mapIndexed { letterIndex, pair ->
            pair.second.groupBy { it.booth }
                .mapNotNull { (booth, artists) ->
                    booth ?: return@mapNotNull null
                    val tableNumber = booth.filter { it.isDigit() }.toInt()
                    val letter = booth[0]
                    val gridX = animeNyc2024IndexX(letter, tableNumber)
                    val gridY = (tableNumber - 1) / 2 + (if (tableNumber > 4) 1 else 0) + 1
                    val artist = artists.singleOrNull()
                    if (artist != null) {
                        val images = AlleyImageUtils.getArtistImages(
                            year = artist.year,
                            images = artist.images,
                        )
                        val imageIndex = if (showRandomCatalogImage) {
                            images.indices.randomOrNull()
                        } else {
                            0
                        }
                        Table.Single(
                            year = artist.year,
                            artistId = artist.id,
                            booth = booth,
                            section = null,
                            image = imageIndex?.let(images::getOrNull),
                            imageIndex = imageIndex,
                            favorite = artist.favorite,
                            gridX = gridX,
                            gridY = gridY,
                        )
                    } else {
                        val primaryArtist = artists.first()
                        val images = AlleyImageUtils.getArtistImages(
                            year = primaryArtist.year,
                            images = primaryArtist.images,
                        )
                        val imageIndex = if (showRandomCatalogImage) {
                            images.indices.randomOrNull()
                        } else {
                            0
                        }
                        Table.Shared(
                            year = primaryArtist.year,
                            artistIds = artists.map { it.id },
                            booth = booth,
                            section = null,
                            image = imageIndex?.let(images::getOrNull),
                            imageIndex = imageIndex,
                            favorite = artists.any { it.favorite },
                            gridX = gridX,
                            gridY = gridY,
                        )
                    }
                }
        }.flatten()
    }

    private fun animeNyc2025IndexFrontX(letter: Char, booth: Int): Int? {
        if (booth > 4) return null
        val isEven = (booth % 2) == 0
        // These are irregular enough that they're just hardcoded
        return when (letter) {
            'A' -> -1 // This is okay because there are no odd As so this becomes 0 further down
            'B' -> 1
            'C' -> 5
            'E' -> 11
            'G' -> 16
            'H' -> 20
            'J' -> 26
            'L' -> 32
            'M' -> 34
            'N' -> 38
            'O' -> 40
            'P' -> 44
            'Q' -> 46
            'R' -> 49
            'S' -> 53
            'U' -> 59
            'W' -> 65
            else -> return null
        } + (if (isEven) 1 else 0)
    }

    private fun animeNyc2025IndexX(letter: Char, booth: Int): Int {
        val frontIndex = animeNyc2025IndexFrontX(letter, booth)
        if (frontIndex != null) return frontIndex
        val isEven = (booth % 2) == 0

        val offset = when {
            booth <= 4 -> if (isEven) 0 else -1
            isEven -> 1
            else -> -1
        }
        return (letter - 'A') * 3 + offset - 1
    }

    private fun mapAnimeNyc2025Booths(booths: List<BoothWithFavorite>): List<Table> {
        @Suppress("UNCHECKED_CAST")
        val letterToBooths = (booths.groupBy { it.booth?.take(1) }
            .filterKeys { it != null } as Map<String, List<BoothWithFavorite>>)
            .toList()
            .sortedBy { it.first }
        val showRandomCatalogImage = settings.showRandomCatalogImage.value
        return letterToBooths.mapIndexed { letterIndex, pair ->
            pair.second.groupBy { it.booth }
                .mapNotNull { (booth, artists) ->
                    booth ?: return@mapNotNull null
                    val tableNumber = booth.filter { it.isDigit() }.toInt()
                    val letter = booth[0]
                    val gridX = animeNyc2025IndexX(letter, tableNumber)
                    val gridY = (tableNumber - 1) / 2 + (if (tableNumber > 4) 1 else 0) +
                            (if (tableNumber > 16) 2 else 1)
                    val artist = artists.singleOrNull()
                    if (artist != null) {
                        val images = AlleyImageUtils.getArtistImages(
                            year = artist.year,
                            images = artist.images,
                        )
                        val imageIndex = if (showRandomCatalogImage) {
                            images.indices.randomOrNull()
                        } else {
                            0
                        }
                        Table.Single(
                            year = artist.year,
                            artistId = artist.id,
                            booth = booth,
                            section = null,
                            image = imageIndex?.let(images::getOrNull),
                            imageIndex = imageIndex,
                            favorite = artist.favorite,
                            gridX = gridX,
                            gridY = gridY,
                        )
                    } else {
                        val primaryArtist = artists.first()
                        val images = AlleyImageUtils.getArtistImages(
                            year = primaryArtist.year,
                            images = primaryArtist.images,
                        )
                        val imageIndex = if (showRandomCatalogImage) {
                            images.indices.randomOrNull()
                        } else {
                            0
                        }
                        Table.Shared(
                            year = primaryArtist.year,
                            artistIds = artists.map { it.id },
                            booth = booth,
                            section = null,
                            image = imageIndex?.let(images::getOrNull),
                            imageIndex = imageIndex,
                            favorite = artists.any { it.favorite },
                            gridX = gridX,
                            gridY = gridY,
                        )
                    }
                }
        }.flatten()
    }

    fun onFavoriteToggle(entry: ArtistEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(favorite = favorite))
    }

    fun onIgnoredToggle(entry: ArtistEntryGridModel, ignored: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(ignored = ignored))
    }

    suspend fun tableEntry(year: DataYear, artistId: String) =
        artistEntryDao.getEntry(year, artistId)?.let {
            ArtistEntryGridModel.buildFromEntry(
                randomSeed = randomSeed,
                showOnlyConfirmedTags = false, // This shouldn't matter here
                entry = it,
                showOutdatedCatalogs = false,
                fallbackCatalog = artistEntryDao.getFallbackImages(it.artist),
            )
        }

    data class GridData(
        val maxX: Int,
        val maxY: Int,
        val tables: List<Table>,
    )

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): MapViewModel
    }
}
