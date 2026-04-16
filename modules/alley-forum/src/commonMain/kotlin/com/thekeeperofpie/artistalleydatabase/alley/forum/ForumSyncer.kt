package com.thekeeperofpie.artistalleydatabase.alley.forum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.ColumnAdapters
import com.thekeeperofpie.artistalleydatabase.alley.data.toSeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.forum.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.textRes
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.discord.ChannelFlag
import com.thekeeperofpie.artistalleydatabase.discord.CreateMessage
import com.thekeeperofpie.artistalleydatabase.discord.Embed
import com.thekeeperofpie.artistalleydatabase.discord.ForumLayout
import com.thekeeperofpie.artistalleydatabase.discord.Message
import com.thekeeperofpie.artistalleydatabase.discord.Thread
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.compose.resources.getString
import java.nio.file.Files
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import artistalleydatabase.modules.alley.data.generated.resources.Res as AlleyDataRes

@OptIn(ExperimentalSerializationApi::class)
internal object ForumSyncer {

    var error by mutableStateOf<String?>(null)

    private val THROTTLE_DELAY = 3.seconds

    suspend fun verifyChannel() {
        val channel = DiscordApi.getChannel(BuildKonfig.discordForumChannelId)
        if (channel.defaultForumLayout != ForumLayout.GALLERY_VIEW) {
            val errorMessage =
                "Directory channel is not gallery layout, was ${channel.defaultForumLayout}"
            println(errorMessage)
            error = errorMessage
        }
        val threads = DiscordApi.getThreads(BuildKonfig.discordForumChannelId)
        println("Threads = ${threads.threads.map { it.name }}")
    }

    suspend fun deleteAllThreads() {
        println("Deleting ALL threads")
        val threads = DiscordApi.getThreads(BuildKonfig.discordForumChannelId)
        threads.threads
            .filter {
                val flags = it.flags
                flags == null || (flags.flags and ChannelFlag.PINNED.flag) == 0
            }
            .forEach {
                delay(THROTTLE_DELAY)
                println("Deleting ${it.name}")
                DiscordApi.deleteThread(it.id)
            }
    }

    suspend fun syncPinned() = withContext(Dispatchers.IO) {
        val existingPin = DiscordApi.getThreads(BuildKonfig.discordForumChannelId)
            .threads
            .find {
                val flags = it.flags ?: return@find false
                (flags.flags and ChannelFlag.PINNED.flag) != 0
            }
        val message = """
        |# Welcome to the AX AA Directory!
        |This is a mirror of ${AlleyUtils.siteUrl}. We encourage you to use that site if possible as it offers a better UI, search by tags, and offline support.
        |## Attendees
        |Please be respectful in artists' threads. This is a space to converse with artists, ask them questions about the stuff they're selling, and get excited about all of the amazing art.
        |
        |Artist threads will only be unlocked if the artist has allowed chatting.
        |## Artists
        |If you would like to unlock your post for chatting or edit your data, see your page at ${AlleyUtils.siteUrl}. You can verify through the prompt at the bottom of that page.
        |
        |You may also use the AA Directory bot via `/aa verify [convention] [booth]`, which will unlock the private artist only channel and give you a form access link.
        |## Questions, suggestions, concerns
        |If you have any feedback for the directory itself, please visit us in<#${BuildKonfig.discordPublicArtistAlleyChannelId}> or contact <@${BuildKonfig.discordContactUserId}>.
        """.trimMargin()
        if (existingPin == null) {
            DiscordApi.createThread(
                channelId = BuildKonfig.discordForumChannelId,
                title = "What is this?",
                firstMessage = CreateMessage(content = message),
            )
        } else {
            DiscordApi.editMessage(
                channelId = existingPin.id,
                messageId = existingPin.id,
                message = CreateMessage(content = message),
            )
        }
    }

