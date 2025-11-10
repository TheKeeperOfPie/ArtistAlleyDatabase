@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class)

import app.cash.sqldelight.async.coroutines.awaitCreate
import com.thekeeperofpie.artistalleydatabase.alley.functions.ArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.functions.Database
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistDatabaseEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.w3c.fetch.Response
import kotlin.js.Promise

@JsExport
class Worker {
    companion object {
        @JsStatic
        fun request(context: EventContext<Env>): Promise<Response> {
            val functionPath = context.functionPath.orEmpty()
            return when {
                functionPath.endsWith("insertArtist") -> insertArtist(context)
                else -> promise { Response.error() }
            }
        }

        fun insertArtist(context: EventContext<Env>) = promise {
            val sqlDriver = WorkerSqlDriver(database = context.env.ANIME_EXPO_2026_DB)
            val database = Database(
                sqlDriver, ArtistEntryAnimeExpo2026.Adapter(
                    linksAdapter = ColumnAdapters.listStringAdapter,
                    storeLinksAdapter = ColumnAdapters.listStringAdapter,
                    catalogLinksAdapter = ColumnAdapters.listStringAdapter,
                    commissionsAdapter = ColumnAdapters.listStringAdapter,
                    seriesInferredAdapter = ColumnAdapters.listStringAdapter,
                    seriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
                    merchInferredAdapter = ColumnAdapters.listStringAdapter,
                    merchConfirmedAdapter = ColumnAdapters.listStringAdapter,
                    imagesAdapter = ColumnAdapters.listCatalogImageAdapter,
                )
            )
            Database.Schema.awaitCreate(sqlDriver)
            val artist = Json.decodeFromString<ArtistDatabaseEntry.Impl>(context.request.text().await())
            database.artistEntryAnimeExpo2026Queries.insertArtist(
                ArtistEntryAnimeExpo2026(
                    id = artist.id,
                    booth = artist.booth,
                    name = artist.name,
                    summary = artist.summary,
                    links = artist.links,
                    storeLinks = artist.storeLinks,
                    catalogLinks = artist.catalogLinks,
                    linkFlags = 0,
                    linkFlags2 = 0,
                    driveLink = artist.driveLink,
                    notes = artist.notes,
                    commissions = artist.commissions,
                    commissionFlags = 0,
                    seriesInferred = artist.seriesInferred,
                    seriesConfirmed = artist.seriesConfirmed,
                    merchInferred = artist.merchInferred,
                    merchConfirmed = artist.merchConfirmed,
                    images = artist.images,
                    counter = artist.counter,
                )
            )
            Response("")
        }
    }
}

private fun <T> promise(block: suspend CoroutineScope.() -> T): Promise<T> {
    return Promise { resolve, reject ->
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                resolve(block())
            } catch (t: Throwable) {
                reject(t)
            }
        }
    }
}
