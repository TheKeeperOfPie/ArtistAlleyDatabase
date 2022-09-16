package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistEntry
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class ParserTest {

    private val json = Json {
        isLenient = true
        prettyPrint = true
    }

    private val parser = VgmdbParser(json)

    @Test
    fun searchAlbum() {
        val actual = runBlocking { parser.search("AICL-393") }?.albums.orEmpty()
        assertWithMessage(actual.joinToString("\n"))
            .that(actual)
            .containsAtLeast(
                SearchResults.AlbumResult(
                    id = "101937",
                    catalogId = "AICL-3935~6",
                    names = mapOf(
                        "en" to "Higher's High / Akari Nanawo [Limited Edition]",
                        "ja" to "Higher's High / ナナヲアカリ [初回限定盤]",
                        "ja-latn" to "Higher's High / Akari Nanawo [Limited Edition]"
                    ),
                ),
                SearchResults.AlbumResult(
                    id = "101938",
                    catalogId = "AICL-3938~9",
                    names = mapOf(
                        "en" to "Higher's High / Akari Nanawo [Limited Edition]",
                        "ja" to "Higher's High / ナナヲアカリ [期間生産限定盤]",
                        "ja-latn" to "Higher's High / Akari Nanawo [Limited Edition]"
                    ),
                ),
            )
    }

    @Test
    fun searchOneAlbum() {
        val actual = runBlocking { parser.search("AICL-3938~9") }
        assertThat(actual).isEqualTo(
            SearchResults(
                albums = listOf(
                    SearchResults.AlbumResult(
                        id = "101938",
                        catalogId = "AICL-3938~9",
                        names = mapOf(
                            "en" to "Higher's High / Akari Nanawo [Limited Edition]",
                            "ja" to "Higher's High / ナナヲアカリ [期間生産限定盤]",
                            "ja-latn" to "Higher's High / Akari Nanawo [Limited Edition]"
                        ),
                    )
                )
            )
        )
    }

    @Test
    fun searchArtist() {
        val actual = runBlocking { parser.search("Honey") }?.artists.orEmpty()
        assertWithMessage(actual.joinToString("\n"))
            .that(actual)
            .contains(
                SearchResults.ArtistResult(
                    id = "14655",
                    name = "HoneyWorks"
                )
            )
    }

    @Test
    fun parseAlbum() {
        val actual = runBlocking { parser.parseAlbum("101938") }
        assertThat(actual).isEqualTo(
            AlbumEntry(
                id = "101938",
                catalogId = "AICL-3938~9",
                names = mapOf(
                    "en" to "Higher's High / Akari Nanawo [Limited Edition]",
                    "ja" to "Higher's High / ナナヲアカリ [期間生産限定盤]",
                    "ja-latn" to "Higher's High / Akari Nanawo [Limited Edition]"
                ),
                coverArt = "https://medium-media.vgm.io/albums/83/101938/101938-643d29f79e54.jpg",
                vocalists= listOf(
                    ArtistColumnEntry(
                    id = "29051",
                    names = mapOf(
                        "en" to "Akari Nanawo",
                        "ja" to  "ナナヲアカリ",
                    ),
                )).map(json::encodeToString),
                composers = listOf(
                    ArtistColumnEntry(
                        id = "29243",
                        names = mapOf(
                            "en" to "Seiji Nayuta",
                            "ja" to  "ナユタセイジ",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "9559",
                        names = mapOf(
                            "en" to "DECO*27",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "35178",
                        names = mapOf(
                            "en" to "LASTorder",
                        )
                    ),
                ).map(json::encodeToString),
            )
        )
    }

    @Test
    fun parseArtist() {
        val actual = runBlocking { parser.parseArtist("29051") }
        assertThat(actual).isEqualTo(
            ArtistEntry(
                id = "29051",
                names = mapOf(
                    "en" to "Akari Nanawo",
                    "ja" to "ナナヲ アカリ",
                ),
                picture = "https://media.vgm.io/artists/15/29051/29051-1527082407.jpg"
            )
        )
    }
}