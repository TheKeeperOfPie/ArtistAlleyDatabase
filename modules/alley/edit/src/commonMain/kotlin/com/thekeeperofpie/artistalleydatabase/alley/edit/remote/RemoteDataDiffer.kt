package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistRemoteEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LinkCategory
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.category
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.readString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant

@SingleIn(AppScope::class)
@Inject
class RemoteDataDiffer(
    val editDatabase: AlleyEditDatabase,
) {
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    suspend fun calculateDiff(dataYear: DataYear, file: PlatformFile): List<ArtistRemoteEntry> {
        val fixedName = file.nameWithoutExtension.replace(';', ':')
        val timestamp = Instant.parseOrNull(fixedName)
            ?: Instant.parseOrNull("${fixedName}T00:00:00Z")
        return if (timestamp == null) {
            // TODO: Better surface errors
            emptyList()
        } else {
            val oldEntries = editDatabase.loadRemoteArtistDataForDiff(dataYear)
            val newEntries = parseEntries(file, timestamp)
            newEntries.mapNotNull { newEntry ->
                // Infer previous via an exact match of name and booth
                val previous = oldEntries[newEntry.id] ?: return@mapNotNull newEntry

                // Overwrite timestamp so that equality check passes
                val compareEntry = previous.copy(timestamp = timestamp)
                val updatedEntry = newEntry.copy(confirmedId = previous.confirmedId)
                updatedEntry.takeIf { compareEntry != updatedEntry }
            }
        }
    }

    private suspend fun parseEntries(file: PlatformFile, timestamp: Instant) =
        json.decodeFromString<List<Entry>>(file.readString())
            .map {
                ArtistRemoteEntry(
                    confirmedId = null,
                    booth = it.booth,
                    name = it.name,
                    summary = it.summary,
                    links = it.links.map { fixLink(it) },
                    timestamp = timestamp,
                )
            }
            .distinctBy { it.booth to it.name }
            .sortedBy { it.booth }

    private fun fixLink(link: String): String {
        val trimmedLink = if (link.lowercase().startsWith("http")) {
            link
        } else {
            "https://$link"
        }.removeSuffix("/").removeSuffix("#").lowercase()

        // TODO: There's more link types where this should apply
        val linkModel = LinkModel.parse(trimmedLink)
        if (linkModel.type.category == LinkCategory.SOCIALS) {
            return linkModel.link.substringBefore("?")
        }

        return trimmedLink
    }

    @Serializable
    data class Entry(
        val booth: String,
        val name: String,
        val links: List<String>,
        val summary: String?,
        val tags: List<String>,
    )
}
