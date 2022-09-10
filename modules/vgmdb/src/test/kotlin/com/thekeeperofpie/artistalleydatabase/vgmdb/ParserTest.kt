package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ParserTest {

    @Test
    fun searchAlbum() {
        val actual = runBlocking { VgmdbParser().search("AICL-393") }?.albums.orEmpty()
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
        val actual = runBlocking { VgmdbParser().search("AICL-3938~9") }
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
    fun parseAlbum() {
        val actual = runBlocking { VgmdbParser().parseAlbum("101938") }
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
                performers = emptyList(),
            )
        )
    }
}