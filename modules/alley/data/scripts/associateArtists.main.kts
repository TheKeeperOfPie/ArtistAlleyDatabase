@file:DependsOn("org.apache.commons:commons-csv:1.13.0")

import org.apache.commons.csv.CSVFormat
import java.io.File
import java.net.URI
import java.util.UUID

val inputs = __FILE__.resolve("../../inputs")
    .walk()
    .filter { it.name == "artists.csv" }
    .map { it.parentFile.name to it }
    .sortedByDescending { it.first }

val artists = mutableMapOf<String, Artist>()

inputs.forEach { (parentName, input) ->
    read(input).forEach {
        val ids = if (it.isMapped("UUIDs")) {
            it["UUIDs"].lines().filter { it.isNotBlank() }
        } else {
            listOf(it["UUID"])
        }
        val names = if (it.isMapped("Artist names")) {
            it["Artist names"].split("\n\n")
                .map { it.split("\n").joinToString(separator = " / ") { it.trim() } }
        } else {
            listOf(it["Artist"])
        }
        val artist = Artist(
            sheet = parentName,
            ids = ids,
            names = names,
            booth = it["Booth"],
            links = it["Links"].split("\n")
                .filter { it.isNotBlank() }
                .map { it.trim() }
                .mapNotNull(::parseLink),
        )
        if (artist.ids.isEmpty()) {
            inferArtist(artist, "")
        } else {
            artist.ids.forEach { artistId ->
                val existingArtist = artists[artistId]
                if (artistId.isNotBlank() && existingArtist != null) {
                    artists[artistId] =
                        existingArtist.copy(links = existingArtist.links + artist.links)
                    println("Returning artist ${artist.names}")
                } else {
                    inferArtist(artist, artistId)
                }
            }
        }
    }
}

fun inferArtist(artist: Artist, artistId: String) {
    val inferred = artists.values.firstNotNullOfOrNull { existingArtist ->
        existingArtist.links.firstNotNullOfOrNull { searchingLink ->
            artist.links.filter { it.first == searchingLink.first }
                .firstNotNullOfOrNull {
                    it.takeIf { it.second == searchingLink.second }
                }
        }?.let { existingArtist to it }
    }
    val inferredArtist = inferred?.first
    val inferredLink = inferred?.second
    if (inferredArtist != null) {
        if (!artist.ids.containsAll(inferredArtist.ids)) {
            println(
                "${artist.booth} - ${artist.names} in ${artist.sheet} " +
                        "should have ID ${inferredArtist.ids}, found by $inferredLink"
            )
        }
    } else {
        artists[artistId] = artist
    }
}

fun parseLink(link: String): Pair<LinkType, String>? {
    val uri = URI.create(link)
    val path = uri.path?.removePrefix("/")?.removeSuffix("/") ?: return null
    val host = uri.host?.removePrefix("www.") ?: return null
    return when (host) {
        "bsky.app" -> LinkType.BLUESKY to path.substringAfter("profile/")
        "instagram.com" -> LinkType.INSTAGRAM to path
        "tumblr.com" -> LinkType.TUMBLR to path.removePrefix("blog/")
        "x.com", "twitter.com" -> LinkType.X to path
        else -> when {
            host.contains("bsky.social") ->
                LinkType.BLUESKY to host.substringBefore(".bsky.social")
            host.contains("tumblr.com") ->
                LinkType.TUMBLR to host.substringBefore(".tumblr.com")
            else -> null
        }
    }
}

enum class LinkType {
    BLUESKY,
    INSTAGRAM,
    TUMBLR,
    X,
}

data class Artist(
    val sheet: String,
    val ids: List<String>,
    val names: List<String>,
    val booth: String,
    val links: List<Pair<LinkType, String>>,
)

fun read(file: File) = sequence {
    file.bufferedReader().use { reader ->
        CSVFormat.RFC4180.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build()
            .parse(reader)
            .use { parser ->
                parser.forEach {
                    yield(it)
                }
            }
    }
}
