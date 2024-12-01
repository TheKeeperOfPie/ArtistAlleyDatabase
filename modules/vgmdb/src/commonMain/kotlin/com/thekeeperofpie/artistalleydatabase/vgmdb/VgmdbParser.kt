package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils_network.WebScraper
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.DiscEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.TrackEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtist
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class VgmdbParser(
    private val json: Json,
    private val httpClient: HttpClient,
    private val webScraper: WebScraper,
) {

    companion object {
        private const val BASE_URL = "https://vgmdb.net"

        private val LANGUAGE_MAPPING = mapOf(
            "English" to "en",
            "Japanese" to "ja",
            "Romaji" to "ja-latn",
        )
    }

    suspend fun search(query: String) = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = "$BASE_URL/search?q=$encodedQuery"
        // TODO: Ktor returns no response for a redirected single album this, but BrowserFetcher
        //  formats text incorrectly
        val response = httpClient.get(url)
        val responseBody = response.bodyAsText()
        val encodedPath: String
        val finalResponse: String
        if (responseBody.isBlank()) {
            val (finalUrl, scrapeResponse) = webScraper.get(url)
            encodedPath = Url(finalUrl).encodedPath
            finalResponse = scrapeResponse
        } else {
            encodedPath = response.headers["Location"]?.let { Url(it).encodedPath }
                ?: response.request.url.encodedPath
            finalResponse = responseBody
        }
        search(encodedPath, finalResponse)
    }

    fun search(finalPath: String, response: String) =
        if (finalPath.startsWith("/album") || response.contains("albumtools")) {
            val id = finalPath.substringAfter("/album/").substringBefore("/")
            parseAlbumHtml(id, response)?.let {
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
        } else if (finalPath.startsWith("/search")) {
            parseSearchHtml(response)
        } else null

    private fun parseSearchHtml(input: String): SearchResults? {
        val document = Ksoup.parse(input, BASE_URL)
        val innerMain = document.getElementById("innermain") ?: return null
        val albums = mutableListOf<SearchResults.AlbumResult>()
        val artists = mutableListOf<SearchResults.ArtistResult>()
        innerMain.getElementsByTag("div")
            .forEach {
                when (it.attribute("id")?.value) {
                    "albumresults" -> {
                        albums += it.getElementsByTag("tr")
                            .drop(1) // Skip the header
                            .mapNotNull {
                                val cells = it.getElementsByTag("td")
                                val catalogId = cells.getOrNull(0)
                                    ?.getElementsByClass("catalog")?.text()
                                val info = cells.getOrNull(2)
                                    ?.getElementsByTag("a")?.singleOrNull()
                                val id = info?.attr("href")
                                    ?.substringAfter("album/")
                                    ?: return@mapNotNull null
                                val names = parseAlbumTitles(info).orEmpty()
                                SearchResults.AlbumResult(
                                    id = id,
                                    catalogId = catalogId,
                                    names = names,
                                )
                            }
                    }
                    "artistresults" -> {
                        artists += it.getElementsByTag("tr")
                            .drop(1) // Skip the header
                            .mapNotNull {
                                val cells = it.getElementsByTag("td")
                                val info = cells.getOrNull(0)
                                    ?.getElementsByTag("a")?.singleOrNull()
                                val id = info?.attr("href")
                                    ?.substringAfter("artist/")
                                    ?: return@mapNotNull null
                                SearchResults.ArtistResult(
                                    id = id,
                                    name = info.text(),
                                )
                            }
                    }
                    else -> Unit
                }
            }

        return SearchResults(
            albums = albums,
            artists = artists,
        )
    }

    suspend fun parseAlbum(id: String) = withContext(Dispatchers.IO) {
        val response = httpClient.get("$BASE_URL/album/$id").bodyAsText()
        parseAlbumHtml(id, response)
    }

    private fun parseAlbumHtml(
        id: String,
        input: String,
    ): AlbumEntry? {
        val document = Ksoup.parse(input, BASE_URL)
        val innerMain = document.getElementById("innermain") ?: return null
        val names = parseAlbumTitles(innerMain.getElementsByTag("h1").single())

        val coverArt = innerMain.expectFirst("#coverart")
            .attr("style")
            .removePrefix("background-image: url('")
            .removeSuffix("')")
            .takeIf(String::isNotEmpty)

        var catalogId: String? = null

        val infoTable = innerMain.select("#rightfloat", "div", "div", "table")
        infoTable?.getElementsByTag("tr")
            ?.map {
                val text = it.selectFirst("b")?.ownText()
                when (text) {
                    "Catalog Number" -> {
                        val catalogData = it.getElementsByTag("td").getOrNull(1)
                        val innerA = catalogData?.getElementsByTag("a")?.singleOrNull()
                        catalogId = if (innerA?.attr("href") == "#") {
                            innerA.ownText()
                        } else {
                            catalogData?.ownText()?.substringBefore("(")
                        }
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }
                    }
                }
            }

        val performers = mutableListOf<Either<String, ArtistColumnEntry>>()
        val composers = mutableListOf<Either<String, ArtistColumnEntry>>()

        innerMain.getElementById("collapse_credits")
            ?.getElementsByTag("tr")
            ?.map {
                val name = it.select("td", "b", "span")?.ownText()
                when (name?.lowercase()) {
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
        val trackDivs =
            innerMain.getElementById("tracklist")?.parent()?.parent()?.getElementsByTag("div")
        if (trackDivs != null) {
            val languages = trackDivs.firstOrNull()?.getElementsByTag("ul")?.firstOrNull()
                ?.getElementsByTag("li")
                ?.map { it.getElementsByTag("a").firstOrNull()?.text() }
            val languageSections = trackDivs.getOrNull(2)?.getElementsByClass("tl").orEmpty()
            val firstLanguageDiscs = languageSections.firstOrNull()?.let { section ->
                val language = languages?.firstOrNull().orEmpty()
                    .substringBefore("/")
                    .let { LANGUAGE_MAPPING.getOrDefault(it, it) }
                var index = 0
                val children = section.children()
                    .filter {
                        when (it.tagName()) {
                            "span", "table" -> true
                            else -> false
                        }
                    }

                val entries = mutableListOf<TempDiskEntry>()

                while (index < children.size) {
                    val discSpanOne = children[index]
                    val tableSpanOne =
                        children.getOrNull(index + 1).takeIf { it?.tagName() == "table" }
                    val tableSpanTwo =
                        children.getOrNull(index + 2).takeIf { it?.tagName() == "table" }
                    val tableSpan = tableSpanOne ?: tableSpanTwo
                    if (tableSpan == null) {
                        index = children.size
                        continue
                    }

                    val discSpanTwo =
                        children.getOrNull(index + 1).takeIf { it?.tagName() == "span" }
                    val discNamePartOne = discSpanOne.text().substringBefore("[").trim()
                    val discNamePartTwo = discSpanTwo?.text()?.trim().orEmpty()
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
                    val discDuration = durationSpan.getElementsByTag("span")
                        .getOrNull(2)?.text().orEmpty()

                    val tracks = tableSpan.getElementsByTag("tr").map {
                        val tableData = it.getElementsByTag("td")
                        TrackEntry(
                            number = tableData.getOrNull(0)?.text()?.trim().orEmpty(),
                            titles = mapOf(
                                language to tableData.getOrNull(1)?.text()?.trim().orEmpty()
                            ),
                            duration = tableData.getOrNull(2)?.text()?.trim().orEmpty(),
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
                    it.getElementsByTag("table").map {
                        it.getElementsByTag("tr").map {
                            val tableData = it.getElementsByTag("td")
                            tableData.getOrNull(1)?.text()?.trim().orEmpty()
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

        return AlbumEntry(
            id = id.ifBlank {
                document.selectFirst("head")
                    ?.selectFirst("link")
                    ?.attr("href")
                    ?.substringAfter("album/")
                    ?.substringBefore("/")
            }.orEmpty(),
            catalogId = catalogId,
            names = names.orEmpty(),
            coverArt = coverArt,
            performers = performers.map {
                if (it is Either.Left) {
                    it.value
                } else {
                    json.encodeToString(it.rightOrNull())
                }
            },
            composers = composers.map {
                if (it is Either.Left) {
                    it.value
                } else {
                    json.encodeToString(it.rightOrNull())
                }
            },
            discs = discs.map(json::encodeToString),
        )
    }

    private fun String.removeMatchingResults(regex: Regex) = regex.findAll(this)
        .toList()
        .reversed()
        .fold(this) { stripped, result ->
            stripped.removeRange(result.range)
        }

    private fun parseArtistCredits(
        element: Element,
    ): MutableList<Either<String, ArtistColumnEntry>> {
        val allNames = element.getElementsByTag("td").getOrNull(1)?.text()
        val parenthesesRegex = Regex("""(\([^()]*\)(?=,|\z))""")
        val strippedAllNames = allNames?.removeMatchingResults(parenthesesRegex)

        val allNamesWithHoles = strippedAllNames?.split(",")
            ?.map { it.trim() }.orEmpty()

        val artistsWithIds = element.getElementsByTag("td")
            .getOrNull(1)
            ?.getElementsByTag("a")
            ?.mapNotNull {
                val artistId = it.attr("href").substringAfter("artist/")
                if (artistId.isNotBlank()) {
                    val artistNames = it.getElementsByTag("span")
                        .associate { it.attr("lang") to it.ownText() }
                        .filter { it.key.isNotBlank() && it.value.isNotBlank() }
                        .ifEmpty { mapOf("en" to it.text()) }
                    ArtistColumnEntry(
                        id = artistId,
                        names = artistNames,
                    )
                } else null
            }.orEmpty()

        val artists = mutableListOf<Either<String, ArtistColumnEntry>>()
        allNamesWithHoles.map { name ->
            val split = name.split("/").map(String::trim)
            val artist = artistsWithIds.find {
                split.any(it.names::containsValue) || it.names.containsValue(name)
            }
            artists += if (artist == null) {
                Either.Left(name)
            } else {
                Either.Right(artist)
            }
        }
        return artists
    }

    private fun parseAlbumTitles(selectable: Element?) =
        selectable?.getElementsByClass("albumtitle")
            ?.associate {
                it.attr("lang").lowercase() to it.ownText()
            }
            ?.filter { it.value.isNotBlank() }

    suspend fun parseArtist(id: String) = withContext(Dispatchers.IO) {
        val response = httpClient.get("$BASE_URL/artist/$id").bodyAsText()
        parseArtistHtml(id, response)
    }

    private fun parseArtistHtml(
        id: String,
        input: String,
    ): VgmdbArtist? {
        val document = Ksoup.parse(input, BASE_URL)
        val innerMain = document.getElementById("innermain") ?: return null
        val spans = innerMain.getElementsByTag("span")
        val name = spans.firstOrNull { it.className().isEmpty() && it.id().isEmpty() }?.text()
            ?: return null

        val leftColumn = innerMain.getElementById("leftfloat")
        val japaneseName = leftColumn?.selectFirst("span")?.text()
        val picture = leftColumn?.select("div", "a")?.attr("href")

        val names = mapOf(
            "en" to name.trim(),
            "ja" to japaneseName?.trim().orEmpty(),
        ).filterNot { it.value.isBlank() }

        return VgmdbArtist(
            id = id,
            names = names,
            picture = picture,
        )
    }

    private fun Element.select(vararg selectors: String) =
        selectors.fold(this as Element?) { element, selector ->
            element?.selectFirst(cssQuery = selector)
        }

    private data class TempDiskEntry(
        val name: String,
        val duration: String,
        val tracks: List<TrackEntry>,
    )
}
