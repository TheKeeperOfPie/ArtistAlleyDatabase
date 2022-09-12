package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import it.skrape.core.htmlDocument
import it.skrape.selects.CssSelectable
import it.skrape.selects.DocElement
import it.skrape.selects.ElementNotFoundException
import it.skrape.selects.html5.h1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

class VgmdbParser {

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
        innerMain.all("div")
            .forEach {
                when (it.attribute("id")) {
                    "albumresults" -> {
                        albums += it.all("tr")
                            .drop(1) // Skip the header
                            .mapNotNull {
                                val cells = it.all("td")
                                val catalogId = cells.getOrNull(0)?.get(".catalog")?.text
                                val albumInfo = cells.getOrNull(2)?.get("a")
                                val id = albumInfo?.attribute("href")
                                    ?.substringAfter("album/")
                                    ?: return@mapNotNull null
                                val names = parseAlbumTitles(albumInfo)
                                SearchResults.AlbumResult(
                                    id = id,
                                    catalogId = catalogId,
                                    names = names,
                                )
                            }
                    }
                    else -> Unit
                }
            }

        SearchResults(
            albums = albums
        )
    }

    suspend fun parseAlbum(id: String) = withContext(Dispatchers.IO) {
        parseAlbumHtml(id, URL("$BASE_URL/album/$id").readText())
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

        AlbumEntry(
            id = id,
            catalogId = catalogId,
            names = names,
            coverArt = coverArt,
            performers = emptyList()
        )
    }

    private fun parseAlbumTitles(selectable: CssSelectable) =
        selectable.all(".albumtitle")
            .associate {
                it.attribute("lang").lowercase(Locale.getDefault()) to it.ownText
            }

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