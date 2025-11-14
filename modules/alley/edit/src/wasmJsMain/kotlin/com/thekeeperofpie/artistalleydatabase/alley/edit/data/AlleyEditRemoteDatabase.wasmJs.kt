package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
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
    actual suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistEditInfo? =
        withContext(PlatformDispatchers.IO) {
            val response = ktorClient.get(window.origin + "/database/artist/$artistId")
            if (response.status != HttpStatusCode.OK) return@withContext null
            val artist = response.body<ArtistDatabaseEntry.Impl>()
            ArtistEditInfo(
                id = Uuid.parse(artist.id),
                booth = artist.booth,
                name = artist.name,
                summary = artist.summary,
                links = artist.links,
                storeLinks = artist.storeLinks,
                catalogLinks = artist.catalogLinks,
                notes = artist.notes,
                commissions = artist.commissions,
                seriesInferred = artist.seriesInferred,
                seriesConfirmed = artist.seriesConfirmed,
                merchInferred = artist.merchInferred,
                merchConfirmed = artist.merchConfirmed,
            )
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

    actual suspend fun saveArtist(dataYear: DataYear, artist: ArtistDatabaseEntry.Impl): Unit =
        withContext(PlatformDispatchers.IO) {
            ktorClient.put(window.origin + "/database/insertArtist") {
                contentType(ContentType.Application.Json)
                setBody(artist)
            }
        }
}
