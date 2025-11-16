package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ListImages
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.browser.window
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class AlleyEditRemoteDatabase(
    private val ktorClient: HttpClient,
) {
    // TODO: Error handling
    actual suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistDatabaseEntry.Impl? =
        withContext(PlatformDispatchers.IO) {
            val response = ktorClient.get(window.origin + "/database/artist/$artistId")
            if (response.status != HttpStatusCode.OK) return@withContext null
            response.body<ArtistDatabaseEntry.Impl>()
        }

    actual suspend fun loadArtists(dataYear: DataYear): List<ArtistSummary> =
        withContext(PlatformDispatchers.IO) {
            try {
                ktorClient.get(window.origin + "/database/artists")
                    .body<List<ArtistSummary>>()
            } catch (_: Throwable) {
                // TODO
                emptyList()
            }
        }

    actual suspend fun saveArtist(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl?,
        updated: ArtistDatabaseEntry.Impl,
    ): ArtistSave.Response.Result =
        withContext(PlatformDispatchers.IO) {
            try {
                ktorClient.put(window.origin + "/database/insertArtist") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        ArtistSave.Request(
                            initial = initial,
                            updated = updated,
                        )
                    )
                }
                    .body<ArtistSave.Response>()
                    .result
            } catch (t: Throwable) {
                t.printStackTrace()
                ArtistSave.Response.Result.Failed(t)
            }
        }

    actual suspend fun listImages(dataYear: DataYear, artistId: Uuid): List<EditImage> =
        withContext(PlatformDispatchers.IO) {
            try {
                ktorClient.post(window.origin + "/database/listImages") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        ListImages.Request(EditImage.NetworkImage.makePrefix(dataYear, artistId))
                    )
                }
                    .body<ListImages.Response>()
                    .keys
                    .map { EditImage.NetworkImage(Uri.parse(window.origin + "/database/image/$it")) }
            } catch (t: Throwable) {
                t.printStackTrace()
                emptyList()
            }
        }

    actual suspend fun uploadImage(
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
    ): EditImage = withContext(PlatformDispatchers.IO) {
        val name = Uuid.random().toString()
        val key = EditImage.NetworkImage.makePrefix(dataYear, artistId) + "/$name"
        val bytes = platformFile.readBytes()
        ktorClient.put(window.origin + "/database/uploadImage/$key") {
            contentType(ContentType.Application.OctetStream)
            setBody(bytes)
        }
        EditImage.NetworkImage(Uri.parse(window.origin + "/database/image/$key"))
    }
}
