package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.data.toArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.toStampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.ResponseWithBody
import com.thekeeperofpie.artistalleydatabase.alley.functions.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.HistoryListDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.coroutines.await
import org.w3c.fetch.Headers
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun ArtistDatabaseEntry.Impl.fixForJs() =
    copy(verifiedArtist = coerceBooleanForJs(verifiedArtist))

internal fun StampRallyDatabaseEntry.fixForJs() =
    copy(confirmed = coerceBooleanForJs(confirmed))

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

    suspend fun loadArtistFormDiff(
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
            socialLinks = HistoryListDiff.diffList(
                formEntry.beforeSocialLinks,
                formEntry.afterSocialLinks
            ),
            storeLinks = HistoryListDiff.diffList(
                formEntry.beforeStoreLinks,
                formEntry.afterStoreLinks
            ),
            portfolioLinks = HistoryListDiff.diffList(
                formEntry.beforePortfolioLinks,
                formEntry.afterPortfolioLinks
            ),
            catalogLinks = HistoryListDiff.diffList(
                formEntry.beforeCatalogLinks,
                formEntry.afterCatalogLinks
            ),
            commissions = HistoryListDiff.diffList(
                formEntry.beforeCommissions,
                formEntry.afterCommissions
            ),
            seriesInferred = HistoryListDiff.diffList(
                formEntry.beforeSeriesInferred,
                formEntry.afterSeriesInferred
            ),
            seriesConfirmed = HistoryListDiff.diffList(
                formEntry.beforeSeriesConfirmed,
                formEntry.afterSeriesConfirmed
            ),
            merchInferred = HistoryListDiff.diffList(
                formEntry.beforeMerchInferred,
                formEntry.afterMerchInferred
            ),
            merchConfirmed = HistoryListDiff.diffList(
                formEntry.beforeMerchConfirmed,
                formEntry.afterMerchConfirmed
            ),
            formNotes = formEntry.formNotes.orEmpty(),
            timestamp = formEntry.timestamp,
        )
    }

    suspend fun loadArtistFormHistoryDiff(
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
            socialLinks = HistoryListDiff.diffList(
                formEntry.beforeSocialLinks,
                formEntry.afterSocialLinks
            ),
            storeLinks = HistoryListDiff.diffList(
                formEntry.beforeStoreLinks,
                formEntry.afterStoreLinks
            ),
            portfolioLinks = HistoryListDiff.diffList(
                formEntry.beforePortfolioLinks,
                formEntry.afterPortfolioLinks
            ),
            catalogLinks = HistoryListDiff.diffList(
                formEntry.beforeCatalogLinks,
                formEntry.afterCatalogLinks
            ),
            commissions = HistoryListDiff.diffList(
                formEntry.beforeCommissions,
                formEntry.afterCommissions
            ),
            seriesInferred = HistoryListDiff.diffList(
                formEntry.beforeSeriesInferred,
                formEntry.afterSeriesInferred
            ),
            seriesConfirmed = HistoryListDiff.diffList(
                formEntry.beforeSeriesConfirmed,
                formEntry.afterSeriesConfirmed
            ),
            merchInferred = HistoryListDiff.diffList(
                formEntry.beforeMerchInferred,
                formEntry.afterMerchInferred
            ),
            merchConfirmed = HistoryListDiff.diffList(
                formEntry.beforeMerchConfirmed,
                formEntry.afterMerchConfirmed
            ),
            formNotes = formEntry.formNotes.orEmpty(),
            timestamp = formEntry.timestamp,
        )
    }

    suspend fun loadStampRally(
        context: EventContext,
        request: BackendRequest.StampRally,
    ): StampRallyDatabaseEntry? =
        when (request.dataYear) {
            DataYear.ANIME_EXPO_2026 -> Databases.editDatabase(context)
                .stampRallyEntryAnimeExpo2026Queries
                .getStampRally(request.stampRallyId)
                .awaitAsOneOrNull()
                ?.toStampRallyDatabaseEntry()
                ?.fixForJs()
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> null // TODO: Return legacy years?
        }

    suspend fun loadStampRallyFormDiffs(
        context: EventContext,
        dataYear: DataYear,
        artistId: Uuid,
    ): List<StampRallyEntryDiff> = Databases.formDatabase(context)
        .stampRallyFormEntryQueries
        .getFormEntriesByArtist(dataYear, artistId)
        .awaitAsList()
        .map { formEntry ->
            StampRallyEntryDiff(
                id = formEntry.stampRallyId,
                fandom = formEntry.afterFandom.orEmpty()
                    .takeIf { it != formEntry.beforeFandom.orEmpty() },
                tables = HistoryListDiff.diffList(
                    formEntry.beforeTables,
                    formEntry.afterTables
                ),
                links = HistoryListDiff.diffList(formEntry.beforeLinks, formEntry.afterLinks),
                tableMin = formEntry.afterTableMin.takeIf { it != formEntry.beforeTableMin },
                prize = formEntry.afterPrize.takeIf { it != formEntry.beforePrize },
                prizeLimit = formEntry.afterPrizeLimit.takeIf { it != formEntry.beforePrizeLimit },
                series = HistoryListDiff.diffList(
                    formEntry.beforeSeries,
                    formEntry.afterSeries
                ),
                merch = HistoryListDiff.diffList(formEntry.beforeMerch, formEntry.afterMerch),
                notes = formEntry.afterNotes.takeIf { it != formEntry.beforeNotes },
                deleted = coerceBooleanForJs(formEntry.deleted),
                timestamp = formEntry.timestamp,
            )
        }

    suspend fun loadStampRallyFormDiff(
        context: EventContext,
        dataYear: DataYear,
        artistId: Uuid,
        stampRallyId: String,
    ): StampRallyEntryDiff? = Databases.formDatabase(context)
        .stampRallyFormEntryQueries
        .getFormEntriesByStampRally(dataYear, artistId, stampRallyId)
        .awaitAsOneOrNull()
        ?.let { formEntry ->
            StampRallyEntryDiff(
                id = formEntry.stampRallyId,
                fandom = formEntry.afterFandom.orEmpty()
                    .takeIf { it != formEntry.beforeFandom.orEmpty() },
                tables = HistoryListDiff.diffList(
                    formEntry.beforeTables,
                    formEntry.afterTables
                ),
                links = HistoryListDiff.diffList(formEntry.beforeLinks, formEntry.afterLinks),
                tableMin = formEntry.afterTableMin.takeIf { it != formEntry.beforeTableMin },
                prize = formEntry.afterPrize.takeIf { it != formEntry.beforePrize },
                prizeLimit = formEntry.afterPrizeLimit.takeIf { it != formEntry.beforePrizeLimit },
                series = HistoryListDiff.diffList(
                    formEntry.beforeSeries,
                    formEntry.afterSeries
                ),
                merch = HistoryListDiff.diffList(formEntry.beforeMerch, formEntry.afterMerch),
                notes = formEntry.afterNotes.takeIf { it != formEntry.beforeNotes },
                deleted = coerceBooleanForJs(formEntry.deleted),
                timestamp = formEntry.timestamp,
            )
        }

    suspend fun loadStampRallyFormHistoryDiff(
        context: EventContext,
        dataYear: DataYear,
        artistId: Uuid,
        stampRallyId: String,
        timestamp: Instant,
    ): StampRallyEntryDiff? = Databases.formDatabase(context)
        .stampRallyFormEntryQueries
        .getFormHistoryEntry(dataYear, artistId, stampRallyId, timestamp)
        .awaitAsOneOrNull()
        ?.let { formEntry ->
            StampRallyEntryDiff(
                id = formEntry.stampRallyId,
                fandom = formEntry.afterFandom.orEmpty()
                    .takeIf { it != formEntry.beforeFandom.orEmpty() },
                tables = HistoryListDiff.diffList(
                    formEntry.beforeTables,
                    formEntry.afterTables
                ),
                links = HistoryListDiff.diffList(formEntry.beforeLinks, formEntry.afterLinks),
                tableMin = formEntry.afterTableMin.takeIf { it != formEntry.beforeTableMin },
                prize = formEntry.afterPrize.takeIf { it != formEntry.beforePrize },
                prizeLimit = formEntry.afterPrizeLimit.takeIf { it != formEntry.beforePrizeLimit },
                series = HistoryListDiff.diffList(
                    formEntry.beforeSeries,
                    formEntry.afterSeries
                ),
                merch = HistoryListDiff.diffList(formEntry.beforeMerch, formEntry.afterMerch),
                notes = formEntry.afterNotes.takeIf { it != formEntry.beforeNotes },
                deleted = coerceBooleanForJs(formEntry.deleted),
                timestamp = formEntry.timestamp,
            )
        }

    /**
     * Exposes image for local development so that it doesn't access the remote R2 bucket via the
     * public domain.
     */
    suspend fun loadImage(context: EventContext, key: String): Response {
        if (!BuildKonfig.debug) return Utils.unauthorizedResponse
        val file = context.env.ARTIST_ALLEY_IMAGES_BUCKET.get(key).await()
            ?: return Response(null, ResponseInit(status = 404))
        return Response(file.body, ResponseInit(headers = Headers().apply {
            val contentType = file.httpMetadata.contentType
            if (contentType != null) {
                set("Content-Type", contentType)
            }
        }))
    }

    suspend fun uploadImage(context: EventContext, path: String): Response {
        if (!BuildKonfig.debug) return Utils.unauthorizedResponse
        context.env.ARTIST_ALLEY_IMAGES_BUCKET
            .put(path, context.request.unsafeCast<ResponseWithBody>().body)
            .await()
        return Response("")
    }
}
