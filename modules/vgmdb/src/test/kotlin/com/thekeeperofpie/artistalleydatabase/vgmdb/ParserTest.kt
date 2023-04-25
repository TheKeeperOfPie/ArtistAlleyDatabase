package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.squareup.moshi.rawType
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.DiscEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.TrackEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtist
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.rules.TemporaryFolder

class ParserTest {

    private val json = Json {
        isLenient = true
        prettyPrint = true
    }

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val parser by lazy {
        VgmdbParser(json, OkHttpClient.Builder().build())
    }

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
    fun searchOneAlbum2() {
        val actual = runBlocking { parser.search("LACM-14644") }
        assertThat(actual).isEqualTo(
            SearchResults(
                albums = listOf(
                    SearchResults.AlbumResult(
                        id = "68192",
                        catalogId = "LACM-14644",
                        names = mapOf(
                            "en" to "You & I / Ayaka Ohashi [Ady Edition]",
                            "ja" to "ユー&アイ / 大橋彩香 [アディ盤]",
                            "ja-latn" to "You & I / Ayaka Ohashi [Ady Edition]"
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
                ).encodeListToString(),
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
                ).encodeListToString(),
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
                        ).encodeListToString()
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
                        ).encodeListToString()
                    ),
                ).encodeListToString(),
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
                ).encodeListToString(),
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
                ).encodeListToString(),
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
                        ).encodeListToString()
                    ),
                ).encodeListToString(),
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
                    "Michael Blunck",
                    "T-ache",
                    "Hyeryun",
                    ArtistColumnEntry(
                        id = "5302",
                        names = mapOf(
                            "en" to "NieN",
                        )
                    ),
                    "Ruka Kimura",
                    "Sara * M",
                    "Naoki Hashimoto",
                    "So Fly",
                    "JC",
                    "Lim Ryu",
                    "Kate Lesing",
                    "MYULEE",
                    "OneStar",
                    "Lucy",
                    "Suri",
                    "Kjun",
                    "Flash Finger",
                    "E.Q.P",
                    "Bangkoon",
                ).encodeListToString(),
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
                    "NDLee",
                    ArtistColumnEntry(
                        id = "47206",
                        names = mapOf(
                            "en" to "Cranky",
                        )
                    ),
                    "Mr.Funky",
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
                    "Paul Bazooka",
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
                    "3rd Coast",
                    "Flash Finger", "J.Williams",
                    "Spike",
                    "Tsukasa",
                    "Plastik",
                ).encodeListToString(),
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
                        ).encodeListToString()
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
                        ).encodeListToString()
                    ),
                ).encodeListToString(),
            )
        )
    }

    @Test
    fun parseAlbum4() {
        val actual = runBlocking { parser.parseAlbum("59653") }
        assertThat(actual).isEqualTo(
            AlbumEntry(
                id = "59653",
                catalogId = "HMCD-0005",
                names = mapOf(
                    "en" to "HATSUNE MIKU EXPO 2016 E.P.",
                    "ja" to "HATSUNE MIKU EXPO 2016 E.P.",
                    "ja-latn" to "HATSUNE MIKU EXPO 2016 E.P.",
                ),
                coverArt = "https://medium-media.vgm.io/albums/35/59653/59653-1466021248.png",
                performers = listOf(
                    ArtistColumnEntry(
                        id = "2466",
                        names = mapOf(
                            "en" to "Miku Hatsune",
                            "ja" to "初音ミク",
                        )
                    ),
                ).encodeListToString(),
                composers = listOf(
                    ArtistColumnEntry(
                        id = "11654",
                        names = mapOf(
                            "en" to "Hachioji P",
                            "ja" to "八王子P",
                        )
                    ),
                    "BIGHEAD",
                    "GuitarHeroPianoZero",
                    ArtistColumnEntry(
                        id = "9553",
                        names = mapOf(
                            "en" to "cosMo@bousouP",
                            "ja" to "cosMo@暴走P",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "2934",
                        names = mapOf(
                            "en" to "Eiji Hirasawa",
                            "ja" to "平沢栄司",
                        )
                    ),
                    "CircusP",
                    "Render/ Monk/ Lola Fair",
                    "Xiao-Ming",
                    ArtistColumnEntry(
                        id = "16741",
                        names = mapOf(
                            "en" to "YZYX",
                        )
                    ),
                    "AlexTrip Sands",
                    ArtistColumnEntry(
                        id = "6457",
                        names = mapOf(
                            "en" to "Anamanaguchi",
                        )
                    ),
                    ArtistColumnEntry(
                        id = "9553",
                        names = mapOf(
                            "en" to "cosMo@bosoP",
                        )
                    ),
                ).encodeListToString(),
                discs = listOf(
                    DiscEntry(
                        name = "Disc 1 Original Work",
                        duration = "51:57",
                        tracks = listOf(
                            TrackEntry(
                                number = "01",
                                titles = mapOf(
                                    "en" to "Blue Star",
                                ),
                                duration = "3:41",
                            ),
                            TrackEntry(
                                number = "02",
                                titles = mapOf(
                                    "en" to "Sharing The World",
                                ),
                                duration = "4:07",
                            ),
                            TrackEntry(
                                number = "03",
                                titles = mapOf(
                                    "en" to "Glass Wall",
                                ),
                                duration = "4:41",
                            ),
                            TrackEntry(
                                number = "04",
                                titles = mapOf(
                                    "en" to "The Disappearance of Hatsune Miku",
                                    "ja" to "初音ミクの消失",
                                ),
                                duration = "4:47",
                            ),
                            TrackEntry(
                                number = "05",
                                titles = mapOf(
                                    "en" to "Fragments of Stars (piano ver.)",
                                    "ja" to "星のカケラ (piano ver.)",
                                ),
                                duration = "4:00",
                            ),
                            TrackEntry(
                                number = "06",
                                titles = mapOf(
                                    "en" to "Ten Thousand Stars",
                                ),
                                duration = "3:54",
                            ),
                            TrackEntry(
                                number = "07",
                                titles = mapOf(
                                    "en" to "Constellation",
                                ),
                                duration = "3:36",
                            ),
                            TrackEntry(
                                number = "08",
                                titles = mapOf(
                                    "en" to "Together, Make The Magic!",
                                ),
                                duration = "3:42",
                            ),
                            TrackEntry(
                                number = "09",
                                titles = mapOf(
                                    "en" to "ALL IS MUSIC!",
                                ),
                                duration = "4:13",
                            ),
                            TrackEntry(
                                number = "10",
                                titles = mapOf(
                                    "en" to "MikuMambo",
                                ),
                                duration = "3:26",
                            ),
                            TrackEntry(
                                number = "11",
                                titles = mapOf(
                                    "en" to "Miku",
                                ),
                                duration = "3:40",
                            ),
                            TrackEntry(
                                number = "12",
                                titles = mapOf(
                                    "en" to "Blue Star (Carpainter/TREKKIE TRAX Remix)",
                                ),
                                duration = "3:34",
                            ),
                            TrackEntry(
                                number = "13",
                                titles = mapOf(
                                    "en" to "Blue Star (Mark Redito Remix)",
                                ),
                                duration = "4:36",
                            ),
                        ).encodeListToString()
                    ),
                ).encodeListToString(),
            )
        )
    }

    /**
     * Tests "Composed By"
     */
    @Test
    fun parseAlbum5() {
        val actual = runBlocking { parser.parseAlbum("65600") }
        assertThat(actual).isEqualTo(
            AlbumEntry(
                id = "65600",
                catalogId = "KIGA-30",
                names = mapOf(
                    "en" to "Pure3: Feel Classics ~Naoya Shimokawa~",
                    "ja" to "Pure3: Feel Classics ~Naoya Shimokawa~",
                    "ja-latn" to "Pure3: Feel Classics ~Naoya Shimokawa~",
                ),
                coverArt = "https://medium-media.vgm.io/albums/00/65600/65600-312cb4cf07e6.jpg",
                performers = emptyList(),
                composers = listOf(
                    ArtistColumnEntry(
                        id = "28",
                        names = mapOf(
                            "en" to "Naoya Shimokawa",
                            "ja" to "下川直哉",
                        )
                    ),
                ).encodeListToString(),
                discs = listOf(
                    DiscEntry(
                        name = "Disc 1",
                        duration = "50:48",
                        tracks = listOf(
                            TrackEntry(
                                number = "01",
                                titles = mapOf(
                                    "ja" to "Feeling Heart",
                                ),
                                duration = "3:56",
                            ),
                            TrackEntry(
                                number = "02",
                                titles = mapOf(
                                    "ja" to "心はいつもあなたのそばに",
                                ),
                                duration = "4:13",
                            ),
                            TrackEntry(
                                number = "03",
                                titles = mapOf(
                                    "ja" to "ありがとう",
                                ),
                                duration = "4:56",
                            ),
                            TrackEntry(
                                number = "04",
                                titles = mapOf(
                                    "ja" to "時の魔法",
                                ),
                                duration = "4:21",
                            ),
                            TrackEntry(
                                number = "05",
                                titles = mapOf(
                                    "ja" to "POWDER SNOW",
                                ),
                                duration = "3:45",
                            ),
                            TrackEntry(
                                number = "06",
                                titles = mapOf(
                                    "ja" to "Free and Dream",
                                ),
                                duration = "2:56",
                            ),
                            TrackEntry(
                                number = "07",
                                titles = mapOf(
                                    "ja" to "君をのせて",
                                ),
                                duration = "4:45",
                            ),
                            TrackEntry(
                                number = "08",
                                titles = mapOf(
                                    "ja" to "ヒトリ",
                                ),
                                duration = "4:31",
                            ),
                            TrackEntry(
                                number = "09",
                                titles = mapOf(
                                    "ja" to "さよならのこと",
                                ),
                                duration = "3:30",
                            ),
                            TrackEntry(
                                number = "10",
                                titles = mapOf(
                                    "ja" to "キミガタメ",
                                ),
                                duration = "4:56",
                            ),
                            TrackEntry(
                                number = "11",
                                titles = mapOf(
                                    "ja" to "closing",
                                ),
                                duration = "5:04",
                            ),
                            TrackEntry(
                                number = "12",
                                titles = mapOf(
                                    "ja" to "Feeling Heart (another mix)",
                                ),
                                duration = "3:55",
                            ),
                        ).encodeListToString()
                    ),
                ).encodeListToString(),
            )
        )
    }

    // TODO: Specific hardcoded test for name containing a "/", which is used
    //  internally for split logic, but can also be part of an artist's name

    @Test
    fun parseAlbum6() {
        assertThat(
            runBlocking { parser.parseAlbum("99954") }
                ?.composers
                ?.filter { it.startsWith("{") }
                ?.size
        ).isEqualTo(31)
    }

    @Test
    fun parseArtist() {
        val actual = runBlocking { parser.parseArtist("29051") }
        assertThat(actual).isEqualTo(
            VgmdbArtist(
                id = "29051",
                names = mapOf(
                    "en" to "Akari Nanawo",
                    "ja" to "ナナヲ アカリ",
                ),
                picture = "https://media.vgm.io/artists/15/29051/29051-1527082407.jpg"
            )
        )
    }

    private fun List<Any>.encodeListToString() = map {
        when (it) {
            is String -> it
            else -> json.encodeToString(json.serializersModule.serializer(it.javaClass.rawType), it)
        }
    }
}