    suspend fun syncThreads(range: ClosedRange<Booth>? = null) = withContext(Dispatchers.IO) {
        val database = AlleySqlDatabase(
            driver = createDriver(),
            artistEntryAnimeExpo2026Adapter = ColumnAdapters.artistEntryAnimeExpo2026Adapter,
            seriesEntryAdapter = ColumnAdapters.seriesEntryAdapter,
        )
        val series = database.seriesEntryQueries.getSeries().awaitAsList()
            .associate { it.id to it.toSeriesInfo() }
        val artists = database.artistEntryAnimeExpo2026Queries.getArtists()
            .awaitAsList()
            .filterNot { it.booth.isNullOrBlank() }
            .filter {
                range ?: return@filter true
                val booth = it.booth?.let(Booth::fromStringOrNull)
                booth != null && booth in range
            }
            .map { Artist.fromEntry(it, series) }
            .shuffled()

        val threadsList = DiscordApi.getThreads(BuildKonfig.discordForumChannelId)
        println("Threads = ${threadsList.threads.map { it.name }}")

        val threads = threadsList.threads
            .filter {
                val flags = it.flags
                flags == null || (flags.flags and ChannelFlag.PINNED.flag) == 0
            }
            .filter {
                val threadBooth = it.name?.substringBefore("-")?.trim()
                    ?.let(Booth::fromStringOrNull)
                threadBooth == null || range == null || threadBooth in range
            }
            .map(::ThreadWrapper)
            .toMutableSet()

        val missing = mutableListOf<Artist>()
        val changed = mutableListOf<Pair<Artist, ThreadWithContent>>()
        artists.forEach { artist ->
            val thread = threads.find {
                it.booth == artist.booth && it.artistName == artist.entry.name
            }
            threads.remove(thread)
            if (thread == null) {
                missing += artist
            } else {
                delay(THROTTLE_DELAY)
                println("Fetching ${thread.thread.name}")
                val (secondMessage, firstMessage) = DiscordApi.getOldestThreadMessages(thread.thread.id)
                changed += artist to ThreadWithContent(thread, firstMessage, secondMessage)
            }
        }

        println("Missing = ${missing.sortedBy { it.entry.booth }.map { it.threadTitle }}")
        println(
            "Changed = ${
                changed.sortedBy { it.first.entry.booth }.map { it.first.threadTitle }
            }"
        )
        if (range == null) {
            println("Removed = ${threads.map { it.thread.name }}")
        }

        missing.forEach {
            delay(THROTTLE_DELAY)
            DiscordApi.createThread(
                channelId = BuildKonfig.discordForumChannelId,
                title = it.threadTitle,
                firstMessage = it.threadContent.first,
                secondMessage = it.threadContent.second,
                imageAttachments = it.threadContent.imageAttachments,
            )
            println("Created ${it.threadTitle}")
        }

        changed.forEach {
            delay(THROTTLE_DELAY)
            DiscordApi.editMessage(
                channelId = it.second.thread.thread.id,
                messageId = it.second.firstMessage.id,
                message = it.first.threadContent.first,
                imageAttachments = it.first.threadContent.imageAttachments,
            )
            println("Edited ${it.second.firstMessage.id}")
            delay(THROTTLE_DELAY)
//            DiscordApi.editMessage(
//                channelId = it.second.thread.thread.id,
//                messageId = it.second.secondMessage.id,
//                message = it.first.threadContent.second,
//            )
//            println("Edited ${it.second.secondMessage.id}")
            println("Edited ${it.second.thread.thread.name}")
            if (it.second.thread.thread.name != it.first.threadTitle) {
                delay(THROTTLE_DELAY)
                DiscordApi.modifyThread(
                    threadId = it.second.thread.thread.id,
                    name = it.first.threadTitle
                )
                println("Updated ${it.second.thread.thread.name} -> ${it.first.threadTitle}")
            }
        }

