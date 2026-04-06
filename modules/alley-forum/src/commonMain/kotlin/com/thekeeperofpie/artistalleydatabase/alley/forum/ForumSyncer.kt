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

    suspend fun verifyChannel() {
        val channel = DiscordApi.getChannel(BuildKonfig.discordForumChannelId)
        if (channel.defaultForumLayout != ForumLayout.GALLERY_VIEW) {
            val errorMessage =
                "Directory channel is not gallery layout, was ${channel.defaultForumLayout}"
            println(errorMessage)
            error = errorMessage
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
                message = message,
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
        val changed = mutableListOf<Pair<Artist, ThreadWrapper>>()
        artists.forEach { artist ->
            val thread = threads.find {
                it.booth == artist.booth && it.artistName == artist.entry.name
            }
            threads.remove(thread)
            if (thread == null) {
                missing += artist
            } else {
                if (thread.messageHash != artist.messageHash) {
                    delay(1.seconds)
                    println("Fetching ${thread.thread.name}")
                    val message = DiscordApi.getChannelMessage(thread.thread.id, thread.thread.id)
                    val original = message.content.lines()
                    val revised = artist.threadContent.lines()
                    val diff = DiffRowGenerator().generateDiffRows(original, revised)
                    println("Diff for ${thread.thread.name}: ")
                    diff.withIndex()
                        .filter { it.value.tag != DiffRow.Tag.EQUAL }
                        .forEach { println("\t ${it.index}: ${it.value}") }
                    changed += artist to thread
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
            delay(1.seconds)
            DiscordApi.createThread(
                channelId = BuildKonfig.discordForumChannelId,
                title = it.threadTitle,
                message = it.threadContent,
            )
            println("Created ${it.threadTitle}")
        }

        changed.forEach {
            delay(1.seconds)
            if (it.second.messageHash != it.first.messageHash) {
                DiscordApi.editMessage(
                    channelId = it.second.thread.id,
                    messageId = it.second.thread.id,
                    message = it.first.threadContent,
                )
                println("Edited ${it.second.thread.name}")
            }
            if (it.second.thread.name != it.first.threadTitle) {
                delay(1.seconds)
                DiscordApi.modifyThread(
                    threadId = it.second.thread.id,
                    name = it.first.threadTitle
                )
                println("Updated ${it.second.thread.name} -> ${it.first.threadTitle}")
            }
        }

        if (range == null) {
            threads.forEach {
                delay(1.seconds)
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
        val seriesInferred: List<String?>,
        val seriesConfirmed: List<String?>,
    ) {
        companion object {
            fun fromEntry(entry: ArtistEntryAnimeExpo2026, series: Map<String, SeriesInfo>) =
                Artist(
                    entry = entry,
                    seriesInferred = entry.seriesInferred.mapSeriesNames(series),
                    seriesConfirmed = entry.seriesConfirmed.mapSeriesNames(series),
                )

            private fun List<String>.mapSeriesNames(series: Map<String, SeriesInfo>) =
                map { series[it]?.name(AniListLanguageOption.DEFAULT) }
        }

        val booth = entry.booth?.let(Booth::fromStringOrNull)

        val threadContent = buildString {
            if (!entry.summary.isNullOrBlank()) {
                appendLine(entry.summary)
            }

            // Portfolio goes first to ensure image preview shows artwork
            if (entry.portfolioLinks.isNotEmpty()) {
                appendLine("### Portfolio")
                entry.portfolioLinks.forEach {
                    appendLine("- $it")
                }
            }

            if (entry.socialLinks.isNotEmpty()) {
                appendLine("### Links")
                entry.socialLinks.forEach {
                    appendLine("- $it")
                }
            }

            if (entry.storeLinks.isNotEmpty()) {
                appendLine("### Store")
                entry.storeLinks.forEach {
                    appendLine("- $it")
                }
            }

            if (entry.commissions.isNotEmpty()) {
                appendLine("### Commissions")
                entry.commissions.forEach {
                    appendLine("- $it")
                }
            }

            if (seriesConfirmed.isNotEmpty()) {
                appendLine("### Series")
                seriesConfirmed.forEach { appendLine("- $it") }
            } else if (seriesInferred.isNotEmpty()) {
                appendLine("### Series - Unconfirmed")
                seriesInferred.forEach { appendLine("- $it") }
            }

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

        val messageHash = threadContent.hashCode().absoluteValue.toString()

        val threadTitle =
            "${entry.booth} - ${entry.name} - $messageHash".also {
                if (it.length > 100) {
                    throw IllegalStateException("Thread title too long: $it")
                }
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
        val message: Message?,
    )
}
