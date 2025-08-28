package com.thekeeperofpie.artistalleydatabase.alley.artist

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2023Queries
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2024Queries
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2025Queries
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryAnimeNyc2024
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryAnimeNyc2024Queries
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryAnimeNyc2025
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryAnimeNyc2025Queries
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistWithStampRalliesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.getBooleanFixed
import com.thekeeperofpie.artistalleydatabase.alley.rallies.toStampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.AnimeNycExhibitorTags
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CommissionType
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TagYearFlag
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.json.Json
import com.thekeeperofpie.artistalleydatabase.alley.artistEntry2023.GetEntry as GetEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.artistEntry2024.GetEntry as GetEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.artistEntry2025.GetEntry as GetEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.artistEntryAnimeNyc2024.GetEntry as GetEntryAnimeNyc2024
import com.thekeeperofpie.artistalleydatabase.alley.artistEntryAnimeNyc2025.GetEntry as GetEntryAnimeNyc2025

private fun SqlCursor.toArtistWithUserData2023(): ArtistWithUserData {
    val artistId = getString(0)!!
    return ArtistWithUserData(
        artist = ArtistEntry(
            year = DataYear.ANIME_EXPO_2023,
            id = artistId,
            booth = getString(1),
            name = getString(2)!!,
            summary = getString(4),
            links = getString(5)!!.let(Json::decodeFromString),
            storeLinks = emptyList(),
            catalogLinks = getString(6)!!.let(Json::decodeFromString),
            driveLink = getString(7),
            notes = null,
            commissions = emptyList(),
            seriesInferred = emptyList(),
            seriesConfirmed = emptyList(),
            merchInferred = emptyList(),
            merchConfirmed = emptyList(),
            images = getString(8)!!.let(Json::decodeFromString),
            counter = getLong(9)!!,
        ),
        userEntry = ArtistUserEntry(
            artistId = artistId,
            dataYear = DataYear.ANIME_EXPO_2023,
            favorite = getBooleanFixed(10),
            ignored = getBooleanFixed(11),
        )
    )
}

private fun SqlCursor.toArtistWithUserData2024(): ArtistWithUserData {
    val artistId = getString(0)!!
    return ArtistWithUserData(
        artist = ArtistEntry(
            year = DataYear.ANIME_EXPO_2024,
            id = artistId,
            booth = getString(1),
            name = getString(2)!!,
            summary = getString(3),
            links = getString(4)!!.let(Json::decodeFromString),
            storeLinks = getString(5)!!.let(Json::decodeFromString),
            catalogLinks = getString(6)!!.let(Json::decodeFromString),
            driveLink = getString(7),
            notes = getString(8),
            commissions = emptyList(),
            seriesInferred = getString(9)!!.let(Json::decodeFromString),
            seriesConfirmed = getString(10)!!.let(Json::decodeFromString),
            merchInferred = getString(11)!!.let(Json::decodeFromString),
            merchConfirmed = getString(12)!!.let(Json::decodeFromString),
            images = getString(13)!!.let(Json::decodeFromString),
            counter = getLong(14)!!,
        ),
        userEntry = ArtistUserEntry(
            artistId = artistId,
            dataYear = DataYear.ANIME_EXPO_2024,
            favorite = getBooleanFixed(15),
            ignored = getBooleanFixed(16),
        )
    )
}

private fun SqlCursor.toArtistWithUserData2025(): ArtistWithUserData {
    val artistId = getString(0)!!
    return ArtistWithUserData(
        artist = ArtistEntry(
            year = DataYear.ANIME_EXPO_2025,
            id = artistId,
            booth = getString(1),
            name = getString(2)!!,
            summary = getString(3),
            links = getString(4)!!.let(Json::decodeFromString),
            storeLinks = getString(5)!!.let(Json::decodeFromString),
            catalogLinks = getString(6)!!.let(Json::decodeFromString),
            // Skip 2 for link flags
            driveLink = getString(9),
            notes = getString(10),
            commissions = getString(11)!!.let(Json::decodeFromString),
            // Skip 1 for commission flags
            seriesInferred = getString(13)!!.let(Json::decodeFromString),
            seriesConfirmed = getString(14)!!.let(Json::decodeFromString),
            merchInferred = getString(15)!!.let(Json::decodeFromString),
            merchConfirmed = getString(16)!!.let(Json::decodeFromString),
            images = getString(17)!!.let(Json::decodeFromString),
            counter = getLong(18)!!,
        ),
        userEntry = ArtistUserEntry(
            artistId = artistId,
            dataYear = DataYear.ANIME_EXPO_2025,
            favorite = getBooleanFixed(19),
            ignored = getBooleanFixed(20),
        )
    )
}