        if (range == null) {
            threads.forEach {
                delay(THROTTLE_DELAY)
                DiscordApi.modifyThread(threadId = it.thread.id, archived = true)
            }
        }
    }

    private suspend fun createDriver(): SqlDriver = withContext(Dispatchers.IO) {
        val file = Files.createTempFile(null, ".sqlite").toFile()
        file.deleteOnExit()
        file.writeBytes(Res.readBytes("files/database.sqlite"))
        JdbcSqliteDriver("jdbc:sqlite:${file.absolutePath}")
    }

    private data class MessageData(
        val first: CreateMessage,
        val imageAttachments: List<Pair<String, ByteArray>>,
        val second: CreateMessage,
    )

    @ConsistentCopyVisibility
    private data class Artist private constructor(
        val entry: ArtistEntryAnimeExpo2026,
        val seriesInferred: List<String>,
        val seriesConfirmed: List<String>,
        val threadContent: MessageData,
    ) {
        companion object {
            suspend fun fromEntry(
                entry: ArtistEntryAnimeExpo2026,
                series: Map<String, SeriesInfo>,
            ): Artist {
                val seriesInferred = entry.seriesInferred.mapSeriesNames(series)
                val seriesConfirmed = entry.seriesConfirmed.mapSeriesNames(series)
                return Artist(
                    entry = entry,
                    seriesInferred = seriesInferred,
                    seriesConfirmed = seriesConfirmed,
                    threadContent = buildMessage(entry, seriesInferred, seriesConfirmed)
                )
            }

            private fun List<String>.mapSeriesNames(series: Map<String, SeriesInfo>) =
                map { series[it]?.name(AniListLanguageOption.DEFAULT) ?: it }


            private suspend fun buildMessage(
                entry: ArtistEntryAnimeExpo2026,
                seriesInferred: List<String>,
                seriesConfirmed: List<String>,
            ): MessageData {
                val headerImage = entry.embeds
                    ?.let(AlleyImageUtils::getEmbedImagesMap)
                    ?.firstOrNull()
                    ?.let { "${Uuid.random()}${it.first}" to it }

                val imageEmbed = headerImage?.let {
                    Embed(
                        type = Embed.Type.IMAGE,
                        image = Embed.Image(url = "attachment://${it.first}", flags = 0),
                    )
                }

                val thumbnailImage = entry.embeds
                    ?.let(AlleyImageUtils::getProfileImageWithPath)
                    ?.let { "${Uuid.random()}${it.first}" to it }

                val titleEmbed = Embed(
                    thumbnail = thumbnailImage?.let {
                        Embed.Image(
                            url = "attachment://${it.first}",
                            flags = 0,
                        )
                    },
                    fields = entry.summary
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            listOf(
                                Embed.Field(
                                    name = "\uD83C\uDFA8 ${entry.name}",
                                    value = it,
                                )
                            )
                        },
                )

                suspend fun LinkModel.asMarkdownLink(): String {
                    val textRes = type?.textRes
                    val label = if (textRes == null) {
                        identifier
                    } else if (type == Link.Type.X) {
                        // TODO: Move this to resources
                        // To make the link more clickable on mobile
                        "X (Twitter)"
                    } else {
                        getString(textRes)
                    }
                    return "[$label](${link})"
                }

                val portfolioLinksEmbed = if (entry.portfolioLinks.isNotEmpty()) {
                    Embed.Field(
                        name = "Portfolio",
                        value = entry.portfolioLinks
                            .map(LinkModel::parse)
                            .map { it.asMarkdownLink() }
                            .joinToString("\n"),
                        inline = true,
                    )
                } else null

                val socialLinksEmbed = if (entry.socialLinks.isNotEmpty()) {
                    Embed.Field(
                        name = "Socials",
                        value = entry.socialLinks
                            .map(LinkModel::parse)
                            .map { it.asMarkdownLink() }
                            .joinToString("\n"),
                        inline = true,
                    )
                } else null

                val storeLinksEmbed = if (entry.storeLinks.isNotEmpty()) {
                    Embed.Field(
                        name = "Store",
                        value = entry.storeLinks
                            .map(LinkModel::parse)
                            .map { it.asMarkdownLink() }
                            .joinToString("\n"),
                        inline = true,
                    )
                } else null

                val commissionsEmbed = if (entry.commissions.isNotEmpty()) {
                    Embed.Field(
                        name = "Commissions",
                        value = entry.commissions
                            .map(LinkModel::parse)
                            .map { it.asMarkdownLink() }
                            .joinToString("\n"),
                        inline = true,
                    )
                } else null

                val linksEmbed =
                    Embed(
                        title = "\uD83D\uDD17 Links",
                        fields = listOfNotNull(
                            portfolioLinksEmbed,
                            socialLinksEmbed,
                            storeLinksEmbed,
                            commissionsEmbed,
                        )
                    ).takeUnless { it.fields.isNullOrEmpty() }

                val (seriesTitle, series) = if (seriesConfirmed.isNotEmpty()) {
                    "Series" to seriesConfirmed
                } else if (seriesInferred.isNotEmpty()) {
                    "Series - Unconfirmed" to seriesInferred
                } else {
                    null to emptyList()
                }

                val seriesEmbed = seriesTitle?.let {
                    Embed(
                        fields = listOf(
                            Embed.Field(
                                name = "\uD83C\uDFF7 $it",
                                value = series.joinToString(prefix = "```", postfix = "```")
                            )
                        ),
                    )
                }

                val (merchTitle, merch) = if (entry.merchConfirmed.isNotEmpty()) {
                    "Merch" to entry.merchConfirmed
                } else if (entry.merchInferred.isNotEmpty()) {
                    "Merch - Unconfirmed" to entry.merchInferred
                } else {
                    null to emptyList()
                }

                val merchEmbed = merchTitle?.let {
                    Embed(
                        fields = listOf(
                            Embed.Field(
                                name = "\uD83D\uDCE6 $it",
                                value = merch.joinToString(prefix = "```", postfix = "```")
                            )
                        ),
                    )
                }

                val firstCreateMessage = CreateMessage(
                    content = null,
                    embeds = listOfNotNull(
                        imageEmbed,
                        titleEmbed,
                        linksEmbed,
                        seriesEmbed,
                        merchEmbed,
                    ).ifEmpty { null }
                )
                val messageLength = firstCreateMessage.embeds?.sumOf {
                    (it.title?.length ?: 0) + (it.fields?.sumOf { it.name.length + it.value.length }
                        ?: 0)
                } ?: 0
                if (messageLength > 4000) {
                    throw IllegalStateException("Thread content too long for ${entry.name}: $firstCreateMessage")
                }
                val secondCreateMessage = CreateMessage("Reserved")
                return MessageData(
                    first = firstCreateMessage,
                    imageAttachments = listOfNotNull(
                        thumbnailImage?.let { it.first to AlleyDataRes.readBytes(it.second.first) },
                        headerImage?.let { it.first to AlleyDataRes.readBytes(it.second.first) },
                    ),
                    second = secondCreateMessage,
                )
            }
        }

        val booth = entry.booth?.let(Booth::fromStringOrNull)

        val threadTitle = "${entry.booth} - ${entry.name}".also {
            if (it.length > 100) {
                throw IllegalStateException("Thread title too long: $it")
            }
        }
    }

    private data class ThreadWrapper(val thread: Thread) {
        val booth: Booth?
        val artistName: String?

        init {
            val pieces = thread.name?.split("-")?.map { it.trim() }
            booth = pieces?.firstOrNull()?.let(Booth::fromStringOrNull)
            artistName = pieces?.getOrNull(1)
        }
    }

    private data class ThreadWithContent(
        val thread: ThreadWrapper,
        val firstMessage: Message,
        val secondMessage: Message,
    )

}
