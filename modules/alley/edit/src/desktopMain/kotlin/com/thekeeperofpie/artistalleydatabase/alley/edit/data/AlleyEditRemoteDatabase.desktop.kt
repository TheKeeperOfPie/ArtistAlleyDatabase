package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageKey
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.MerchSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.SeriesSave
import com.thekeeperofpie.artistalleydatabase.alley.models.toArtistSummary
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class AlleyEditRemoteDatabase {

    private val artistsByDataYearAndId =
        mutableMapOf<DataYear, MutableMap<String, ArtistDatabaseEntry.Impl>>()
    private val artistHistoryByDataYearAndId =
        mutableMapOf<DataYear, MutableMap<String, MutableList<ArtistHistoryEntry>>>()
    private val images = mutableMapOf<String, EditImage>()

    private val series = mutableMapOf<Uuid, SeriesInfo>()
    private val merch = mutableMapOf<Uuid, MerchInfo>()

    actual suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistDatabaseEntry.Impl? =
        artistsByDataYearAndId[dataYear]?.get(artistId.toString())

    actual suspend fun loadArtistHistory(
        dataYear: DataYear,
        artistId: Uuid,
    ): List<ArtistHistoryEntry> =
        artistHistoryByDataYearAndId[dataYear]?.get(artistId.toString()).orEmpty()
            .sortedByDescending { it.lastEditTime }

    actual suspend fun loadArtists(dataYear: DataYear) =
        artistsByDataYearAndId[dataYear]?.values.orEmpty().toList().map { it.toArtistSummary() }

    actual suspend fun saveArtist(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl?,
        updated: ArtistDatabaseEntry.Impl,
    ): ArtistSave.Response.Result {
        simulateLatency()
        val oldArtist = loadArtist(dataYear, Uuid.parse(updated.id))
        if (oldArtist != null) {
            val historyEntry = ArtistHistoryEntry(
                status = updated.status.takeIf { it != oldArtist.status },
                booth = updated.booth.takeIf { it != oldArtist.booth }
                    ?.ifBlank { null },
                name = updated.name.takeIf { it != oldArtist.name }
                    ?.ifBlank { null },
                summary = updated.summary.takeIf { it != oldArtist.summary }
                    ?.ifBlank { null },
                links = updated.links.takeIf { it != oldArtist.links }
                    ?.ifEmpty { null },
                storeLinks = updated.storeLinks.takeIf { it != oldArtist.storeLinks }
                    ?.ifEmpty { null },
                catalogLinks = updated.catalogLinks.takeIf { it != oldArtist.catalogLinks }
                    ?.ifEmpty { null },
                notes = updated.notes.takeIf { it != oldArtist.notes }
                    ?.ifBlank { null },
                commissions = updated.commissions.takeIf { it != oldArtist.commissions }
                    ?.ifEmpty { null },
                seriesInferred = updated.seriesInferred.takeIf { it != oldArtist.seriesInferred }
                    ?.ifEmpty { null },
                seriesConfirmed = updated.seriesConfirmed.takeIf { it != oldArtist.seriesConfirmed }
                    ?.ifEmpty { null },
                merchInferred = updated.merchInferred.takeIf { it != oldArtist.merchInferred }
                    ?.ifEmpty { null },
                merchConfirmed = updated.merchConfirmed.takeIf { it != oldArtist.merchConfirmed }
                    ?.ifEmpty { null },
                images = updated.images.takeIf { it != oldArtist.images }
                    ?.ifEmpty { null },
                editorNotes = updated.editorNotes.takeIf { it != oldArtist.editorNotes }
                    ?.ifBlank { null },
                lastEditor = updated.lastEditor.takeIf { it != oldArtist.lastEditor }
                    ?.ifBlank { null },
                lastEditTime = updated.lastEditTime ?: Clock.System.now(),
            )
            artistHistoryByDataYearAndId.getOrPut(dataYear) { mutableMapOf() }
                .getOrPut(updated.id) { mutableListOf() }
                .add(historyEntry)
        }
        artistsByDataYearAndId.getOrPut(dataYear) { mutableMapOf() }[updated.id] = updated
        return ArtistSave.Response.Result.Success
    }

    actual suspend fun listImages(
        dataYear: DataYear,
        artistId: Uuid,
    ): List<EditImage> {
        val prefix = EditImage.NetworkImage.makePrefix(dataYear, artistId)
        return images.entries.filter { it.key.startsWith(prefix) }.map { it.value }
    }

    actual suspend fun uploadImage(
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage {
        simulateLatency()
        val prefix = EditImage.NetworkImage.makePrefix(dataYear, artistId)
        val key = "$prefix/$id.${platformFile.extension}"
        val imageKey = PlatformImageKey(id)
            .takeIf { PlatformImageCache[it] != null }
            ?: PlatformImageCache.add(platformFile)
        val image = EditImage.LocalImage(imageKey, name = key)
        images[key] = image
        return image
    }

    actual suspend fun loadSeries(): List<SeriesInfo> = series.values.toList()

    actual suspend fun saveSeries(
        initial: SeriesInfo?,
        updated: SeriesInfo,
    ): SeriesSave.Response.Result {
        simulateLatency()
        val existing = initial?.uuid?.let { series[it] }
        if (existing != null && existing != initial) {
            return SeriesSave.Response.Result.Outdated(existing)
        }
        series[updated.uuid] = updated
        return SeriesSave.Response.Result.Success
    }

    actual suspend fun loadMerch(): List<MerchInfo> = merch.values.toList()

    actual suspend fun saveMerch(
        initial: MerchInfo?,
        updated: MerchInfo,
    ): MerchSave.Response.Result {
        simulateLatency()
        val existing = initial?.uuid?.let { merch[it] }
        if (existing != null && existing != initial) {
            return MerchSave.Response.Result.Outdated(existing)
        }
        merch[updated.uuid] = updated
        return MerchSave.Response.Result.Success
    }

    private suspend fun simulateLatency() = delay(5.seconds)
}
