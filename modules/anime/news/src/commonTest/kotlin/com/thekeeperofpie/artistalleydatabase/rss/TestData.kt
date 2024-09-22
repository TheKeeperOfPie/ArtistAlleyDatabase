package com.thekeeperofpie.artistalleydatabase.rss

internal object TestData {

    // language=xml
    val animeNewsNetworkXml = """<?xml version="1.0" encoding="utf-8"?>
        <feed xmlns="http://www.w3.org/2005/Atom" xml:lang="en" xmlns:ann="https://www.animenewsnetwork.com/">

          <title>Anime News Network - News</title>
          <subtitle>ANN Atom feed</subtitle>
          <link href="https://www.animenewsnetwork.com/news/atom.xml?ann-edition=us" rel="self"/>
          <link href="https://www.animenewsnetwork.com/news/"/>
          <rights>&#xA9; Anime News Network LLC</rights>
          <updated>2024-08-11T20:28:36-04:00</updated>
          <id>https://www.animenewsnetwork.com/news/atom.xml?ann-edition=us</id>


          <entry xml:lang="en-US">
            <title type="html">Test Title 1</title>
            <link href="https://www.example.com/testLink1"/>
            <id>https://www.example.com/cms/testLink1</id>
            <published>2024-08-11T15:04:38Z</published>
            <updated>2024-08-11T15:04:38Z</updated>
              <summary type="html">Test summary 1</summary>
              <category term="Anime"/>
            <ann:cmssection>News</ann:cmssection>
          </entry>

          <entry xml:lang="en-US">
            <title type="html">Test Title 2</title>
            <link href="https://www.example.com/testLink2"/>
            <id>https://www.example.com/cms/testLink2</id>
            <published>2024-08-11T13:55:14Z</published>
            <updated>2024-08-11T13:55:14Z</updated>
              <summary type="html">Test summary 2</summary>
              <category term="Manga"/>
              <category term="People"/>
            <ann:cmssection>News</ann:cmssection>
          </entry>


      </feed>
    """.trimIndent()

    // language=xml
    val crunchyrollNewsXml = """<?xml version="1.0" encoding="utf-8"?>
        <rss xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:media="http://search.yahoo.com/mrss/" xmlns:snf="http://www.smartnews.be/snf" version="2.0">
            <channel>
                <title>Latest Anime News</title>
                <description>You heard it here first! Stay up to date on the latest anime and manga news straight out of Japan.</description>
                <link>https://crunchyroll.com/news</link>
                <copyright>© Crunchyroll, LLC</copyright>
                <pubDate>Mon, 12 Aug 2024 19:34:00 GMT</pubDate>
                <lastBuildDate>Mon, 12 Aug 2024 19:34:00 GMT</lastBuildDate>
                <language>en-US</language>
                <snf:logo>
                    <url>https://www.crunchyroll.com/i/smartnews/header-logo.png</url>
                </snf:logo>
                <item>
                    <title>Test Title 3</title>
                    <author>Test author 3</author>
                    <category>News</category>
                    <description>Test description 3</description>
                    <content:encoded>
                        <![CDATA[ Test content 3 ]]>
                    </content:encoded>
                    <media:thumbnail url="https://example.com/testImageLink3"/>
                    <pubDate>Mon, 12 Aug 2024 19:34:00 GMT</pubDate>
                    <link>https://example.com/testLink3</link>
                    <guid>https://example.com/testLink3</guid>
                </item>
                <item>
                    <title>Test Title 4</title>
                    <author>Test author 4</author>
                    <category>Interviews</category>
                    <description>Test description 4</description>
                    <content:encoded>
                        <![CDATA[ Test content 4 ]]>
                    </content:encoded>
                    <media:thumbnail url="https://example.com/testImageLink4"/>
                    <pubDate>Mon, 12 Aug 2024 18:34:00 GMT</pubDate>
                    <link>https://example.com/testLink4</link>
                    <guid>https://example.com/testLink4</guid>
                </item>
            </channel>
            </rss>
    """.trimIndent()
}
