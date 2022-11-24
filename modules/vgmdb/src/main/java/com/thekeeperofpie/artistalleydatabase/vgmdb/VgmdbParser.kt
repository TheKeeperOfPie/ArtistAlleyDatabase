package com.thekeeperofpie.artistalleydatabase.vgmdb

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.DiscEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.TrackEntry
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
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

class VgmdbParser(application: Application, private val json: Json) {

    companion object {
        private const val BASE_URL = "https://vgmdb.net"

        private val LANGUAGE_MAPPING = mapOf(
            "English" to "en",
            "Japanese" to "ja",
            "Romaji" to "ja-latn",
        )
    }

    private val okHttpClient =
        OkHttpClient.Builder().cache(
            Cache(
                directory = File(application.cacheDir, "vgmdb"),
                maxSize = 500L * 1024L * 1024L // 500 MiB
            )
        ).build()

    suspend fun search(query: String) = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val request = Request.Builder()
            .url("$BASE_URL/search?q=$encodedQuery")
            .build()

        val (finalPath, text) = okHttpClient.newCall(request).execute()
            .use { it.request.url.encodedPath to it.body?.string().orEmpty() }

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
                        val catalogData = it.byIndex(1, "td")
                        val innerA = catalogData?.get("a")
                        catalogId = if (innerA?.attribute("href") == "#") {
                            innerA.ownText
                        } else {
                            catalogData?.ownText?.substringBefore("(")
                        }
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }
                    }
                }
            }

        val performers = mutableListOf<ArtistColumnEntry>()
        val composers = mutableListOf<ArtistColumnEntry>()

        innerMain["#collapse_credits"]?.all("tr")?.map {
            val name = it["td", "b", "span"]?.ownText
            when (name?.lowercase(Locale.ENGLISH)) {
                "vocals", "vocalist" -> {
                    performers += parseArtistCredits(it)
                }
                "performer", "performed by" -> {
                    performers += parseArtistCredits(it)
                }
                "composer", "composed by" -> {
                    composers += parseArtistCredits(it)
                }
            }
        }

        val discs = mutableListOf<DiscEntry>()
        val trackDivs = innerMain["#tracklist"]?.parent()?.parent()?.all("div")
        if (trackDivs != null) {
            val languages = trackDivs.firstOrNull()?.get("ul")?.all("li")?.map { it["a"]?.text }
            val languageSections = trackDivs.getOrNull(1)?.all(".tl").orEmpty()
            val firstLanguageDiscs = languageSections.firstOrNull()?.let { section ->
                val language = languages?.firstOrNull().orEmpty()
                    .substringBefore("/")
                    .let { LANGUAGE_MAPPING.getOrDefault(it, it) }
                var index = 0
                val children = section.children
                    .filter {
                        when (it.tagName) {
                            "span", "table" -> true
                            else -> false
                        }
                    }

                val entries = mutableListOf<TempDiskEntry>()

                while (index < children.size) {
                    val discSpanOne = children[index]
                    val tableSpanOne =
                        children.getOrNull(index + 1).takeIf { it?.tagName == "table" }
                    val tableSpanTwo =
                        children.getOrNull(index + 2).takeIf { it?.tagName == "table" }
                    val tableSpan = tableSpanOne ?: tableSpanTwo
                    if (tableSpan == null) {
                        index = children.size
                        continue
                    }

                    val discSpanTwo = children.getOrNull(index + 1).takeIf { it?.tagName == "span" }
                    val discNamePartOne = discSpanOne.text.substringBefore("[").trim()
                    val discNamePartTwo = discSpanTwo?.text?.trim().orEmpty()
                    val discName = listOfNotNull(
                        discNamePartOne.takeIf { it.isNotBlank() },
                        discNamePartTwo.takeIf { it.isNotBlank() },
                    ).joinToString(separator = " ")

                    val durationIndex = if (tableSpanOne != null) index + 2 else index + 3
                    val durationSpan = children.getOrNull(durationIndex)
                    if (durationSpan == null) {
                        index = children.size
                        continue
                    }
                    val discDuration = durationSpan.byIndex(1, "span")?.text.orEmpty()

                    val tracks = tableSpan.all("tr").map {
                        val tableData = it.all("td")
                        TrackEntry(
                            number = tableData.getOrNull(0)?.text?.trim().orEmpty(),
                            titles = mapOf(
                                language to tableData.getOrNull(1)?.text?.trim().orEmpty()
                            ),
                            duration = tableData.getOrNull(2)?.text?.trim().orEmpty(),
                        )
                    }

                    entries += TempDiskEntry(
                        name = discName,
                        duration = discDuration,
                        tracks = tracks,
                    )

                    index = durationIndex + 1
                }

                entries
            }.orEmpty()

            val languageDiscsTitles: List<List<List<String>>> = languageSections.drop(1)
                .map {
                    it.all("table").map {
                        it.all("tr").map {
                            val tableData = it.all("td")
                            tableData.getOrNull(1)?.text?.trim().orEmpty()
                        }
                    }
                }

            discs += firstLanguageDiscs
                .mapIndexed { discIndex, disc ->
                    disc.copy(tracks = disc.tracks.mapIndexed { trackIndex, track ->
                        val firstLanguage = track.titles.map { it.key to it.value }
                        val otherLanguages = languageDiscsTitles
                            .mapIndexedNotNull { languageIndex, value ->
                                val language = languages?.get(languageIndex + 1).orEmpty()
                                    .let { LANGUAGE_MAPPING.getOrDefault(it, it) }
                                val trackName = value.getOrNull(discIndex)
                                    ?.getOrNull(trackIndex).orEmpty()
                                (language to trackName).takeUnless {
                                    language.isEmpty() || trackName.isEmpty()
                                }
                            }
                        val allLanguages = (firstLanguage + otherLanguages)
                            .distinctBy { it.second }
                            .associate { it }
                        track.copy(titles = allLanguages)
                    })
                }
                .map { DiscEntry(it.name, it.duration, it.tracks.map(json::encodeToString)) }
        }

        AlbumEntry(
            id = id,
            catalogId = catalogId,
            names = names,
            coverArt = coverArt,
            performers = performers.map(json::encodeToString),
            composers = composers.map(json::encodeToString),
            discs = discs.map(json::encodeToString),
        )
    }

    private fun String.removeMatchingResults(regex: Regex) = regex.findAll(this)
        .toList()
        .reversed()
        .fold(this) { stripped, result ->
            stripped.removeRange(result.range)
        }

    private fun parseArtistCredits(element: DocElement): MutableList<ArtistColumnEntry> {
        val allNames = element.byIndex(1, "td")?.text
        val parenthesesRegex = Regex("""(\([^()]*\)(?=,|\z))""")
        val strippedAllNames = allNames?.removeMatchingResults(parenthesesRegex)

        val allNamesWithHoles = strippedAllNames?.split(",")
            ?.map { it.trim() }.orEmpty()

        val artistsWithIds = element.byIndex(1, "td")?.all("a")?.mapNotNull {
            val artistId = it.attribute("href").substringAfter("artist/")
            if (artistId.isNotBlank()) {
                val artistNames = it.all("span")
                    .associate { it.attribute("lang") to it.ownText }
                    .filter { it.key.isNotBlank() && it.value.isNotBlank() }
                    .ifEmpty { mapOf("en" to it.text) }
                ArtistColumnEntry(
                    id = artistId,
                    names = artistNames,
                )
            } else null
        }.orEmpty()

        val artists = mutableListOf<ArtistColumnEntry>()
        allNamesWithHoles.map { name ->
            val split = name.split("/").map(String::trim)
            artists += artistsWithIds.find { split.any(it.names::containsValue) }
                ?: ArtistColumnEntry(id = null, names = mapOf("unknown" to name))
        }
        return artists
    }

    private fun parseAlbumTitles(selectable: CssSelectable) =
        selectable.all(".albumtitle")
            .associate {
                it.attribute("lang").lowercase(Locale.getDefault()) to it.ownText
            }

    suspend fun parseArtist(id: String) = withContext(Dispatchers.IO) {
        skrape(BrowserFetcher) {
            request {
                url = "$BASE_URL/artist/$id"
            }

            response {
                parseArtistHtml(id, responseBody)
            }
        }
    }

    private fun parseArtistHtml(id: String, text: String) = htmlDocument(text) {
        val innerMain = findNullable("#innermain") ?: return@htmlDocument null
        val spans = innerMain.all("span")
        val name = spans.firstOrNull { it.className.isEmpty() && it.id.isEmpty() }?.text
            ?: return@htmlDocument null

        val leftAndRight = innerMain["div"]?.all("div")
        val leftColumn = leftAndRight?.getOrNull(0) ?: return@htmlDocument null

        val japaneseName = leftColumn["span"]?.text

        val picture = leftColumn["div", "a"]?.attribute("href")

        val names = mapOf(
            "en" to name.trim(),
            "ja" to japaneseName?.trim().orEmpty(),
        ).filterNot { it.value.isBlank() }

        ArtistEntry(
            id = id,
            names = names,
            picture = picture
        )
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

    private fun CssSelectable.byIndex(index: Int, selector: String) = try {
        this.findByIndex(index, selector)
    } catch (ignored: ElementNotFoundException) {
        null
    }

    private fun DocElement.parent() = try {
        this.parent
    } catch (ignore: ElementNotFoundException) {
        null
    }

    private data class TempDiskEntry(
        val name: String,
        val duration: String,
        val tracks: List<TrackEntry>
    )
}