private fun SqlCursor.toArtistWithUserDataAnimeNyc2024(): ArtistWithUserData {
    val artistId = getString(0)!!
    return ArtistWithUserData(
        artist = ArtistEntry(
            year = DataYear.ANIME_NYC_2024,
            id = artistId,
            booth = getString(1),
            name = getString(2)!!,
            summary = getString(3),
            links = getString(4)!!.let(Json::decodeFromString),
            storeLinks = getString(5)!!.let(Json::decodeFromString),
            catalogLinks = getString(6)!!.let(Json::decodeFromString),
            // Skip 2 for link flags
            driveLink = getString(9),
            notes = getString(10),
            commissions = getString(11)!!.let(Json::decodeFromString),
            // Skip 1 for commission flags
            seriesInferred = getString(13)!!.let(Json::decodeFromString),
            seriesConfirmed = getString(14)!!.let(Json::decodeFromString),
            merchInferred = getString(15)!!.let(Json::decodeFromString),
            merchConfirmed = getString(16)!!.let(Json::decodeFromString),
            images = getString(17)!!.let(Json::decodeFromString),
            counter = getLong(18)!!,
        ),
        userEntry = ArtistUserEntry(
            artistId = artistId,
            dataYear = DataYear.ANIME_NYC_2024,
            favorite = getBooleanFixed(19),
            ignored = getBooleanFixed(20),
        )
    )
}

private fun SqlCursor.toArtistWithUserDataAnimeNyc2025(): ArtistWithUserData {
    val artistId = getString(0)!!
    return ArtistWithUserData(
        artist = ArtistEntry(
            year = DataYear.ANIME_NYC_2025,
            id = artistId,
            booth = getString(1),
            name = getString(2)!!,
            summary = getString(3),
            links = getString(4)!!.let(Json::decodeFromString),
            storeLinks = getString(5)!!.let(Json::decodeFromString),
            catalogLinks = getString(6)!!.let(Json::decodeFromString),
            // Skip 2 for link flags
            driveLink = getString(9),
            notes = getString(10),
            commissions = getString(11)!!.let(Json::decodeFromString),
            // Skip 1 for commission flags
            seriesInferred = getString(13)!!.let(Json::decodeFromString),
            seriesConfirmed = getString(14)!!.let(Json::decodeFromString),
            merchInferred = getString(15)!!.let(Json::decodeFromString),
            merchConfirmed = getString(16)!!.let(Json::decodeFromString),
            // Skip 1 for exhibitor tag flags
            images = getString(18)!!.let(Json::decodeFromString),
            counter = getLong(19)!!,
        ),
        userEntry = ArtistUserEntry(
            artistId = artistId,
            dataYear = DataYear.ANIME_NYC_2025,
            favorite = getBooleanFixed(20),
            ignored = getBooleanFixed(21),
        )
    )
}

private fun GetEntry2023.toArtistWithUserData() = ArtistWithUserData(
    artist = ArtistEntry(
        year = DataYear.ANIME_EXPO_2023,
        id = id,
        booth = booth,
        name = name,
        summary = summary,
        links = links,
        storeLinks = emptyList(),
        catalogLinks = catalogLinks,
        driveLink = driveLink,
        notes = null,
        commissions = emptyList(),
        seriesInferred = emptyList(),
        seriesConfirmed = emptyList(),
        merchInferred = emptyList(),
        merchConfirmed = emptyList(),
        images = emptyList(),
        counter = counter,
    ),
    userEntry = ArtistUserEntry(
        artistId = id,
        dataYear = DataYear.ANIME_EXPO_2023,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
        ignored = DaoUtils.coerceBooleanForJs(ignored),
    )
)

private fun GetEntry2024.toArtistWithUserData() = ArtistWithUserData(
    artist = ArtistEntry(
        year = DataYear.ANIME_EXPO_2024,
        id = id,
        booth = booth,
        name = name,
        summary = summary,
        links = links,
        storeLinks = storeLinks,
        catalogLinks = catalogLinks,
        driveLink = driveLink,
        notes = notes,
        commissions = emptyList(),
        seriesInferred = seriesInferred,
        seriesConfirmed = seriesConfirmed,
        merchInferred = merchInferred,
        merchConfirmed = merchConfirmed,
        images = images,
        counter = counter,
    ),
    userEntry = ArtistUserEntry(
        artistId = id,
        dataYear = DataYear.ANIME_EXPO_2024,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
        ignored = DaoUtils.coerceBooleanForJs(ignored),
    )
)

