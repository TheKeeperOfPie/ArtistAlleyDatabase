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
                        id = "12031",
                        names = mapOf(
                            "en" to "Galileo Galilei",
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
                        id = "6876",
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
    fun parseAlbum3() {
        val actual = runBlocking { parser.parseAlbum("105570") }
        assertThat(actual).isEqualTo(
            AlbumEntry(
                id = "105570",
                catalogId = "ASWJP-15022~3",
                names = mapOf(
                    "en" to "SUPERBEAT XONiC ORIGINAL SOUNDTRACK",
                    "ja" to "SUPERBEAT XONiC ORIGINAL SOUNDTRACK",
                    "ja-latn" to "SUPERBEAT XONiC ORIGINAL SOUNDTRACK",
                ),
                coverArt = "https://medium-media.vgm.io/albums/07/105570/105570-72831c9b1ce1.jpg",
                performers = listOf(
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Michael Blunck",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "T-ache",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Hyeryun",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "5302",
                        names = mapOf(
                            "en" to "NieN",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Ruka Kimura",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Sara * M",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Naoki Hashimoto",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "So Fly",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "JC",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Lim Ryu",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Kate Lesing",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "MYULEE",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "OneStar",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Lucy",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Suri",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Kjun",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Flash Finger",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "E.Q.P",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Bangkoon",
                        )
                    ),
                ).map(json::encodeToString),
                composers = listOf(
                    ArtistColumnEntry(
                        id = "7345",
                        names = mapOf(
                            "en" to "Takahiro Eguchi",
                            "ja" to "江口孝宏",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "5301",
                        names = mapOf(
                            "en" to "M2U",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "11366",
                        names = mapOf(
                            "en" to "Planetboom",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "5302",
                        names = mapOf(
                            "en" to "NieN",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "NDLee",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "47206",
                        names = mapOf(
                            "en" to "Cranky",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Mr.Funky",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "3946",
                        names = mapOf(
                            "en" to "Makou",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "36211",
                        names = mapOf(
                            "en" to "Sampling Masters MEGA",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Paul Bazooka",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "3941",
                        names = mapOf(
                            "en" to "Electronic Boutique",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "39460",
                        names = mapOf(
                            "en" to "Nauts"
                        )
                    ),
                    ArtistColumnEntry(
                        id = "1663",
                        names = mapOf(
                            "en" to "Ludwig van Beethoven"
                        ),
                    ),
                    ArtistColumnEntry(
                        id = "48043",
                        names = mapOf(
                            "en" to "Sampling Masters AYA"
                        )
                    ),
                    ArtistColumnEntry(
                        id = "1566",
                        names = mapOf(
                            "en" to "Daisuke Ishiwatari",
                            "ja" to "石渡太輔",
                        )
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "3rd Coast",
                        ),
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Flash Finger",
                        ),
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "J.Williams",
                        ),
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Spike",
                        ),
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Tsukasa",
                        ),
                    ),
                    ArtistColumnEntry(
                        id = null,
                        names = mapOf(
                            "unknown" to "Plastik",
                        ),
                    ),
                ).map(json::encodeToString),
                discs = listOf(
                    DiscEntry(
                        name = "Disc 1",
                        duration = "62:38",
                        tracks = listOf(
                            TrackEntry(
                                number = "01",
                                titles = mapOf(
                                    "en" to "STARGAZER (Opening)",
                                ),
                                duration = "2:08",
                            ),
                            TrackEntry(
                                number = "02",
                                titles = mapOf(
                                    "en" to "Nodding Hawk",
                                ),
                                duration = "2:54",
                            ),
                            TrackEntry(
                                number = "03",
                                titles = mapOf(
                                    "en" to "5.1.5.0",
                                ),
                                duration = "2:06",
                            ),
                            TrackEntry(
                                number = "04",
                                titles = mapOf(
                                    "en" to "No Way Out",
                                ),
                                duration = "3:35",
                            ),
                            TrackEntry(
                                number = "05",
                                titles = mapOf(
                                    "en" to "GIVE & TAKE",
                                ),
                                duration = "2:15",
                            ),
                            TrackEntry(
                                number = "06",
                                titles = mapOf(
                                    "en" to "The Lemon Squash",
                                ),
                                duration = "2:00",
                            ),
                            TrackEntry(
                                number = "07",
                                titles = mapOf(
                                    "en" to "Swedish Girl",
                                ),
                                duration = "1:54",
                            ),
                            TrackEntry(
                                number = "08",
                                titles = mapOf(
                                    "en" to "Fantasti-K",
                                ),
                                duration = "2:12",
                            ),
                            TrackEntry(
                                number = "09",
                                titles = mapOf(
                                    "en" to "Everyday",
                                ),
                                duration = "2:22",
                            ),
                            TrackEntry(
                                number = "10",
                                titles = mapOf(
                                    "en" to "Lop Nur",
                                ),
                                duration = "2:02",
                            ),
                            TrackEntry(
                                number = "11",
                                titles = mapOf(
                                    "en" to "One Juicy Step",
                                ),
                                duration = "2:03",
                            ),
                            TrackEntry(
                                number = "12",
                                titles = mapOf(
                                    "en" to "VOODOO GOOROO",
                                ),
                                duration = "2:05",
                            ),
                            TrackEntry(
                                number = "13",
                                titles = mapOf(
                                    "en" to "Kitty From Hell",
                                ),
                                duration = "2:09",
                            ),
                            TrackEntry(
                                number = "14",
                                titles = mapOf(
                                    "en" to "Drop the SHIT!",
                                ),
                                duration = "1:56",
                            ),
                            TrackEntry(
                                number = "15",
                                titles = mapOf(
                                    "en" to "CHASER",
                                ),
                                duration = "1:52",
                            ),
                            TrackEntry(
                                number = "16",
                                titles = mapOf(
                                    "en" to "Murky Waters",
                                ),
                                duration = "2:20",
                            ),
                            TrackEntry(
                                number = "17",
                                titles = mapOf(
                                    "en" to "\"Moonlight\" Sonata 3rd Movt.",
                                ),
                                duration = "2:00",
                            ),
                            TrackEntry(
                                number = "18",
                                titles = mapOf(
                                    "en" to "H.O.W.ling",
                                ),
                                duration = "2:08",
                            ),
                            TrackEntry(
                                number = "19",
                                titles = mapOf(
                                    "en" to "Dimension Detonator",
                                ),
                                duration = "2:02",
                            ),
                            TrackEntry(
                                number = "20",
                                titles = mapOf(
                                    "en" to "HEAVY DAY (from Guilty Gear Xrd -SIGN-)",
                                ),
                                duration = "4:24",
                            ),
                            TrackEntry(
                                number = "21",
                                titles = mapOf(
                                    "en" to "아침이 좋은 이유 ~Morning Calm~",
                                ),
                                duration = "1:47",
                            ),
                            TrackEntry(
                                number = "22",
                                titles = mapOf(
                                    "en" to "Love is Real",
                                ),
                                duration = "3:31",
                            ),
                            TrackEntry(
                                number = "23",
                                titles = mapOf(
                                    "en" to "Timeline",
                                ),
                                duration = "2:42",
                            ),
                            TrackEntry(
                                number = "24",
                                titles = mapOf(
                                    "en" to "HINAGIKU ~towards the sunlight~",
                                ),
                                duration = "2:02",
                            ),
                            TrackEntry(
                                number = "25",
                                titles = mapOf(
                                    "en" to "Peach Fuzz",
                                ),
                                duration = "1:59",
                            ),
                            TrackEntry(
                                number = "26",
                                titles = mapOf(
                                    "en" to "Party of the Year",
                                ),
                                duration = "2:08",
                            ),
                            TrackEntry(
                                number = "27",
                                titles = mapOf(
                                    "en" to "March of Fear",
                                ),
                                duration = "2:02",
                            ),
                        ).map(json::encodeToString)
                    ),
                    DiscEntry(
                        name = "Disc 2",
                        duration = "49:46",
                        tracks = listOf(
                            TrackEntry(
                                number = "01",
                                titles = mapOf(
                                    "en" to "Systematic Chaos",
                                ),
                                duration = "2:16",
                            ),
                            TrackEntry(
                                number = "02",
                                titles = mapOf(
                                    "en" to "Louder",
                                ),
                                duration = "2:22",
                            ),
                            TrackEntry(
                                number = "03",
                                titles = mapOf(
                                    "en" to "PETER PAN",
                                ),
                                duration = "2:10",
                            ),
                            TrackEntry(
                                number = "04",
                                titles = mapOf(
                                    "en" to "All Night Long",
                                ),
                                duration = "2:31",
                            ),
                            TrackEntry(
                                number = "05",
                                titles = mapOf(
                                    "en" to "Collaboration (Game Version)",
                                ),
                                duration = "2:05",
                            ),
                            TrackEntry(
                                number = "06",
                                titles = mapOf(
                                    "en" to "Keep on Rockin' (Game Version)",
                                ),
                                duration = "1:54",
                            ),
                            TrackEntry(
                                number = "07",
                                titles = mapOf(
                                    "en" to "Aztec Bump",
                                ),
                                duration = "2:03",
                            ),
                            TrackEntry(
                                number = "08",
                                titles = mapOf(
                                    "en" to "Bang Bang Groove",
                                ),
                                duration = "2:00",
                            ),
                            TrackEntry(
                                number = "09",
                                titles = mapOf(
                                    "en" to "Round Trip",
                                ),
                                duration = "1:50",
                            ),
                            TrackEntry(
                                number = "10",
                                titles = mapOf(
                                    "en" to "Trompe L'œil",
                                ),
                                duration = "2:11",
                            ),
                            TrackEntry(
                                number = "11",
                                titles = mapOf(
                                    "en" to "Black Inked Skin",
                                ),
                                duration = "1:56",
                            ),
                            TrackEntry(
                                number = "12",
                                titles = mapOf(
                                    "en" to "Wreckkkk",
                                ),
                                duration = "1:57",
                            ),
                            TrackEntry(
                                number = "13",
                                titles = mapOf(
                                    "en" to "Control",
                                ),
                                duration = "2:05",
                            ),
                            TrackEntry(
                                number = "14",
                                titles = mapOf(
                                    "en" to "Get Down",
                                ),
                                duration = "1:50",
                            ),
                            TrackEntry(
                                number = "15",
                                titles = mapOf(
                                    "en" to "Crazy",
                                ),
                                duration = "1:41",
                            ),
                            TrackEntry(
                                number = "16",
                                titles = mapOf(
                                    "en" to "Interstellar",
                                ),
                                duration = "2:00",
                            ),
                            TrackEntry(
                                number = "17",
                                titles = mapOf(
                                    "en" to "Souls of Vampire ~ I . Shadow Moon",
                                ),
                                duration = "2:03",
                            ),
                            TrackEntry(
                                number = "18",
                                titles = mapOf(
                                    "en" to "Souls of Vampire ~ II . Bloody Vengeance",
                                ),
                                duration = "2:17",
                            ),
                            TrackEntry(
                                number = "19",
                                titles = mapOf(
                                    "en" to "Shine Like A Star (Main Title)",
                                ),
                                duration = "1:03",
                            ),
                            TrackEntry(
                                number = "20",
                                titles = mapOf(
                                    "en" to "Life is Practice (Tutorial)",
                                ),
                                duration = "1:54",
                            ),
                            TrackEntry(
                                number = "21",
                                titles = mapOf(
                                    "en" to "Way To You (Stage Select)",
                                ),
                                duration = "0:32",
                            ),
                            TrackEntry(
                                number = "22",
                                titles = mapOf(
                                    "en" to "Make U Feel (Result)",
                                ),
                                duration = "0:32",
                            ),
                            TrackEntry(
                                number = "23",
                                titles = mapOf(
                                    "en" to "New Level (Total Result)",
                                ),
                                duration = "0:32",
                            ),
                            TrackEntry(
                                number = "24",
                                titles = mapOf(
                                    "en" to "Wicked Game (Result Failed)",
                                ),
                                duration = "0:43",
                            ),
                            TrackEntry(
                                number = "25",
                                titles = mapOf(
                                    "en" to "Back Around (Backstage)",
                                ),
                                duration = "1:02",
                            ),
                            TrackEntry(
                                number = "26",
                                titles = mapOf(
                                    "en" to "Right Now (World Tour)",
                                ),
                                duration = "1:02",
                            ),
                            TrackEntry(
                                number = "27",
                                titles = mapOf(
                                    "en" to "Band in a Box (Option)",
                                ),
                                duration = "1:05",
                            ),
                            TrackEntry(
                                number = "28",
                                titles = mapOf(
                                    "en" to "Rise To The Top (DJ Ranking)",
                                ),
                                duration = "1:02",
                            ),
                            TrackEntry(
                                number = "29",
                                titles = mapOf(
                                    "en" to "Fiesta (Ending)",
                                ),
                                duration = "0:17",
                            ),
                            TrackEntry(
                                number = "30",
                                titles = mapOf(
                                    "en" to "Now Rolling... (Loading)",
                                ),
                                duration = "2:51",
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