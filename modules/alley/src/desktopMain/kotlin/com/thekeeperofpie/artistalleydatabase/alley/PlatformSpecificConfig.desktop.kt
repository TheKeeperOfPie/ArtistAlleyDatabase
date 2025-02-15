package com.thekeeperofpie.artistalleydatabase.alley

import org.apache.commons.csv.CSVFormat
import java.io.File
import java.net.URI
import java.util.UUID

actual object PlatformSpecificConfig {
    actual val type = PlatformType.DESKTOP
    actual val defaultPageSize = 50
    actual val showPagingButtons = true
    actual val scrollbarsAlwaysVisible = true
}

fun test() {
    val inputs = File("").resolve("../../inputs")
        .walk()
        .filter { it.name == "artists.csv" }
        .map { "" to  it }
    val artists = mutableMapOf<String, Artist>()

    inputs.forEach { (parentName, input) ->
        read(input).forEach {
            val artist = Artist(
                sheet = parentName,
                id = it["UUID"]!!,
                name = it["Artist"]!!,
                links = it["Links"]!!.split("\n")
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
                    .mapNotNull(::parseLink),
            )
            val artistId = artist.id
            val existingArtist = artists[artistId]
            if (existingArtist != null) {
                artists[artistId] = existingArtist.copy(links = existingArtist.links + artist.links)
            } else {
                val inferred = artists.values.firstNotNullOfOrNull { existingArtist ->
                    existingArtist.links.firstNotNullOfOrNull { searchingLink ->
                        artist.links.filter { it.first == searchingLink.first }
                            .firstNotNullOfOrNull {
                                it.takeIf { it.second == searchingLink.second }
                            }?.let { searchingLink to it }
                    }?.let { existingArtist to it }
                }
                val inferredArtist = inferred?.first
                val inferredLink = inferred?.second
                if (inferredArtist != null) {
                    println("${artist.name} in ${artist.sheet} should have ID ${inferredArtist.id}, found by $inferredLink")
                } else {
                    artists[artist.id] = artist
                }
            }
        }
    }
}

fun parseLink(link: String): Pair<LinkType, String>? {
    val uri = URI.create(link)
    val path = uri.path?.removePrefix("/")?.removeSuffix("/") ?: return null
    val host = uri.host?.removePrefix("www.") ?: return null
    return when (host) {
        "bsky.app" -> LinkType.BLUESKY to path.substringAfter("profile/")
        "instagram.com" -> LinkType.INSTAGRAM to path
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
    val id: String,
    val name: String,
    val links: List<Pair<LinkType, String>>,
)

fun read(file: File) = sequence {
    file.bufferedReader().use { reader ->
        CSVFormat.RFC4180.parse(reader).use { parser ->
            parser.forEach {
                yield(it)
            }
        }
    }
}