private fun GetEntry2025.toArtistWithUserData() = ArtistWithUserData(
    artist = ArtistEntry(
        year = DataYear.ANIME_EXPO_2025,
        id = id,
        booth = booth,
        name = name,
        summary = summary,
        links = links,
        storeLinks = storeLinks,
        catalogLinks = catalogLinks,
        driveLink = driveLink,
        notes = notes,
        commissions = commissions,
        seriesInferred = seriesInferred,
        seriesConfirmed = seriesConfirmed,
        merchInferred = merchInferred,
        merchConfirmed = merchConfirmed,
        images = images,
        counter = counter,
    ),
    userEntry = ArtistUserEntry(
        artistId = id,
        dataYear = DataYear.ANIME_EXPO_2025,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
        ignored = DaoUtils.coerceBooleanForJs(ignored),
    )
)

private fun GetEntryAnimeNyc2024.toArtistWithUserData() = ArtistWithUserData(
    artist = ArtistEntry(
        year = DataYear.ANIME_NYC_2024,
        id = id,
        booth = booth,
        name = name,
        summary = summary,
        links = links,
        storeLinks = storeLinks,
        catalogLinks = catalogLinks,
        driveLink = driveLink,
        notes = notes,
        commissions = commissions,
        seriesInferred = seriesInferred,
        seriesConfirmed = seriesConfirmed,
        merchInferred = merchInferred,
        merchConfirmed = merchConfirmed,
        images = images,
        counter = counter,
    ),
    userEntry = ArtistUserEntry(
        artistId = id,
        dataYear = DataYear.ANIME_NYC_2024,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
        ignored = DaoUtils.coerceBooleanForJs(ignored),
    )
)

private fun GetEntryAnimeNyc2025.toArtistWithUserData() = ArtistWithUserData(
    artist = ArtistEntry(
        year = DataYear.ANIME_NYC_2025,
        id = id,
        booth = booth,
        name = name,
        summary = summary,
        links = links,
        storeLinks = storeLinks,
        catalogLinks = catalogLinks,
        driveLink = driveLink,
        notes = notes,
        commissions = commissions,
        seriesInferred = seriesInferred,
        seriesConfirmed = seriesConfirmed,
        merchInferred = merchInferred,
        merchConfirmed = merchConfirmed,
        images = images,
        counter = counter,
    ),
    userEntry = ArtistUserEntry(
        artistId = id,
        dataYear = DataYear.ANIME_NYC_2025,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
        ignored = DaoUtils.coerceBooleanForJs(ignored),
    )
)

fun ArtistEntry2023.toArtistEntry() = ArtistEntry(
    year = DataYear.ANIME_EXPO_2023,
    id = id,
    booth = booth,
    name = name,
    summary = summary,
    links = links,
    storeLinks = emptyList(),
    catalogLinks = catalogLinks,
    driveLink = driveLink,
    notes = null,
    commissions = emptyList(),
    seriesInferred = emptyList(),
    seriesConfirmed = emptyList(),
    merchInferred = emptyList(),
    merchConfirmed = emptyList(),
    images = images,
    counter = counter,
)

fun ArtistEntry2024.toArtistEntry() = ArtistEntry(
    year = DataYear.ANIME_EXPO_2024,
    id = id,
    booth = booth,
    name = name,
    summary = summary,
    links = links,
    storeLinks = storeLinks,
    catalogLinks = catalogLinks,
    driveLink = driveLink,
    notes = notes,
    commissions = emptyList(),
    seriesInferred = seriesInferred,
    seriesConfirmed = seriesConfirmed,
    merchInferred = merchInferred,
    merchConfirmed = merchConfirmed,
    images = images,
    counter = counter,
)

fun ArtistEntry2025.toArtistEntry() = ArtistEntry(
    year = DataYear.ANIME_EXPO_2025,
    id = id,
    booth = booth,
    name = name,
    summary = summary,
    links = links,
    storeLinks = storeLinks,
    catalogLinks = catalogLinks,
    driveLink = driveLink,
    notes = notes,
    commissions = commissions,
    seriesInferred = seriesInferred,
    seriesConfirmed = seriesConfirmed,
    merchInferred = merchInferred,
    merchConfirmed = merchConfirmed,
    images = images,
    counter = counter,
)

