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
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ListImages
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
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
    actual suspend fun databaseCreate() {
        sendRequest(BackendRequest.DatabaseCreate)
    }

    // TODO: Error handling
    actual suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistDatabaseEntry.Impl? =
        withContext(dispatchers.io) {
            sendRequest(BackendRequest.Artist(dataYear, artistId))
        }

    actual suspend fun loadArtistWithFormMetadata(
        dataYear: DataYear,
        artistId: Uuid,
    ): BackendRequest.ArtistWithFormMetadata.Response? =
        withContext(dispatchers.io) {
            sendRequest(BackendRequest.ArtistWithFormMetadata(dataYear, artistId))
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
    ): BackendRequest.ArtistSave.Response =
        withContext(dispatchers.io) {
            try {
                sendRequest(
                    BackendRequest.ArtistSave(
                        dataYear = dataYear,
                        initial = initial,
                        updated = updated,
                    )
                )!!
            } catch (t: Throwable) {
                t.printStackTrace()
                BackendRequest.ArtistSave.Response.Failed(t.message.orEmpty())
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
    ): BackendRequest.SeriesSave.Response =
        withContext(dispatchers.io) {
            try {
                sendRequest(BackendRequest.SeriesSave(initial = initial, updated = updated))!!
            } catch (t: Throwable) {
                t.printStackTrace()
                BackendRequest.SeriesSave.Response.Failed(t.message.orEmpty())
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
    ): BackendRequest.MerchSave.Response =
        withContext(dispatchers.io) {
            try {
                sendRequest(BackendRequest.MerchSave(initial = initial, updated = updated))!!
            } catch (t: Throwable) {
                t.printStackTrace()
                BackendRequest.MerchSave.Response.Failed(t.message.orEmpty())
            }
        }

    actual suspend fun generateFormLink(
        dataYear: DataYear,
        artistId: Uuid,
        forceRegenerate: Boolean,
    ): String? =
        withContext(dispatchers.io) {
            val oneTimeKeys = generateOneTimeEncryptionKeys()
            try {
                val response = sendRequest(
                    BackendRequest.GenerateFormKey(
                        artistId = artistId,
                        publicKeyForResponse = oneTimeKeys.publicKey,
                        forceRegenerate = forceRegenerate,
                    )
                ) ?: return@withContext null
                val accessKey = AlleyCryptography.oneTimeDecrypt(
                    privateKey = oneTimeKeys.privateKey,
                    payload = response,
                )

                formLink(accessKey)
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

    actual suspend fun loadArtistWithHistoricalFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
        formTimestamp: Instant,
    ): BackendRequest.ArtistWithHistoricalFormEntry.Response? =
        withContext(dispatchers.io) {
            try {
                sendRequest(
                    BackendRequest.ArtistWithHistoricalFormEntry(
                        dataYear = dataYear,
                        artistId = artistId,
                        formTimestamp = formTimestamp,
                    )
                )
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

    actual suspend fun deleteArtist(
        dataYear: DataYear,
        expected: ArtistDatabaseEntry.Impl,
    ): BackendRequest.ArtistDelete.Response =
        withContext(dispatchers.io) {
            try {
                sendRequest(BackendRequest.ArtistDelete(dataYear, expected))!!
            } catch (t: Throwable) {
                t.printStackTrace()
                BackendRequest.ArtistDelete.Response.Failed(t.message.orEmpty())
            }
        }

    actual suspend fun fakeArtistFormLink(): String? =
        withContext(dispatchers.io) {
            val oneTimeKeys = generateOneTimeEncryptionKeys()
            try {
                val response = sendRequest(
                    BackendRequest.FakeArtistData(
                        publicKeyForResponse = oneTimeKeys.publicKey,
                    )
                ) ?: return@withContext null
                val accessKey = AlleyCryptography.oneTimeDecrypt(
                    privateKey = oneTimeKeys.privateKey,
                    payload = response,
                )
                formLink(accessKey)
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }

    actual suspend fun deleteFakeArtistData() {
        withContext(dispatchers.io) {
            sendRequest(BackendRequest.DeleteFakeArtistData)
        }
    }

    private fun imageFromIdAndKey(id: Uuid, key: String) = EditImage.NetworkImage(
        uri = Uri.parse(
            BuildKonfig.imagesUrl.ifBlank { "${window.origin}/edit/api/image" } + "/$key"
        ),
        id = id,
    )

    private fun formLink(accessKey: String): String =
        Uri.parse(if (BuildKonfig.isWasmDebug) window.location.origin else AlleyUtils.formUrl)
            .buildUpon()
            .path("/form/artist")
            .appendQueryParameter(AlleyCryptography.ACCESS_KEY_PARAM, accessKey)
            .build()
            .toString()

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
