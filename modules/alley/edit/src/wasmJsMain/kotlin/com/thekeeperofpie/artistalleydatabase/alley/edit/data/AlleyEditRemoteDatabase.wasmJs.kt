package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography.generateOneTimeEncryptionKeys
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ListImages
import com.thekeeperofpie.artistalleydatabase.alley.models.network.MerchSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.SeriesSave
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.browser.window
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class AlleyEditRemoteDatabase(
    private val dispatchers: CustomDispatchers,
    private val ktorClient: HttpClient,
) {
    // TODO: Error handling
    actual suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistDatabaseEntry.Impl? =
        withContext(dispatchers.io) {
            sendRequest(BackendRequest.Artist(dataYear, artistId))
        }

    actual suspend fun loadArtistHistory(
        dataYear: DataYear,
        artistId: Uuid,
    ): List<ArtistHistoryEntry> = withContext(dispatchers.io) {
        sendRequest(BackendRequest.ArtistHistory(dataYear, artistId)).orEmpty()
    }

    actual suspend fun loadArtists(dataYear: DataYear): List<ArtistSummary> =
        withContext(dispatchers.io) {
            sendRequest(BackendRequest.Artists).orEmpty()
        }

    actual suspend fun saveArtist(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl?,
        updated: ArtistDatabaseEntry.Impl,
    ): ArtistSave.Response =
        withContext(dispatchers.io) {
            try {
                sendRequest(
                    ArtistSave.Request(
                        dataYear = dataYear,
                        initial = initial,
                        updated = updated,
                    )
                )!!
            } catch (t: Throwable) {
                t.printStackTrace()
                ArtistSave.Response.Failed(t.message.orEmpty())
            }
        }

    actual suspend fun listImages(dataYear: DataYear, artistId: Uuid): List<EditImage> =
        withContext(dispatchers.io) {
            try {
                sendRequest(
                    ListImages.Request(
                        EditImage.NetworkImage.makePrefix(
                            dataYear,
                            artistId
                        )
                    )
                )!!
                    .idsAndKeys
                    .map { imageFromIdAndKey(it.first, it.second) }
            } catch (t: Throwable) {
                t.printStackTrace()
                emptyList()
            }
        }

    actual suspend fun uploadImage(
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage = withContext(dispatchers.io) {
        val key = EditImage.NetworkImage.makePrefix(dataYear, artistId) +
                "/$id.${platformFile.extension}"
        val bytes = platformFile.readBytes()

        ktorClient.put(window.origin + "/edit/api/uploadImage/$key") {
            contentType(ContentType.Application.OctetStream)
            setBody(bytes)
        }

        imageFromIdAndKey(id, key)
    }

    // TODO: Cache this and rely on manual refresh to avoid extra row reads
    actual suspend fun loadSeries(): List<SeriesInfo> =
        withContext(dispatchers.io) {
            sendRequest(BackendRequest.Series).orEmpty()
        }

    actual suspend fun saveSeries(
        initial: SeriesInfo?,
        updated: SeriesInfo,
    ): SeriesSave.Response.Result =
        withContext(dispatchers.io) {
            try {
                sendRequest(SeriesSave.Request(initial = initial, updated = updated))!!.result
            } catch (t: Throwable) {
                t.printStackTrace()
                SeriesSave.Response.Result.Failed(t.message.orEmpty())
            }
        }

    // TODO: Cache this and rely on manual refresh to avoid extra row reads
    actual suspend fun loadMerch(): List<MerchInfo> =
        withContext(dispatchers.io) {
            sendRequest(BackendRequest.Merch).orEmpty()
        }

    actual suspend fun saveMerch(
        initial: MerchInfo?,
        updated: MerchInfo,
    ): MerchSave.Response.Result =
        withContext(dispatchers.io) {
            try {
                sendRequest(MerchSave.Request(initial = initial, updated = updated))!!.result
            } catch (t: Throwable) {
                t.printStackTrace()
                MerchSave.Response.Result.Failed(t.message.orEmpty())
            }
        }

    actual suspend fun generateFormLink(dataYear: DataYear, artistId: Uuid): String? =
        withContext(dispatchers.io) {
            val oneTimeKeys = generateOneTimeEncryptionKeys()
            try {
                val response = sendRequest(
                    BackendRequest.GenerateFormKey(
                        artistId = artistId,
                        publicKeyForResponse = oneTimeKeys.publicKey,
                    )
                ) ?: return@withContext null
                val accessKey = AlleyCryptography.oneTimeDecrypt(
                    privateKey = oneTimeKeys.privateKey,
                    payload = response,
                )
                Uri.parse(window.location.origin)
                    .buildUpon()
                    .path("/form/artist")
                    .appendQueryParameter(AlleyCryptography.ACCESS_KEY_PARAM, accessKey)
                    .build()
                    .toString()
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }

    actual suspend fun loadArtistFormQueue(): List<ArtistFormQueueEntry> =
        withContext(dispatchers.io) {
            try {
                sendRequest(BackendRequest.ArtistFormQueue) ?: emptyList()
            } catch (t: Throwable) {
                t.printStackTrace()
                emptyList()
            }
        }

    actual suspend fun loadArtistFormHistory(): List<ArtistFormHistoryEntry> =
        withContext(dispatchers.io) {
            try {
                sendRequest(BackendRequest.ArtistFormHistory) ?: emptyList()
            } catch (t: Throwable) {
                t.printStackTrace()
                emptyList()
            }
        }

    actual suspend fun loadArtistWithFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
    ): BackendRequest.ArtistWithFormEntry.Response? =
        withContext(dispatchers.io) {
            try {
                sendRequest(BackendRequest.ArtistWithFormEntry(dataYear, artistId))
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }

    actual suspend fun saveArtistAndClearFormEntry(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl,
        updated: ArtistDatabaseEntry.Impl,
        formEntryTimestamp: Instant,
    ): BackendRequest.ArtistCommitForm.Response =
        withContext(dispatchers.io) {
            try {
                sendRequest(
                    BackendRequest.ArtistCommitForm(
                        dataYear = dataYear,
                        initial = initial,
                        updated = updated,
                        formEntryTimestamp = formEntryTimestamp
                    )
                ) ?: BackendRequest.ArtistCommitForm.Response.Failed("Failed to commit form diff")
            } catch (t: Throwable) {
                t.printStackTrace()
                BackendRequest.ArtistCommitForm.Response.Failed(t.message.orEmpty())
            }
        }

    private fun imageFromIdAndKey(id: Uuid, key: String) = EditImage.NetworkImage(
        uri = Uri.parse(
            BuildKonfig.imagesUrl.ifBlank { "${window.origin}/edit/api/image" } + "/$key"
        ),
        id = id,
    )

    private suspend inline fun <reified Request, reified Response> sendRequest(
        request: Request,
    ): Response? where Request : BackendRequest, Request : BackendRequest.WithResponse<Response> {
        val response = ktorClient.post(window.origin + "/edit/api") {
            contentType(ContentType.Application.Json)
            setBody<BackendRequest>(request)
        }
        if (response.status != HttpStatusCode.OK) {
            // TODO: Surface errors
            return null
        }
        return response.body<Response>()
    }
}