fun ArtistEntryAnimeNyc2024.toArtistEntry() = ArtistEntry(
    year = DataYear.ANIME_NYC_2024,
    id = id,
    booth = booth,
    name = name,
    summary = summary,
    links = links,
    storeLinks = storeLinks,
    catalogLinks = catalogLinks,
    driveLink = driveLink,
    notes = notes,
    commissions = commissions,
    seriesInferred = seriesInferred,
    seriesConfirmed = seriesConfirmed,
    merchInferred = merchInferred,
    merchConfirmed = merchConfirmed,
    images = images,
    counter = counter,
)

fun ArtistEntryAnimeNyc2025.toArtistEntry() = ArtistEntry(
    year = DataYear.ANIME_NYC_2025,
    id = id,
    booth = booth,
    name = name,
    summary = summary,
    links = links,
    storeLinks = storeLinks,
    catalogLinks = catalogLinks,
    driveLink = driveLink,
    notes = notes,
    commissions = commissions,
    seriesInferred = seriesInferred,
    seriesConfirmed = seriesConfirmed,
    merchInferred = merchInferred,
    merchConfirmed = merchConfirmed,
    images = images,
    counter = counter,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val settings: ArtistAlleySettings,
    private val dao2023: suspend () -> ArtistEntry2023Queries = { database().artistEntry2023Queries },
    private val dao2024: suspend () -> ArtistEntry2024Queries = { database().artistEntry2024Queries },
    private val dao2025: suspend () -> ArtistEntry2025Queries = { database().artistEntry2025Queries },
    private val daoAnimeNyc2024: suspend () -> ArtistEntryAnimeNyc2024Queries = { database().artistEntryAnimeNyc2024Queries },
    private val daoAnimeNyc2025: suspend () -> ArtistEntryAnimeNyc2025Queries = { database().artistEntryAnimeNyc2025Queries },
) {
    suspend fun getEntry(year: DataYear, id: String) =
        when (year) {
            DataYear.ANIME_EXPO_2023 -> dao2023()
                .getEntry(id)
                .awaitAsOneOrNull()
                ?.toArtistWithUserData()
            DataYear.ANIME_EXPO_2024 -> dao2024()
                .getEntry(id)
                .awaitAsOneOrNull()
                ?.toArtistWithUserData()
            DataYear.ANIME_EXPO_2025 -> dao2025()
                .getEntry(id)
                .awaitAsOneOrNull()
                ?.toArtistWithUserData()
            DataYear.ANIME_NYC_2024 -> daoAnimeNyc2024()
                .getEntry(id)
                .awaitAsOneOrNull()
                ?.toArtistWithUserData()
            DataYear.ANIME_NYC_2025 -> daoAnimeNyc2025()
                .getEntry(id)
                .awaitAsOneOrNull()
                ?.toArtistWithUserData()
        }

    suspend fun getFallbackImages(entry: ArtistEntry) =
        getFallbackImages(entry.year, entry.id, entry.images)

    suspend fun getFallbackImages(
        year: DataYear,
        id: String,
        images: List<CatalogImage>,
    ): Pair<DataYear, List<com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage>>? {
        // This check is required in every use case, so it's moved into this method
        if (images.isNotEmpty()) return null
        return DataYear.entries.asReversed()
            .dropWhile { it != year }
            .firstNotNullOfOrNull { year ->
                getEntry(year, id)
                    ?.artist?.images?.takeIf { it.isNotEmpty() }
                    ?.let { year to AlleyDataUtils.getArtistImages(year, it) }
            }
    }

    suspend fun getEntriesByBooth(year: DataYear, booth: String) =
        when (year) {
            DataYear.ANIME_EXPO_2023 -> dao2023()
                .getEntriesByBooth(booth)
                .awaitAsList()
                .map { it.toArtistEntry() }
            DataYear.ANIME_EXPO_2024 -> dao2024()
                .getEntriesByBooth(booth)
                .awaitAsList()
                .map { it.toArtistEntry() }
            DataYear.ANIME_EXPO_2025 -> dao2025()
                .getEntriesByBooth(booth)
                .awaitAsList()
                .map { it.toArtistEntry() }
            DataYear.ANIME_NYC_2024 -> daoAnimeNyc2024()
                .getEntriesByBooth(booth)
                .awaitAsList()
                .map { it.toArtistEntry() }
            DataYear.ANIME_NYC_2025 -> daoAnimeNyc2025()
                .getEntriesByBooth(booth)
                .awaitAsList()
                .map { it.toArtistEntry() }
        }

    fun getEntryFlow(id: String) = settings.dataYear
        .flatMapLatest {
            when (it) {
                DataYear.ANIME_EXPO_2023 -> dao2023()
                    .getEntry(id)
                    .asFlow()
                    .mapToOne(PlatformDispatchers.IO)
                    .mapLatest { it.toArtistWithUserData() }
                DataYear.ANIME_EXPO_2024 -> dao2024()
                    .getEntry(id)
                    .asFlow()
                    .mapToOne(PlatformDispatchers.IO)
                    .mapLatest { it.toArtistWithUserData() }
                DataYear.ANIME_EXPO_2025 -> dao2025()
                    .getEntry(id)
                    .asFlow()
                    .mapToOne(PlatformDispatchers.IO)
                    .mapLatest { it.toArtistWithUserData() }
                DataYear.ANIME_NYC_2024 -> daoAnimeNyc2024()
                    .getEntry(id)
                    .asFlow()
                    .mapToOne(PlatformDispatchers.IO)
                    .mapLatest { it.toArtistWithUserData() }
                DataYear.ANIME_NYC_2025 -> daoAnimeNyc2025()
                    .getEntry(id)
                    .asFlow()
                    .mapToOne(PlatformDispatchers.IO)
                    .mapLatest { it.toArtistWithUserData() }
            }
        }

    suspend fun getEntryWithStampRallies(
        dataYear: DataYear,
        artistId: String,
    ): ArtistWithStampRalliesEntry? =
        when (dataYear) {
            DataYear.ANIME_EXPO_2023 -> dao2023().transactionWithResult {
                val artist = getEntry(dataYear, artistId) ?: return@transactionWithResult null
                val stampRallies = dao2023().getStampRallyEntries(artistId).awaitAsList()
                    .map { it.toStampRallyEntry() }
                ArtistWithStampRalliesEntry(artist, stampRallies)
            }
            DataYear.ANIME_EXPO_2024 -> dao2024().transactionWithResult {
                val artist = getEntry(dataYear, artistId) ?: return@transactionWithResult null
                val stampRallies = dao2024().getStampRallyEntries(artistId).awaitAsList()
                    .map { it.toStampRallyEntry() }
                ArtistWithStampRalliesEntry(artist, stampRallies)
            }
            DataYear.ANIME_EXPO_2025 -> dao2025().transactionWithResult {
                val artist = getEntry(dataYear, artistId) ?: return@transactionWithResult null
                val stampRallies = dao2025().getStampRallyEntries(artistId).awaitAsList()
                    .map { it.toStampRallyEntry() }
                ArtistWithStampRalliesEntry(artist, stampRallies)
            }
            DataYear.ANIME_NYC_2024 -> daoAnimeNyc2024().run {
                val artist = getEntry(dataYear, artistId) ?: return null
                ArtistWithStampRalliesEntry(artist, emptyList())
            }
            DataYear.ANIME_NYC_2025 -> daoAnimeNyc2025().run {
                val artist = getEntry(dataYear, artistId) ?: return null
                ArtistWithStampRalliesEntry(artist, emptyList())
            }
        }

    fun search(
        year: DataYear,
        query: String,
        searchQuery: ArtistSearchQuery,
        onlyFavorites: Boolean = false,
        lockedBooths: Set<String> = emptySet(),
    ): Pair<String, String> {
        val tableName = year.artistTableName
        val filterParams = searchQuery.filterParams
        val andClauses = mutableListOf<String>().apply {
            if (onlyFavorites) this += "artistUserEntry.favorite = 1"
            if (lockedBooths.isNotEmpty()) {
                this += "$tableName.booth IN " +
                        lockedBooths.joinToString(
                            prefix = "(",
                            separator = ",",
                            postfix = ")"
                        ) { "'$it'" }
            }

            // Search for "http" as a simplification of logic, since checking
            // not empty would require a separate query template
            if (filterParams.showOnlyWithCatalog) this += "$tableName.driveLink LIKE 'http%'"

            if (year != DataYear.ANIME_EXPO_2023 && year != DataYear.ANIME_EXPO_2024) {
                val commissionFlags = filterParams.commissionsIn.fold(0) { flags, type ->
                    val index = CommissionType.entries.indexOf(type)
                    flags or (1 shl index)
                }

                if (commissionFlags != 0) {
                    this += "($tableName.commissionFlags & $commissionFlags) != 0"
                }

                val linkFlags = filterParams.linkTypesIn.fold(0) { flags, type ->
                    val index = Link.Type.entries.indexOf(type)
                    if (index < 32) {
                        flags or (1 shl index)
                    } else {
                        flags
                    }
                }
                val linkFlags2 = filterParams.linkTypesIn.fold(0) { flags, type ->
                    val index = Link.Type.entries.indexOf(type)
                    if (index >= 32) {
                        flags or (1 shl (index - 32))
                    } else {
                        flags
                    }
                }

                val linkTypeStatements = mutableListOf<String>()
                if (linkFlags != 0) {
                    linkTypeStatements += "($tableName.linkFlags & $linkFlags) != 0"
                }
                if (linkFlags2 != 0) {
                    linkTypeStatements += "($tableName.linkFlags2 & $linkFlags2) != 0"
                }

                if (linkTypeStatements.isNotEmpty()) {
                    this += "(${linkTypeStatements.joinToString(separator = " OR ")})"
                }
            }

            if (year == DataYear.ANIME_NYC_2025) {
                val exhibitorTagFlags =
                    AnimeNycExhibitorTags.parseFlags(filterParams.exhibitorTagsIn)
                if (exhibitorTagFlags != 0L) {
                    this += "($tableName.exhibitorTagFlags & $exhibitorTagFlags) != 0"
                }
            }

            // TODO: Locked series/merch doesn't enforce AND
            if (filterParams.seriesIn.isNotEmpty()) {
                val yearFilter = when (year) {
                    DataYear.ANIME_EXPO_2023 -> ""
                    else -> {
                        val flag = TagYearFlag.getFlag(
                            year,
                            confirmed = filterParams.showOnlyConfirmedTags
                        )
                        "(artistSeriesConnection.yearFlags & $flag) != 0 AND "
                    }
                }

                val seriesList = filterParams.seriesIn.joinToString(separator = ",") {
                    DatabaseUtils.sqlEscapeString(it)
                }

                this += "$tableName.id IN (SELECT artistId from artistSeriesConnection WHERE " +
                        yearFilter +
                        "artistSeriesConnection.seriesId IN ($seriesList))"
            }

            if (filterParams.merchIn.isNotEmpty()) {
                val yearFilter = when (year) {
                    DataYear.ANIME_EXPO_2023 -> ""
                    else -> {
                        val flag = TagYearFlag.getFlag(
                            year,
                            confirmed = filterParams.showOnlyConfirmedTags
                        )
                        "(artistMerchConnection.yearFlags & $flag) != 0 AND "
                    }
                }

                val merchList = filterParams.merchIn.joinToString(separator = ",") {
                    DatabaseUtils.sqlEscapeString(it)
                }

                this += "$tableName.id IN (SELECT artistId from artistMerchConnection WHERE " +
                        yearFilter +
                        "artistMerchConnection.merchId IN ($merchList))"
            }
        }

        val ascending = if (filterParams.sortAscending) "ASC" else "DESC"
        val sortSuffix = when (filterParams.sortOption) {
            ArtistSearchSortOption.BOOTH -> "ORDER BY $tableName.booth COLLATE NOCASE"
            ArtistSearchSortOption.ARTIST -> "ORDER BY $tableName.name COLLATE NOCASE"
            ArtistSearchSortOption.RANDOM -> "ORDER BY orderIndex"
        } + " $ascending" + " NULLS LAST"
        val randomSortSelectSuffix =
            (", substr(${tableName}_fts.counter * 0.${searchQuery.randomSeed}," +
                    " length(${tableName}_fts.counter) + 2) as orderIndex")
                .takeIf { filterParams.sortOption == ArtistSearchSortOption.RANDOM }
                .orEmpty()
        val selectSuffix = ", artistUserEntry.favorite, artistUserEntry.ignored"

        val joinStatement = """
                LEFT OUTER JOIN artistUserEntry
                ON idAsKey = artistUserEntry.artistId
                AND '${year.serializedName}' = artistUserEntry.dataYear
            """.trimIndent()

        val andStatement = andClauses.takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "WHERE ", separator = "\nAND ").orEmpty()

        if (query.isEmpty()) {
            val countStatement = """
                SELECT COUNT(*)
                FROM $tableName
                ${joinStatement.replace("idAsKey", "$tableName.id")}
                $andStatement
            """.trimIndent()
            val statement = """
                SELECT $tableName.*$selectSuffix${randomSortSelectSuffix.replace("_fts", "")}
                FROM $tableName
                ${joinStatement.replace("idAsKey", "$tableName.id")}
                $andStatement
                ${sortSuffix.replace("_fts", "")}
                """.trimIndent()

            return countStatement to statement
        }

        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)
        val targetColumns = listOfNotNull(
            "booth",
            "name",
            "summary",
            "notes".takeIf {
                year != DataYear.ANIME_EXPO_2023 && year != DataYear.ANIME_EXPO_2024
            }, // TODO: Expose 2024 notes?
        ).let {
            if (year == DataYear.ANIME_EXPO_2023) {
                it
            } else {
                it + listOfNotNull(
                    "seriesInferred".takeUnless { filterParams.showOnlyConfirmedTags },
                    "seriesConfirmed",
                    "merchInferred".takeUnless { filterParams.showOnlyConfirmedTags },
                    "merchConfirmed",
                )
            }
        }

        val matchOptions = mutableListOf<String>()
        val matchQuery = buildString {
            append("'")
            append(matchOptions.joinToString(separator = " ", postfix = " "))
            append("{ ${targetColumns.joinToString(separator = " ")} } : $matchOrQuery'")
        }

        val likeStatement = targetColumns.joinToString(separator = "\nOR ") {
            "(${DaoUtils.makeLikeAndQuery("${tableName}_fts.$it", queries)})"
        }

        val countStatement = DaoUtils.buildSearchCountStatement(
            ftsTableName = "${tableName}_fts",
            idField = "id",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
            additionalJoinStatement = joinStatement,
            andStatement = andStatement.replace(tableName, "${tableName}_fts"),
        )

        val statement = DaoUtils.buildSearchStatement(
            tableName = tableName,
            ftsTableName = "${tableName}_fts",
            select = "$tableName.*$selectSuffix",
            idField = "id",
            likeOrderBy = "",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
            additionalJoinStatement = joinStatement,
            orderBy = sortSuffix,
            randomSeed = searchQuery.randomSeed
                .takeIf { filterParams.sortOption == ArtistSearchSortOption.RANDOM },
            andStatement = andStatement,
        )

        return countStatement to statement
    }

    suspend fun searchCount(
        year: DataYear,
        query: String,
        searchQuery: ArtistSearchQuery,
        onlyFavorites: Boolean = false,
        lockedBooths: Set<String> = emptySet(),
    ): Flow<Int> {
        val (countStatement, _) = search(year, query, searchQuery, onlyFavorites, lockedBooths)
        return DaoUtils.makeQuery(
            driver(),
            statement = countStatement,
            tableNames = listOf("${year.artistTableName}_fts", "artistUserEntry"),
            mapper = { it.getLong(0)!!.toInt() },
        ).asFlow()
            .mapToOneOrDefault(0, PlatformDispatchers.IO)
    }

    fun searchPagingSource(
        year: DataYear,
        query: String,
        searchQuery: ArtistSearchQuery,
        onlyFavorites: Boolean = false,
        lockedBooths: Set<String> = emptySet(),
    ): PagingSource<Int, ArtistWithUserData> {
        val (countStatement, searchStatement) = search(
            year = year,
            query = query,
            searchQuery = searchQuery,
            onlyFavorites = onlyFavorites,
            lockedBooths = lockedBooths,
        )

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = searchStatement,
            tableNames = listOf("${year.artistTableName}_fts", "artistUserEntry"),
            mapper = when (year) {
                DataYear.ANIME_EXPO_2023 -> SqlCursor::toArtistWithUserData2023
                DataYear.ANIME_EXPO_2024 -> SqlCursor::toArtistWithUserData2024
                DataYear.ANIME_EXPO_2025 -> SqlCursor::toArtistWithUserData2025
                DataYear.ANIME_NYC_2024 -> SqlCursor::toArtistWithUserDataAnimeNyc2024
                DataYear.ANIME_NYC_2025 -> SqlCursor::toArtistWithUserDataAnimeNyc2025
            },
        )
    }
}
