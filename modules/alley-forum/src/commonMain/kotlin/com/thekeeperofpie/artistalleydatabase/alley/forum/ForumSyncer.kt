package com.thekeeperofpie.artistalleydatabase.alley.forum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kmpalette.color
import com.kmpalette.palette.graphics.Palette
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.ColumnAdapters
import com.thekeeperofpie.artistalleydatabase.alley.data.toSeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.forum.alley_forum.generated.resources.Res
import com.thekeeperofpie.artistalleydatabase.alley.forum.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.fullName
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
import com.thekeeperofpie.artistalleydatabase.discord.MessageComponent
import com.thekeeperofpie.artistalleydatabase.discord.Thread
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.getString
import java.nio.file.Files
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import artistalleydatabase.modules.alley.data.generated.resources.Res as AlleyDataRes

@OptIn(ExperimentalSerializationApi::class)
internal class ForumSyncer(private val environment: Environment) {

    var error by mutableStateOf<String?>(null)

    private val THROTTLE_DELAY = 3.seconds

    private val api = DiscordApi(environment)

    suspend fun verifyChannel() {
        val channel = api.getChannel(environment.forumChannelId)
        if (channel.defaultForumLayout != ForumLayout.GALLERY_VIEW) {
            val errorMessage =
                "Directory channel is not gallery layout, was ${channel.defaultForumLayout}"
            println(errorMessage)
            error = errorMessage
        }
        val threads = api.getThreads(environment.forumChannelId)
        println("Threads = ${threads.threads.map { it.name }}")
    }

    suspend fun deleteAllThreads() {
        println("Deleting ALL threads")
        val threads = api.getThreads(environment.forumChannelId)
        threads.threads
            .filter {
                val flags = it.flags
                flags == null || (flags.flags and ChannelFlag.PINNED.flag) == 0
            }
            .forEach {
                delay(THROTTLE_DELAY)
                println("Deleting ${it.name}")
                api.deleteThread(it.id)
            }
    }

    suspend fun syncPinned() = withContext(Dispatchers.IO) {
        val existingPin = api.getThreads(environment.forumChannelId)
            .threads
            .find {
                val flags = it.flags ?: return@find false
                (flags.flags and ChannelFlag.PINNED.flag) != 0
            }
        val message = """
        |# Welcome to the AX AA Directory!
        |
        |This is a mirror of ${AlleyUtils.siteUrl}, providing an easy way to view all of the artists that will be tabling at ${
            getString(
                DataYear.ANIME_EXPO_2026.fullName
            )
        }. We encourage you to use the site instead as it offers a better UI, search by tags, and offline support.
        |## Attendees
        |Please be respectful in artists' threads. This is a space to converse with artists, ask them questions about the stuff they're selling, and get excited about all of the amazing art.
        |## Artists
        |If you would like to edit your data, verify yourself at the bottom of your artist page at ${AlleyUtils.siteUrl}.
        |
        |You may also use the AA Directory bot via `/aa verify [convention] [booth]`, which will unlock the private artist only channel and give you a form access link.
        |## Questions, suggestions, concerns
        |If you have any feedback for the directory itself, please visit us in<#${environment.publicArtistAlleyChannelId}> or contact <@${environment.contactUserId}>.
        """.trimMargin()
        val imageName = "files/forum_pin_banner.png"
        val imageAttachments = listOf(imageName to Res.readBytes(imageName))
        if (existingPin == null) {
            api.createThread(
                channelId = environment.forumChannelId,
                title = "What is this?",
                firstMessage = CreateMessage(content = message),
                imageAttachments = imageAttachments,
            )
        } else {
            api.editMessage(
                channelId = existingPin.id,
                messageId = existingPin.id,
                message = CreateMessage(content = message),
                imageAttachments = imageAttachments,
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

        val threadsList = api.getThreads(environment.forumChannelId)
        println("Threads = ${threadsList.threads.map { it.name }}")
        if (threadsList.hasMore) {
            // TODO: Not sure if this is required
            throw UnsupportedOperationException("Threads has more, unhandled")
        }

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
                val (secondMessage, firstMessage) = api.getOldestThreadMessages(thread.thread.id)
                changed += artist to ThreadWithContent(thread, firstMessage, secondMessage)
            }
        }

        missing.sortBy { it.entry.booth }
        changed.sortBy { it.first.booth }

        println("Missing = ${missing.map { it.threadTitle }}")
        println("Changed = ${changed.map { it.first.threadTitle }}")
        if (range == null) {
            println("Removed = ${threads.map { it.thread.name }}")
        }

        missing.forEach {
            delay(THROTTLE_DELAY)
            api.createThread(
                channelId = environment.forumChannelId,
                title = it.threadTitle,
                firstMessage = it.threadContent.first,
                secondMessage = it.threadContent.second,
                imageAttachments = it.threadContent.imageAttachments,
            )
            println("Created ${it.threadTitle}")
        }

        changed.forEach {
            delay(THROTTLE_DELAY)
            api.editMessage(
                channelId = it.second.thread.thread.id,
                messageId = it.second.firstMessage.id,
                message = it.first.threadContent.first,
                imageAttachments = it.first.threadContent.imageAttachments,
            )
            println("Edited ${it.second.firstMessage.id}")
            delay(THROTTLE_DELAY)
//            api.editMessage(
//                channelId = it.second.thread.thread.id,
//                messageId = it.second.secondMessage.id,
//                message = it.first.threadContent.second,
//            )
//            println("Edited ${it.second.secondMessage.id}")
            println("Edited ${it.second.thread.thread.name}")
            if (it.second.thread.thread.name != it.first.threadTitle) {
                delay(THROTTLE_DELAY)
                api.modifyThread(
                    threadId = it.second.thread.thread.id,
                    name = it.first.threadTitle
                )
                println("Updated ${it.second.thread.thread.name} -> ${it.first.threadTitle}")
            }
        }

        if (range == null) {
            threads.forEach {
                delay(THROTTLE_DELAY)
                api.modifyThread(threadId = it.thread.id, archived = true)
            }
        }
        println("Finished syncing threads")
    }

