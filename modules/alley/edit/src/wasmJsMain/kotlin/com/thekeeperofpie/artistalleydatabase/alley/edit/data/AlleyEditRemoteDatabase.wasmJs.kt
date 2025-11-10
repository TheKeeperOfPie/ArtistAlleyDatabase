package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.browser.window
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@SingleIn(AppScope::class)
@Inject
actual class AlleyEditRemoteDatabase(
    private val ktorClient: HttpClient,
) {
    actual suspend fun saveArtist(artist: ArtistDatabaseEntry.Impl) = withContext(PlatformDispatchers.IO) {
        val response = ktorClient.put(window.origin + "/database/insertArtist"){
            setBody(Json.encodeToString(artist))
        }
        ConsoleLogger.log("insertArtist: ${response.bodyAsText()}")
    }
}
