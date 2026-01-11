package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.data.toArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun ArtistDatabaseEntry.Impl.fixForJs() =
    copy(verifiedArtist = coerceBooleanForJs(verifiedArtist))

// TODO: js target boolean equality broken
// JS interop infers this as 0, whereas KotlinX Serialization requires true
@Suppress("EQUALITY_NOT_APPLICABLE_WARNING")
internal fun coerceBooleanForJs(value: Boolean?) = value == true || value.toString() == "1"

/** Shared utilities between [AlleyEditBackend] and [AlleyFormBackend] */
internal object BackendUtils {

    suspend fun loadArtist(context: EventContext, dataYear: DataYear, artistId: Uuid) =
        when (dataYear) {
            DataYear.ANIME_EXPO_2026 -> Databases.editDatabase(context)
                .artistEntryAnimeExpo2026Queries
                .getArtist(artistId.toString())
                .awaitAsOneOrNull()
                ?.toArtistDatabaseEntry()
                ?.fixForJs()
            // TODO: Support other conventions?
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> null
        }

    suspend fun loadFormDiff(
        context: EventContext,
        dataYear: DataYear,
        artistId: Uuid,
    ): ArtistEntryDiff? {
        val formEntry = Databases.formDatabase(context)
            .artistFormEntryQueries
            .getFormEntry(dataYear, artistId)
            .awaitAsOneOrNull()
            ?: return null
        return ArtistEntryDiff(
            booth = formEntry.afterBooth.orEmpty()
                .takeIf { it != formEntry.beforeBooth.orEmpty() },
            name = formEntry.afterName.orEmpty()
                .takeIf { it != formEntry.beforeName.orEmpty() },
            summary = formEntry.afterSummary.orEmpty()
                .takeIf { it != formEntry.beforeSummary.orEmpty() },
            notes = formEntry.afterNotes.orEmpty()
                .takeIf { it != formEntry.beforeNotes.orEmpty() },
            socialLinks = ArtistEntryDiff.diffList(
                formEntry.beforeSocialLinks,
                formEntry.afterSocialLinks
            ),
            storeLinks = ArtistEntryDiff.diffList(
                formEntry.beforeStoreLinks,
                formEntry.afterStoreLinks
            ),
            portfolioLinks = ArtistEntryDiff.diffList(
                formEntry.beforePortfolioLinks,
                formEntry.afterPortfolioLinks
            ),
            catalogLinks = ArtistEntryDiff.diffList(
                formEntry.beforeCatalogLinks,
                formEntry.afterCatalogLinks
            ),
            commissions = ArtistEntryDiff.diffList(
                formEntry.beforeCommissions,
                formEntry.afterCommissions
            ),
            seriesInferred = ArtistEntryDiff.diffList(
                formEntry.beforeSeriesInferred,
                formEntry.afterSeriesInferred
            ),
            seriesConfirmed = ArtistEntryDiff.diffList(
                formEntry.beforeSeriesConfirmed,
                formEntry.afterSeriesConfirmed
            ),
            merchInferred = ArtistEntryDiff.diffList(
                formEntry.beforeMerchInferred,
                formEntry.afterMerchInferred
            ),
            merchConfirmed = ArtistEntryDiff.diffList(
                formEntry.beforeMerchConfirmed,
                formEntry.afterMerchConfirmed
            ),
            formNotes = formEntry.formNotes.orEmpty(),
            timestamp = formEntry.timestamp,
        )
    }

    suspend fun loadFormHistoryDiff(
        context: EventContext,
        dataYear: DataYear,
        artistId: Uuid,
        timestamp: Instant,
    ): ArtistEntryDiff? {
        val formEntry = Databases.formDatabase(context)
            .artistFormEntryQueries
            .getFormHistoryEntry(dataYear, artistId, timestamp)
            .awaitAsOneOrNull()
            ?: return null
        return ArtistEntryDiff(
            booth = formEntry.afterBooth.orEmpty()
                .takeIf { it != formEntry.beforeBooth.orEmpty() },
            name = formEntry.afterName.orEmpty()
                .takeIf { it != formEntry.beforeName.orEmpty() },
            summary = formEntry.afterSummary.orEmpty()
                .takeIf { it != formEntry.beforeSummary.orEmpty() },
            notes = formEntry.afterNotes.orEmpty()
                .takeIf { it != formEntry.beforeNotes.orEmpty() },
            socialLinks = ArtistEntryDiff.diffList(
                formEntry.beforeSocialLinks,
                formEntry.afterSocialLinks
            ),
            storeLinks = ArtistEntryDiff.diffList(
                formEntry.beforeStoreLinks,
                formEntry.afterStoreLinks
            ),
            portfolioLinks = ArtistEntryDiff.diffList(
                formEntry.beforePortfolioLinks,
                formEntry.afterPortfolioLinks
            ),
            catalogLinks = ArtistEntryDiff.diffList(
                formEntry.beforeCatalogLinks,
                formEntry.afterCatalogLinks
            ),
            commissions = ArtistEntryDiff.diffList(
                formEntry.beforeCommissions,
                formEntry.afterCommissions
            ),
            seriesInferred = ArtistEntryDiff.diffList(
                formEntry.beforeSeriesInferred,
                formEntry.afterSeriesInferred
            ),
            seriesConfirmed = ArtistEntryDiff.diffList(
                formEntry.beforeSeriesConfirmed,
                formEntry.afterSeriesConfirmed
            ),
            merchInferred = ArtistEntryDiff.diffList(
                formEntry.beforeMerchInferred,
                formEntry.afterMerchInferred
            ),
            merchConfirmed = ArtistEntryDiff.diffList(
                formEntry.beforeMerchConfirmed,
                formEntry.afterMerchConfirmed
            ),
            formNotes = formEntry.formNotes.orEmpty(),
            timestamp = formEntry.timestamp,
        )
    }
}