    private suspend fun createDriver(): SqlDriver = withContext(Dispatchers.IO) {
        val file = Files.createTempFile(null, ".sqlite").toFile()
        file.deleteOnExit()
        file.writeBytes(AlleyDataRes.readBytes("files/database.sqlite"))
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

                val headerSwatches = headerImage?.second?.first
                    ?.let { AlleyDataRes.readBytes(it) }
                    ?.let { Palette.from(it.decodeToImageBitmap()).generate() }
                    ?.swatches?.sortedByDescending { it.population }

                val imageEmbed = headerImage?.let {
                    Embed(
                        type = Embed.Type.IMAGE,
                        image = Embed.Image(url = "attachment://${it.first}", flags = 0),
                    )
                }

                val thumbnailImage = entry.embeds
                    ?.let(AlleyImageUtils::getProfileImageWithPath)
                    ?.let { "${Uuid.random()}${it.first}" to it }

                val thumbnailSwatches = thumbnailImage?.second?.first
                    ?.let { AlleyDataRes.readBytes(it) }
                    ?.let { Palette.from(it.decodeToImageBitmap()).generate() }
                    ?.swatches?.sortedByDescending { it.population }

                val titleEmbed = Embed(
                    thumbnail = thumbnailImage?.let {
                        Embed.Image(
                            url = "attachment://${it.first}",
                            flags = 0,
                        )
                    },
                    title = "\uD83C\uDFA8 ${entry.name}",
                    description = entry.summary?.takeIf { it.isNotBlank() }
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

                val seriesOne = mutableListOf<String>()
                val seriesTwo = mutableListOf<String>()
                val seriesThree = mutableListOf<String>()

                var seriesOneCharCount = 0
                var seriesTwoCharCount = 0
                series.forEach {
                    if ((seriesOneCharCount + it.length + 2) < 1000) {
                        seriesOneCharCount += it.length + 2
                        seriesOne += it
                    } else if ((seriesTwoCharCount + it.length + 2) < 1000) {
                        seriesTwoCharCount += it.length + 2
                        seriesTwo += it
                    } else {
                        seriesThree += it
                    }
                }

                val seriesOneEmbed = seriesTitle?.let {
                    Embed(
                        fields = listOf(
                            Embed.Field(
                                name = "\uD83C\uDFF7 $it",
                                value = seriesOne.joinToString(prefix = "```", postfix = "```")
                            )
                        ),
                    )
                }

                val seriesTwoEmbed = seriesTitle
                    ?.takeIf { seriesTwo.isNotEmpty() }
                    ?.let {
                        Embed(
                            fields = listOf(
                                Embed.Field(
                                    name = "\uD83C\uDFF7 $it",
                                    value = seriesTwo.joinToString(prefix = "```", postfix = "```")
                                )
                            ),
                        )
                    }

                val seriesThreeEmbed = seriesTitle
                    ?.takeIf { seriesThree.isNotEmpty() }
                    ?.let {
                        Embed(
                            fields = listOf(
                                Embed.Field(
                                    name = "\uD83C\uDFF7 $it",
                                    value = seriesThree.joinToString(prefix = "```", postfix = "```")
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

                val allSwatches = headerSwatches.orEmpty() + thumbnailSwatches.orEmpty()
                val embeds = listOfNotNull(
                    imageEmbed,
                    titleEmbed,
                    linksEmbed,
                    seriesOneEmbed,
                    seriesTwoEmbed,
                    seriesThreeEmbed,
                    merchEmbed,
                ).mapIndexed { index, embed ->
                    embed.copy(
                        color = allSwatches.getOrNull(index % allSwatches.size.coerceAtLeast(1))
                            ?.color
                            ?.toArgb()
                            ?.let { it and 0x00FFFFFF },
                    )
                }
                val firstCreateMessage = CreateMessage(
                    content = null,
                    embeds = embeds.ifEmpty { null },
                    components = listOf(
                        MessageComponent.ActionRow(
                            MessageComponent.Button(
                                style = MessageComponent.Button.Style.LINK,
                                label = "Open in Directory",
                                url = BuildKonfig.directoryUrl +
                                        "/artist/${DataYear.ANIME_EXPO_2026.serializedName}/${entry.id}",
                            )
                        )
                    )
                )
                val messageLength = firstCreateMessage.embeds?.sumOf {
                    (it.title?.length ?: 0) + (it.fields?.sumOf { it.name.length + it.value.length }
                        ?: 0)
                } ?: 0
                if (messageLength > 4000) {
                    throw IllegalStateException("Thread content too long for ${entry.name}: $firstCreateMessage")
                }

                val violatingFields = firstCreateMessage.embeds?.flatMap {
                    it.fields?.filter { it.value.length > 1024 }.orEmpty()
                }
                if (!violatingFields.isNullOrEmpty()) {
                    throw IllegalStateException("Field values too long for ${entry.name}: $violatingFields")
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
