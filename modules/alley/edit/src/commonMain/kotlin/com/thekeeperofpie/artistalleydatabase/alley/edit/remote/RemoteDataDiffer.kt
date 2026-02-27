package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

import com.fleeksoft.ksoup.Ksoup
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistRemoteEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.readString
import kotlin.time.Instant

@SingleIn(AppScope::class)
@Inject
class RemoteDataDiffer(
    val editDatabase: AlleyEditDatabase,
) {
    suspend fun calculateDiff(dataYear: DataYear, file: PlatformFile): List<ArtistRemoteEntry> {
        val timestamp = Instant.parseOrNull(file.nameWithoutExtension)
            ?: Instant.parseOrNull("${file.nameWithoutExtension}T00:00:00Z")
        return if (timestamp == null) {
            // TODO: Better surface errors
            emptyList()
        } else {
            val oldEntries = editDatabase.loadRemoteArtistData(dataYear)
            val newEntries = parseEntries(file, timestamp)
            newEntries.mapNotNull { newEntry ->
                // Infer previous via an exact match of name and booth
                val previous = oldEntries.find { oldEntry ->
                    newEntry.name == oldEntry.name &&
                            newEntry.booth == oldEntry.booth
                } ?: return@mapNotNull newEntry

                // Overwrite timestamp so that equality check passes
                val compareEntry = previous.copy(timestamp = timestamp)
                val updatedEntry = newEntry.copy(confirmedId = previous.confirmedId)
                updatedEntry.takeIf { compareEntry != updatedEntry }
            }
        }
    }


    private suspend fun parseEntries(file: PlatformFile, timestamp: Instant) =
        Ksoup.parse(file.readString())
            .select(".event")
            .map {
                val title = it.selectFirst(".title")
                val name = title?.selectFirst("a")?.text()?.ifEmpty { null } ?: title?.text()
                val link = title?.selectFirst("a")?.attr("href")
                val socials = title?.selectFirst(".socials").let {
                    try {
                        it?.select("a")?.map { it.attr("href") }
                    } catch (_: Throwable) {
                        null
                    }
                }
                val booth = it.selectFirst(".channel")?.selectFirst("span")?.text()?.let {
                    val letter = it.firstOrNull() ?: return@let null
                    val number = it.drop(1).padStart(2, '0')
                    "$letter$number"
                }
                val website = it.selectFirst(".start")
                    ?.selectFirst("a")
                    ?.attr("href")
                    ?.removeSuffix("/")
                val summary = it.selectFirst(".desc")?.text()
                val links = (socials.orEmpty() + link + website)
                    .filterNotNull()
                    .map(::fixLink)
                    .distinct()
                    .sorted()
                ArtistRemoteEntry(
                    confirmedId = null,
                    booth = booth,
                    name = name,
                    summary = summary,
                    links = links,
                    timestamp = timestamp,
                )
            }
            .sortedBy { it.booth }

    private fun fixLink(link: String) = if (link.lowercase().startsWith("http")) {
        link
    } else {
        "https://$link"
    }.removeSuffix("/").lowercase()
}
