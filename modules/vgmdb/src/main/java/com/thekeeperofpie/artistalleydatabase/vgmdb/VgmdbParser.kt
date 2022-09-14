package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistEntry
import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.CssSelectable
import it.skrape.selects.DocElement
import it.skrape.selects.ElementNotFoundException
import it.skrape.selects.html5.h1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

class VgmdbParser(private val json: Json) {

    companion object {
        private const val BASE_URL = "https://vgmdb.net"
    }

    suspend fun search(query: String) = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val urlConnection = URL("$BASE_URL/search?q=$encodedQuery").openConnection()
        val text = urlConnection.getInputStream().use {
            it.bufferedReader().use {
                it.readText()
            }
        }

        val finalPath = urlConnection.url.path
        if (finalPath.startsWith("/search")) {
            parseSearchHtml(text)
        } else if (finalPath.startsWith("/album")) {
            val id = finalPath.substringAfter("/album/").substringBefore("/")
            parseAlbumHtml(id, text)?.let {
                SearchResults(
                    albums = listOf(
                        SearchResults.AlbumResult(
                            id = it.id,
                            catalogId = it.catalogId,
                            names = it.names,
                        )
                    )
                )
            }
        } else null
    }

    private fun parseSearchHtml(text: String) = htmlDocument(text) {
        val innerMain = this["#innermain"] ?: return@htmlDocument null
        val albums = mutableListOf<SearchResults.AlbumResult>()
        val artists = mutableListOf<SearchResults.ArtistResult>()
        innerMain.all("div")
            .forEach {
                when (it.attribute("id")) {
                    "albumresults" -> {
                        albums += it.all("tr")
                            .drop(1) // Skip the header
                            .mapNotNull {
                                val cells = it.all("td")
                                val catalogId = cells.getOrNull(0)?.get(".catalog")?.text
                                val info = cells.getOrNull(2)?.get("a")
                                val id = info?.attribute("href")
                                    ?.substringAfter("album/")
                                    ?: return@mapNotNull null
                                val names = parseAlbumTitles(info)
                                SearchResults.AlbumResult(
                                    id = id,
                                    catalogId = catalogId,
                                    names = names,
                                )
                            }
                    }
                    "artistresults" -> {
                        artists += it.all("tr")
                            .drop(1) // Skip the header
                            .mapNotNull {
                                val cells = it.all("td")
                                val info = cells.getOrNull(0)?.get("a")
                                val id = info?.attribute("href")
                                    ?.substringAfter("artist/")
                                    ?: return@mapNotNull null
                                SearchResults.ArtistResult(
                                    id = id,
                                    name = info.text,
                                )
                            }
                    }
                    else -> Unit
                }
            }

        SearchResults(
            albums = albums,
            artists = artists,
        )
    }

    suspend fun parseAlbum(id: String) = withContext(Dispatchers.IO) {
        skrape(BrowserFetcher) {
            request {
                url = "$BASE_URL/album/$id"
            }

            response {
                parseAlbumHtml(id, responseBody)
            }
        }
    }

    private fun parseAlbumHtml(id: String, text: String) = htmlDocument(text) {
        val innerMain = this["#innermain"] ?: return@htmlDocument null
        val names = innerMain.h1 { parseAlbumTitles(this) }

        val coverArt = innerMain.findFirst("#coverart") {
            attribute("style")
                .removePrefix("background-image: url('")
                .removeSuffix("')")
                .takeIf(String::isNotEmpty)
        }

        var catalogId: String? = null

        val infoTable = innerMain["#rightfloat", "div", "div", "table"]
        infoTable?.all("tr")
            ?.map {
                when (it["b"]?.ownText) {
                    "Catalog Number" -> {
                        val catalogData = it.findByIndex(1, "td")
                        val innerA = catalogData["a"]
                        catalogId = if (innerA?.attribute("href") == "#") {
                            innerA.ownText
                        } else {
                            catalogData.ownText.substringBefore("(alternate")
                        }
                            .trim()
                            .takeIf { it.isNotBlank() }
                    }
                }
            }

        val vocalists = mutableListOf<ArtistColumnEntry>()
        val composers = mutableListOf<ArtistColumnEntry>()

        innerMain["#collapse_credits"]?.all("tr")?.map {
            val name = it["td", "b", "span"]?.ownText
            when (name?.lowercase(Locale.ENGLISH)) {
                "vocals" -> {
                    vocalists += parseArtistCredits(it)
                }
                "composer" -> {
                    composers += parseArtistCredits(it)
                }
            }
        }

        AlbumEntry(
            id = id,
            catalogId = catalogId,
            names = names,
            coverArt = coverArt,
            vocalists = vocalists.map(json::encodeToString),
            composers = composers.map(json::encodeToString),
        )
    }

    private fun parseArtistCredits(element: DocElement) = mutableListOf<ArtistColumnEntry>().apply {
        val td = element.findByIndex(1, "td")
        td.all("a").map {
            val artistId = it.attribute("href").substringAfter("artist/")
            if (artistId.isNotBlank()) {
                val artistNames = it.all("span")
                    .associate { it.attribute("lang") to it.ownText }
                    .filter { it.key.isNotBlank() && it.value.isNotBlank() }
                    .ifEmpty { mapOf("en" to it.text) }
                this += ArtistColumnEntry(
                    id = artistId,
                    names = artistNames,
                )
            }
        }
    }

    private fun parseAlbumTitles(selectable: CssSelectable) =
        selectable.all(".albumtitle")
            .associate {
                it.attribute("lang").lowercase(Locale.getDefault()) to it.ownText
            }

    // TODO: Actually parse an artist
    fun parseArtist(id: String): ArtistEntry? = null/* withContext(Dispatchers.IO) {
        skrape(BrowserFetcher) {
            request {
                url = "$BASE_URL/artist/$id"
            }

            response {
                parseArtistHtml(id, responseBody)
            }
        }
    }

    private fun parseArtistHtml(id: String, text: String): ArtistEntry? = htmlDocument(text) {
        // TODO: Actually parse an artist
        null
    }*/

    private operator fun CssSelectable.get(vararg selector: String): DocElement? {
        return try {
            var current = this.findNullable(selector[0]) ?: return null
            selector.drop(1).forEach {
                current = current.findNullable(it) ?: return@get null
            }
            current
        } catch (e: ElementNotFoundException) {
            null
        }
    }

    private fun CssSelectable.findNullable(selector: String) = try {
        this.findFirst(selector)
    } catch (ignored: ElementNotFoundException) {
        null
    }

    private fun CssSelectable.all(selector: String) = try {
        this.findAll(selector)
    } catch (ignored: ElementNotFoundException) {
        emptyList()
    }
}