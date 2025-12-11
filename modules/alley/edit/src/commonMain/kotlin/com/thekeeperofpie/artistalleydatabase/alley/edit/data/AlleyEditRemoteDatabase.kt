package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.MerchSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.SeriesSave
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import io.github.vinceglb.filekit.PlatformFile
import kotlin.uuid.Uuid

expect class AlleyEditRemoteDatabase {
    suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistDatabaseEntry.Impl?
    suspend fun loadArtistHistory(dataYear: DataYear, artistId: Uuid): List<ArtistHistoryEntry>
    suspend fun loadArtists(dataYear: DataYear): List<ArtistSummary>
    suspend fun saveArtist(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl?,
        updated: ArtistDatabaseEntry.Impl,
    ): ArtistSave.Response.Result

    suspend fun listImages(dataYear: DataYear, artistId: Uuid): List<EditImage>
    suspend fun uploadImage(
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage

    suspend fun loadSeries(): List<SeriesInfo>
    suspend fun saveSeries(initial: SeriesInfo?, updated: SeriesInfo): SeriesSave.Response.Result

    suspend fun loadMerch(): List<MerchInfo>
    suspend fun saveMerch(initial: MerchInfo?, updated: MerchInfo): MerchSave.Response.Result
}
