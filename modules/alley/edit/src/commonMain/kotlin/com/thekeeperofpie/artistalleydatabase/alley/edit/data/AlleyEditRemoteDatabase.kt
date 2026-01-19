package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import io.github.vinceglb.filekit.PlatformFile
import kotlin.time.Instant
import kotlin.uuid.Uuid

expect class AlleyEditRemoteDatabase {
    suspend fun databaseCreate()

    suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistDatabaseEntry.Impl?
    suspend fun loadArtistWithFormMetadata(
        dataYear: DataYear,
        artistId: Uuid,
    ): BackendRequest.ArtistWithFormMetadata.Response?

    suspend fun loadArtistHistory(dataYear: DataYear, artistId: Uuid): List<ArtistHistoryEntry>
    suspend fun loadArtists(dataYear: DataYear): List<ArtistSummary>
    suspend fun saveArtist(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl?,
        updated: ArtistDatabaseEntry.Impl,
    ): BackendRequest.ArtistSave.Response
    suspend fun deleteArtist(
        dataYear: DataYear,
        expected: ArtistDatabaseEntry.Impl,
    ): BackendRequest.ArtistDelete.Response

    suspend fun listImages(dataYear: DataYear, artistId: Uuid): List<EditImage>
    suspend fun uploadImage(
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage

    suspend fun loadSeries(): List<SeriesInfo>
    suspend fun saveSeries(
        initial: SeriesInfo?,
        updated: SeriesInfo,
    ): BackendRequest.SeriesSave.Response
    suspend fun deleteSeries(expected: SeriesInfo): BackendRequest.SeriesDelete.Response

    suspend fun loadMerch(): List<MerchInfo>
    suspend fun saveMerch(
        initial: MerchInfo?,
        updated: MerchInfo,
    ): BackendRequest.MerchSave.Response
    suspend fun deleteMerch(expected: MerchInfo): BackendRequest.MerchDelete.Response

    suspend fun generateFormLink(
        dataYear: DataYear,
        artistId: Uuid,
        forceRegenerate: Boolean,
    ): String?

    suspend fun loadArtistFormQueue(): List<ArtistFormQueueEntry>
    suspend fun loadArtistFormHistory(): List<ArtistFormHistoryEntry>
    suspend fun loadArtistWithFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
    ): BackendRequest.ArtistWithFormEntry.Response?

    suspend fun loadArtistWithHistoricalFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
        formTimestamp: Instant,
    ): BackendRequest.ArtistWithHistoricalFormEntry.Response?

    suspend fun saveArtistAndClearFormEntry(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl,
        updated: ArtistDatabaseEntry.Impl,
        formEntryTimestamp: Instant,
    ): BackendRequest.ArtistCommitForm.Response

    suspend fun fakeArtistFormLink(): String?
    suspend fun deleteFakeArtistData()
}
