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
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.discord.ChannelFlag
import com.thekeeperofpie.artistalleydatabase.discord.ForumLayout
import com.thekeeperofpie.artistalleydatabase.discord.Message
import com.thekeeperofpie.artistalleydatabase.discord.Thread
import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.nio.file.Files
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

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
                firstMessage = message,
            )
        } else {
            DiscordApi.editMessage(
                channelId = existingPin.id,
                messageId = existingPin.id,
                message = message,
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
                if (thread.messageHash != artist.messageHash) {
                    delay(THROTTLE_DELAY)
                    println("Fetching ${thread.thread.name}")
                    val firstMessage =
                        DiscordApi.getChannelMessage(thread.thread.id, thread.thread.id)
                    val secondMessage = DiscordApi.getOldestThreadMessage(thread.thread.id)
                    val original = firstMessage.content.lines() + secondMessage.content.lines()
                    val revised =
                        artist.threadContent.first.lines() + artist.threadContent.second.lines()
                    val diff = DiffRowGenerator().generateDiffRows(original, revised)
                    println("Diff for ${thread.thread.name}: ")
                    diff.withIndex()
                        .filter { it.value.tag != DiffRow.Tag.EQUAL }
                        .forEach { println("\t ${it.index}: ${it.value}") }
                    changed += artist to ThreadWithContent(thread, firstMessage, secondMessage)
                }
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
            )
            println("Created ${it.threadTitle}")
        }

        changed.forEach {
            if (it.second.thread.messageHash != it.first.messageHash) {
                delay(THROTTLE_DELAY)
                DiscordApi.editMessage(
                    channelId = it.second.thread.thread.id,
                    messageId = it.second.firstMessage.id,
                    message = it.first.threadContent.first,
                )
                println("Edited ${it.second.firstMessage.id}")
                delay(THROTTLE_DELAY)
                DiscordApi.editMessage(
                    channelId = it.second.thread.thread.id,
                    messageId = it.second.secondMessage.id,
                    message = it.first.threadContent.second,
                )
                println("Edited ${it.second.secondMessage.id}")
                println("Edited ${it.second.thread.thread.name}")
            }
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

    private data class Artist(
        val entry: ArtistEntryAnimeExpo2026,
        val seriesInferred: List<String>,
        val seriesConfirmed: List<String>,
    ) {
        companion object {
            fun fromEntry(entry: ArtistEntryAnimeExpo2026, series: Map<String, SeriesInfo>) =
                Artist(
                    entry = entry,
                    seriesInferred = entry.seriesInferred.mapSeriesNames(series),
                    seriesConfirmed = entry.seriesConfirmed.mapSeriesNames(series),
                )

            private fun List<String>.mapSeriesNames(series: Map<String, SeriesInfo>) =
                map { series[it]?.name(AniListLanguageOption.DEFAULT) ?: it }
        }

        val booth = entry.booth?.let(Booth::fromStringOrNull)

        val threadTitleWithoutHash = "${entry.booth} - ${entry.name}"

        val threadContent = buildMessage()

        val messageHash = threadContent.hashCode().absoluteValue.toString()

        val threadTitle =
            "$threadTitleWithoutHash - $messageHash".also {
                if (it.length > 100) {
                    throw IllegalStateException("Thread title too long: $it")
                }
            }

        private fun buildMessage(): Pair<String, String> {
            val name = entry.name
            val summary = buildString {
                if (!entry.summary.isNullOrBlank()) {
                    appendLine(entry.summary)
                }
            }

            val portfolioLinks = buildString {
                if (entry.portfolioLinks.isNotEmpty()) {
                    appendLine("### Portfolio")
                    entry.portfolioLinks.forEach {
                        appendLine("- $it")
                    }
                }
            }

            val socialLinks = buildString {
                if (entry.socialLinks.isNotEmpty()) {
                    appendLine("### Links")
                    entry.socialLinks.forEach {
                        appendLine("- $it")
                    }
                }
            }

            val storeLinks = buildString {
                if (entry.storeLinks.isNotEmpty()) {
                    appendLine("### Store")
                    entry.storeLinks.forEach {
                        appendLine("- $it")
                    }
                }
            }

            val commissions = buildString {
                if (entry.commissions.isNotEmpty()) {
                    appendLine("### Commissions")
                    entry.commissions.forEach {
                        appendLine("- $it")
                    }
                }
            }

            val series = buildString {
                if (seriesConfirmed.isNotEmpty()) {
                    appendLine("### Series")
                    seriesConfirmed.forEach {
                        appendLine("- $it")
                    }
                } else if (seriesInferred.isNotEmpty()) {
                    appendLine("### Series - Unconfirmed")
                    seriesInferred.forEach {
                        appendLine("- $it")
                    }
                }
            }

            val merch = buildString {
                if (entry.merchConfirmed.isNotEmpty()) {
                    appendLine("### Merch")
                    entry.merchConfirmed.forEach {
                        appendLine("- $it")
                    }
                } else if (entry.merchInferred.isNotEmpty()) {
                    appendLine("### Merch - Unconfirmed")
                    entry.merchInferred.forEach {
                        appendLine("- $it")
                    }
                }
            }

            // Portfolio goes first to ensure image preview shows artwork
            val parts = listOf(
                name,
                summary,
                portfolioLinks,
                socialLinks,
                storeLinks,
                commissions,
                series,
                merch
            )
            var firstMessage = ""
            var secondMessage = ""
            parts.map { it.take(2000) }
                .forEach {
                    if (secondMessage.isEmpty() && firstMessage.length + it.length < 2000) {
                        firstMessage += it
                    } else {
                        secondMessage += it
                    }
                }

            if (secondMessage.length >= 2000) {
                throw IllegalStateException("Thread content too long for $threadTitleWithoutHash: $secondMessage")
            }

            return firstMessage to secondMessage
        }
    }

    private data class ThreadWrapper(val thread: Thread) {
        val booth: Booth?
        val artistName: String?
        val messageHash: String?

        init {
            val pieces = thread.name?.split("-")?.map { it.trim() }
            booth = pieces?.firstOrNull()?.let(Booth::fromStringOrNull)
            artistName = pieces?.getOrNull(1)
            messageHash = pieces?.getOrNull(2)
        }
    }

    private data class ThreadWithContent(
        val thread: ThreadWrapper,
        val firstMessage: Message,
        val secondMessage: Message,
    )
}
