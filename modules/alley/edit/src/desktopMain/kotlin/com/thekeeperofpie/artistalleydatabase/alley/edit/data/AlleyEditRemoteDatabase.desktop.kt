package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class AlleyEditRemoteDatabase {

    private val artistsByDataYearAndId =
        mutableMapOf<DataYear, MutableMap<String, ArtistDatabaseEntry.Impl>>()
    private val images = mutableMapOf<String, EditImage>()

    actual suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistDatabaseEntry.Impl? =
        artistsByDataYearAndId[dataYear]?.get(artistId.toString())

    actual suspend fun loadArtists(dataYear: DataYear) =
        artistsByDataYearAndId[dataYear]?.values.orEmpty().toList()
            .map { ArtistSummary(Uuid.parse(it.id), it.booth, it.name) }

    actual suspend fun saveArtist(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl?,
        updated: ArtistDatabaseEntry.Impl,
    ): ArtistSave.Response.Result {
        artistsByDataYearAndId.getOrPut(dataYear) { mutableMapOf() }[updated.id] =
            updated
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
        platformFile: PlatformFile
    ): EditImage {
        val key = EditImage.NetworkImage.makePrefix(dataYear, artistId) + "/${Uuid.random()}"
        val image = EditImage.LocalImage(platformFile, name = key)
        images[key] = image
        return image
    }
}
