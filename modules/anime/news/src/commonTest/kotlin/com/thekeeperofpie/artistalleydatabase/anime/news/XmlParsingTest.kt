package com.thekeeperofpie.artistalleydatabase.anime.news

import com.thekeeperofpie.artistalleydatabase.anime.news.ann.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.ann.AnimeNewsNetworkFeed
import com.thekeeperofpie.artistalleydatabase.anime.news.cr.CrunchyrollNewsCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.cr.CrunchyrollNewsFeed
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals

class XmlParsingTest {

    private val xml = NewsXml.xml

    @Test
    fun parseAnimeNewsNetwork() {
        val actual = xml.decodeFromString<AnimeNewsNetworkFeed>(TestData.animeNewsNetworkXml)
        val expected = AnimeNewsNetworkFeed(
            rights = "© Anime News Network LLC",
            entries = listOf(
                AnimeNewsNetworkFeed.Entry(
                    title = "Test Title 1",
                    link = AnimeNewsNetworkFeed.Entry.Link(
                        url = "https://www.example.com/testLink1",
                    ),
                    published = LocalDateTime(
                        year = 2024,
                        month = Month.AUGUST,
                        dayOfMonth = 11,
                        hour = 15,
                        minute = 4,
                        second = 38,
                    ).toInstant(UtcOffset.ZERO),
                    updated = LocalDateTime(
                        year = 2024,
                        month = Month.AUGUST,
                        dayOfMonth = 11,
                        hour = 15,
                        minute = 4,
                        second = 38,
                    ).toInstant(UtcOffset.ZERO),
                    summary = "Test summary 1",
                    categories = listOf(
                        AnimeNewsNetworkFeed.Entry.Category(
                            value = AnimeNewsNetworkCategory.ANIME,
                        ),
                    ),
                ),
                AnimeNewsNetworkFeed.Entry(
                    title = "Test Title 2",
                    link = AnimeNewsNetworkFeed.Entry.Link(
                        url = "https://www.example.com/testLink2",
                    ),
                    published = LocalDateTime(
                        year = 2024,
                        month = Month.AUGUST,
                        dayOfMonth = 11,
                        hour = 13,
                        minute = 55,
                        second = 14,
                    ).toInstant(UtcOffset.ZERO),
                    updated = LocalDateTime(
                        year = 2024,
                        month = Month.AUGUST,
                        dayOfMonth = 11,
                        hour = 13,
                        minute = 55,
                        second = 14,
                    ).toInstant(UtcOffset.ZERO),
                    summary = "Test summary 2",
                    categories = listOf(
                        AnimeNewsNetworkFeed.Entry.Category(
                            value = AnimeNewsNetworkCategory.MANGA,
                        ),
                        AnimeNewsNetworkFeed.Entry.Category(
                            value = AnimeNewsNetworkCategory.PEOPLE,
                        ),
                    ),
                ),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun parseCrunchyrollNews() {
        val actual = xml.decodeFromString<CrunchyrollNewsFeed>(TestData.crunchyrollNewsXml)
        val expected = CrunchyrollNewsFeed(
            channel = CrunchyrollNewsFeed.Channel(
                copyright = "© Crunchyroll, LLC",
                logo = CrunchyrollNewsFeed.Channel.Logo(
                    url = "https://www.crunchyroll.com/i/smartnews/header-logo.png",
                ),
                items = listOf(
                    CrunchyrollNewsFeed.Channel.Item(
                        title = "Test Title 3",
                        author = "Test author 3",
                        category = CrunchyrollNewsCategory.NEWS,
                        description = "Test description 3",
                        content = "Test content 3",
                        thumbnail = CrunchyrollNewsFeed.Channel.Item.Thumbnail(
                            url = "https://example.com/testImageLink3",
                        ),
                        pubDate = LocalDateTime(
                            year = 2024,
                            month = Month.AUGUST,
                            dayOfMonth = 12,
                            hour = 19,
                            minute = 34,
                        ).toInstant(UtcOffset.ZERO),
                        link = "https://example.com/testLink3",
                    ),
                    CrunchyrollNewsFeed.Channel.Item(
                        title = "Test Title 4",
                        author = "Test author 4",
                        category = CrunchyrollNewsCategory.INTERVIEWS,
                        description = "Test description 4",
                        content = "Test content 4",
                        thumbnail = CrunchyrollNewsFeed.Channel.Item.Thumbnail(
                            url = "https://example.com/testImageLink4",
                        ),
                        pubDate = LocalDateTime(
                            year = 2024,
                            month = Month.AUGUST,
                            dayOfMonth = 12,
                            hour = 18,
                            minute = 34,
                        ).toInstant(UtcOffset.ZERO),
                        link = "https://example.com/testLink4",
                    ),
                )
            )
        )
        assertEquals(expected, actual)
    }
}
