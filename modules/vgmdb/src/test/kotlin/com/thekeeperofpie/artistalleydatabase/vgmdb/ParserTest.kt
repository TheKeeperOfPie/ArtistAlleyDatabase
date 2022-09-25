package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.DiscEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.TrackEntry
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
                performers = listOf(
                    ArtistColumnEntry(
                        id = "29051",
                        names = mapOf(
                            "en" to "Akari Nanawo",
                            "ja" to "ナナヲアカリ",
                        ),
                    )
                ).map(json::encodeToString),
                composers = listOf(
                    ArtistColumnEntry(
                        id = "29243",
                        names = mapOf(
                            "en" to "Seiji Nayuta",
                            "ja" to "ナユタセイジ",
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
                discs = listOf(
                    DiscEntry(
                        name = "Disc 1 (CD) Vocal",
                        duration = "13:03",
                        tracks = listOf(
                            TrackEntry(
                                number = "01",
                                titles = mapOf("en" to "Higher's High"),
                                duration = "3:32",
                            ),
                            TrackEntry(
                                number = "02",
                                titles = mapOf(
                                    "en" to "Cry Song",
                                    "ja" to "クライソング",
                                ),
                                duration = "3:46",
                            ),
                            TrackEntry(
                                number = "03",
                                titles = mapOf(
                                    "en" to "Drama",
                                    "ja" to "ドラマ",
                                ),
                                duration = "4:13",
                            ),
                            TrackEntry(
                                number = "04",
                                titles = mapOf("en" to "Higher's High (TV Size ver.)"),
                                duration = "1:32",
                            ),
                        ).map(json::encodeToString)
                    ),
                    DiscEntry(
                        name = "Disc 2 (Blu-ray) Vocal, Video",
                        duration = "3:38",
                        tracks = listOf(
                            TrackEntry(
                                number = "01",
                                titles = mapOf(
                                    "en" to """TV Anime "Senyoku no Sigrdrifa" × "Higher's High" Special Movie""",
                                    "ja" to "TVアニメ「戦翼のシグルドリーヴァ」×「Higher's High」スペシャルムービー",
                                ),
                                duration = "3:38",
                            ),
                        ).map(json::encodeToString)
                    ),
                ).map(json::encodeToString),
            )
        )
    }

    @Test
    fun parseAlbum2() {
        val actual = runBlocking { parser.parseAlbum("46010") }
        assertThat(actual).isEqualTo(
            AlbumEntry(
                id = "46010",
                catalogId = "AOA-5101-CD",
                names = mapOf(
                    "en" to "Anohana -The Flower We Saw That Day The Movie- Compilation Music CD",
                    "ja" to "Anohana -The Flower We Saw That Day The Movie- Compilation Music CD",
                    "ja-latn" to "Anohana -The Flower We Saw That Day The Movie- Compilation Music CD",
                ),
                coverArt = "https://medium-media.vgm.io/albums/01/46010/46010-1429984291.jpg",
                performers = listOf(
                    ArtistColumnEntry(
                        id = "11997",
                        names = mapOf(
                            "en" to "REMEDIOS",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "10934",
                        names = mapOf(
                            "en" to "Ai Kayano",
                            "ja" to "茅野愛衣",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "6782",
                        names = mapOf(
                            "en" to "Haruka Tomatsu",
                            "ja" to "戸松遥",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "6782",
                        names = mapOf(
                            "en" to "Saori Hayami",
                            "ja" to "早見沙織",
                        )
                    ),
                ).map(json::encodeToString),
                composers = listOf(
                    ArtistColumnEntry(
                        id = "11997",
                        names = mapOf(
                            "en" to "REMEDIOS",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "12031",
                        names = mapOf(
                            "en" to "Galileo Galilei",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "15602",
                        names = mapOf(
                            "en" to "Yuuki Ozaki",
                            "ja" to "尾崎雄貴",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "8529",
                        names = mapOf(
                            "en" to "Norihiko Machida",
                            "ja" to "町田紀彦",
                        )
                    ),
                ).map(json::encodeToString),
                discs = listOf(
                    DiscEntry(
                        name = "Disc 1",
                        duration = "77:54",
                        tracks = listOf(
                            TrackEntry(
                                number = "01",
                                titles = mapOf(
                                    "ja-latn" to "Aoi Shiori",
                                    "ja" to "青い栞",
                                ),
                                duration = "5:38",
                            ),
                            TrackEntry(
                                number = "02",
                                titles = mapOf("ja-latn" to "The Next Morning"),
                                duration = "1:02",
                            ),
                            TrackEntry(
                                number = "03",
                                titles = mapOf("ja-latn" to "Beautiful Seasons With You"),
                                duration = "4:06",
                            ),
                            TrackEntry(
                                number = "04",
                                titles = mapOf("ja-latn" to "Thin Moonlight ~ Thin As Ice"),
                                duration = "2:03",
                            ),
                            TrackEntry(
                                number = "05",
                                titles = mapOf("ja-latn" to "Secret Feelings ~ Hidden Feelings of Love"),
                                duration = "2:51",
                            ),
                            TrackEntry(
                                number = "06",
                                titles = mapOf("ja-latn" to "Sink ~ Frozen Moments"),
                                duration = "1:33",
                            ),
                            TrackEntry(
                                number = "07",
                                titles = mapOf("ja-latn" to "All About her Death ~ It Has To Do With Her Not Being Here"),
                                duration = "1:26",
                            ),
                            TrackEntry(
                                number = "08",
                                titles = mapOf("ja-latn" to "My Star... ~ Steady As A Star"),
                                duration = "4:31",
                            ),
                            TrackEntry(
                                number = "09",
                                titles = mapOf("ja-latn" to "Guitar Afternoon ~ Lazy Afternoons"),
                                duration = "2:29",
                            ),
                            TrackEntry(
                                number = "10",
                                titles = mapOf("ja-latn" to "Secret Feelings ~ Tender"),
                                duration = "2:00",
                            ),
                            TrackEntry(
                                number = "11",
                                titles = mapOf("ja-latn" to "Before It Gets Dark ~ While The Sun Sets"),
                                duration = "1:26",
                            ),
                            TrackEntry(
                                number = "12",
                                titles = mapOf("ja-latn" to "I Left You ~ Did I Leave You"),
                                duration = "1:56",
                            ),
                            TrackEntry(
                                number = "13",
                                titles = mapOf("ja-latn" to "Dynamic Sunset ~ Words I Heard In The Silent Dawn"),
                                duration = "1:39",
                            ),
                            TrackEntry(
                                number = "14",
                                titles = mapOf("ja-latn" to "Lost Childhood ~ Wondering About"),
                                duration = "1:43",
                            ),
                            TrackEntry(
                                number = "15",
                                titles = mapOf("ja-latn" to "Dear Love ~ My Sweet And Most Dearest Love"),
                                duration = "2:10",
                            ),
                            TrackEntry(
                                number = "16",
                                titles = mapOf("ja-latn" to "Sounds Inside The House"),
                                duration = "1:51",
                            ),
                            TrackEntry(
                                number = "17",
                                titles = mapOf("ja-latn" to "Still... ~ Follow You Still..."),
                                duration = "1:19",
                            ),
                            TrackEntry(
                                number = "18",
                                titles = mapOf("ja-latn" to "On A Silent Afternoon ~ Childhood Marks On The Wall"),
                                duration = "2:00",
                            ),
                            TrackEntry(
                                number = "19",
                                titles = mapOf("ja-latn" to "Going Crazy Over You ~ Going Crazy Over Her"),
                                duration = "3:18",
                            ),
                            TrackEntry(
                                number = "20",
                                titles = mapOf("ja-latn" to "I Left You ~ I’m Here To Make You Cry"),
                                duration = "6:47",
                            ),
                            TrackEntry(
                                number = "21",
                                titles = mapOf("ja-latn" to "Leaving The Ceremony"),
                                duration = "2:10",
                            ),
                            TrackEntry(
                                number = "22",
                                titles = mapOf("ja-latn" to "Not As Friends ~ Can We Make It Not As Friends"),
                                duration = "5:39",
                            ),
                            TrackEntry(
                                number = "23",
                                titles = mapOf("ja-latn" to "Last Train Home ~ Twinkle Train Take Us Home"),
                                duration = "2:45",
                            ),
                            TrackEntry(
                                number = "24",
                                titles = mapOf(
                                    "ja-latn" to "secret base ~Kimi ga Kureta Mono~ (10 years after Ver.)",
                                    "ja" to "secret base ~君がくれたもの~ (10 years after Ver.)",
                                ),
                                duration = "5:53",
                            ),
                            TrackEntry(
                                number = "25",
                                titles = mapOf("ja-latn" to "When It All Comes To An End"),
                                duration = "1:24",
                            ),
                            TrackEntry(
                                number = "26",
                                titles = mapOf("ja-latn" to "Epilogue...From Time To Time"),
                                duration = "3:32",
                            ),
                            TrackEntry(
                                number = "27",
                                titles = mapOf(
                                    "ja-latn" to "Circle Game",
                                    "ja" to "サークルゲーム",
                                ),
                                duration = "4:43",
                            ),
                        ).map(json::encodeToString)
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