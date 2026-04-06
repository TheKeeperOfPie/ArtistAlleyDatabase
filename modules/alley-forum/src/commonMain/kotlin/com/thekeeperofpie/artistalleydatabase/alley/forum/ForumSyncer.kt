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
import com.thekeeperofpie.artistalleydatabase.alley.forum.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.discord.ChannelFlag
import com.thekeeperofpie.artistalleydatabase.discord.ForumLayout
import com.thekeeperofpie.artistalleydatabase.discord.Thread
import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.nio.file.Files
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
        )
        val artists = database.artistEntryAnimeExpo2026Queries.getArtists()
            .awaitAsList()
            .filterNot { it.booth.isNullOrBlank() }
            .filter {
                range ?: return@filter true
                val booth = it.booth?.let(Booth::fromStringOrNull)
                booth != null && booth in range
            }
            .map { it to it.threadContent }
            .shuffled()

        val threadsList = DiscordApi.getThreads(BuildKonfig.discordForumChannelId)
        println("Threads = ${threadsList.threads.map { it.name }}")

        val threads = threadsList.threads
            .filter {
                val flags = it.flags
                flags == null || (flags.flags and ChannelFlag.PINNED.flag) == 0
            }
            .map {
                delay(1.seconds)
                val threadBooth = it.name?.substringBefore("-")?.trim()
                    ?.let(Booth::fromStringOrNull)
                val message = if (threadBooth == null || range == null || threadBooth in range) {
                    println("Fetching ${it.name}")
                    DiscordApi.getChannelMessage(it.id, it.id)
                } else {
                    null
                }
                it to message
            }
            .toMutableSet()

        val missing = mutableListOf<ArtistEntryAnimeExpo2026>()
        val changed = mutableListOf<Pair<ArtistEntryAnimeExpo2026, Thread>>()
        artists.forEach { (artist, expectedContent) ->
            val pair = threads.find { it.first.name == artist.threadTitle }
            threads.remove(pair)
            if (pair == null) {
                missing += artist
            } else if (pair.second?.content?.trim()?.removeSuffix("\n") !=
                expectedContent.trim().removeSuffix("\n")
            ) {
                val original = pair.second?.content.orEmpty().lines()
                val revised = expectedContent.lines()
                val diff = DiffRowGenerator().generateDiffRows(original, revised)
                println("Diff for ${pair.first.name}: ")
                diff.withIndex()
                    .filter { it.value.tag != DiffRow.Tag.EQUAL }
                    .forEach { println("\t ${it.index}: ${it.value}") }
                changed += artist to pair.first
            }
        }

        println("Missing = ${missing.sortedBy { it.booth }.map { it.threadTitle }}")
        println("Changed = ${changed.sortedBy { it.first.booth }.map { it.first.threadTitle }}")
        if (range == null) {
            println("Removed = ${threads.map { it.first.name }}")
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
            DiscordApi.editMessage(
                channelId = it.second.id,
                messageId = it.second.id,
                message = it.first.threadContent,
            )
            if (it.second.name != it.first.threadTitle) {
                delay(1.seconds)
                DiscordApi.modifyThread(threadId = it.second.id, name = it.first.threadTitle)
            }
            println("Updated ${it.second.name}")
        }

        if (range == null) {
            threads.forEach {
                delay(1.seconds)
                DiscordApi.modifyThread(threadId = it.first.id, archived = true)
            }
        }
    }

    private val ArtistEntryAnimeExpo2026.threadTitle get() = "$booth - $name"
    private val ArtistEntryAnimeExpo2026.threadContent
        get() = buildString {
            if (!summary.isNullOrBlank()) {
                appendLine(summary)
            }

            // Portfolio goes first to ensure image preview shows artwork
            if (portfolioLinks.isNotEmpty()) {
                appendLine("### Portfolio")
                portfolioLinks.forEach {
                    appendLine("- $it")
                }
            }

            if (socialLinks.isNotEmpty()) {
                appendLine("### Links")
                socialLinks.forEach {
                    appendLine("- $it")
                }
            }

            if (storeLinks.isNotEmpty()) {
                appendLine("### Store")
                storeLinks.forEach {
                    appendLine("- $it")
                }
            }

            if (commissions.isNotEmpty()) {
                appendLine("### Commissions")
                commissions.forEach {
                    appendLine("- $it")
                }
            }

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

            if (merchConfirmed.isNotEmpty()) {
                appendLine("### Merch")
                merchConfirmed.forEach {
                    appendLine("- $it")
                }
            } else if (merchInferred.isNotEmpty()) {
                appendLine("### Merch - Unconfirmed")
                merchInferred.forEach {
                    appendLine("- $it")
                }
            }
        }

    private suspend fun createDriver(): SqlDriver = withContext(Dispatchers.IO) {
        val file = Files.createTempFile(null, ".sqlite").toFile()
        file.deleteOnExit()
        file.writeBytes(Res.readBytes("files/database.sqlite"))
        JdbcSqliteDriver("jdbc:sqlite:${file.absolutePath}")
    }
}
