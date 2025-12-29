package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.Logo
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.StringUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.yield
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
class ArtistInference(
    applicationScope: ApplicationScope,
    private val artistEntryDao: ArtistEntryDao,
) {
    private val artistInferenceData = flowFromSuspend {
        (artistEntryDao.getArtistInferenceData(DataYear.ANIME_EXPO_2025) +
                artistEntryDao.getArtistInferenceData(DataYear.ANIME_NYC_2025) +
                artistEntryDao.getArtistInferenceData(DataYear.ANIME_EXPO_2024) +
                artistEntryDao.getArtistInferenceData(DataYear.ANIME_NYC_2024) +
                artistEntryDao.getArtistInferenceData(DataYear.ANIME_EXPO_2023))
            .groupBy { it.artistId }
            .mapValues {
                ArtistData(
                    id = it.key,
                    names = it.value.map { it.name }.toSet(),
                    links = it.value.flatMap { it.links }.map(LinkModel::parse).toSet(),
                    storeLinks = it.value.flatMap { it.storeLinks }.map(LinkModel::parse).toSet(),
                    catalogLinks = it.value.flatMap { it.catalogLinks }.toSet(),
                )
            }
            .values
    }.shareIn(applicationScope, SharingStarted.Lazily, replay = 1)

    suspend fun inferArtist(input: Input): List<MatchResult> {
        if (input.name.length <= 3 &&
            input.links.isEmpty() &&
            input.storeLinks.isEmpty() &&
            input.catalogLinks.isEmpty()
        ) {
            return emptyList()
        }
        return artistInferenceData.first()
            .mapIndexed { index, data ->
                if (index % 20 == 0) {
                    yield()
                }
                val matchingLink = matchingLink(data.links, input.links)
                if (matchingLink != null) {
                    return@mapIndexed MatchResult.Link(data, 1f, matchingLink.link)
                }
                val matchingStoreLink = matchingLink(data.storeLinks, input.storeLinks)
                if (matchingStoreLink != null) {
                    return@mapIndexed MatchResult.Link(data, 1f, matchingStoreLink.link)
                }
                val matchingCatalogLink = matchingCatalogLink(data.catalogLinks, input.catalogLinks)
                if (matchingCatalogLink != null) {
                    return@mapIndexed MatchResult.Link(data, 1f, matchingCatalogLink)
                }

                val matchingNameScore =
                    data.names.maxOf { StringUtils.compareSimilarity(it, input.name) }
                MatchResult.Name(data, matchingNameScore)
            }
            .filter { it.score > 0.5f }
            .sortedByDescending { it.score }
            .take(5)
    }

    suspend fun getPreviousYearData(artistId: Uuid) =
        PreviousYearProvider(artistEntryDao, artistId.toString()).getData()
            .takeIf {
                !it.summary.isNullOrBlank() ||
                        it.links.isNotEmpty() ||
                        it.storeLinks.isNotEmpty() ||
                        it.seriesInferred.isNotEmpty() ||
                        it.merchInferred.isNotEmpty()
            }

    private fun matchingLink(
        artistLinks: Set<LinkModel>,
        inputLinks: List<LinkModel>,
    ): LinkModel? = artistLinks.firstOrNull { artistLink ->
        inputLinks.any { inputLink ->
            artistLink.logo == inputLink.logo &&
                    StringUtils.compareSimilarity(
                        artistLink.identifier,
                        inputLink.identifier
                    ) > 0.8f
        }
    }

    private fun matchingCatalogLink(
        artistLinks: Set<String>,
        inputLinks: List<String>,
    ): String? = artistLinks.firstOrNull { artistLink ->
        inputLinks.any { inputLink ->
            StringUtils.compareSimilarity(artistLink, inputLink) > 0.8f
        }
    }

    sealed interface MatchResult {
        val data: ArtistData
        val score: Float

        val name get() = data.names.first()
        val via
            get() = when (this) {
                is Link -> link
                is Name -> data.links.find { it.logo == Logo.X || it.logo == Logo.BLUESKY }?.link
                    ?: data.storeLinks.firstOrNull()?.link
                    ?: data.catalogLinks.firstOrNull()
            }

        data class Name(override val data: ArtistData, override val score: Float) : MatchResult
        data class Link(
            override val data: ArtistData,
            override val score: Float,
            val link: String,
        ) : MatchResult
    }

    data class ArtistData(
        val id: Uuid,
        val names: Set<String>,
        val links: Set<LinkModel>,
        val storeLinks: Set<LinkModel>,
        val catalogLinks: Set<String>,
    )

    data class Input(
        val name: String,
        val links: List<LinkModel>,
        val storeLinks: List<LinkModel>,
        val catalogLinks: List<String>,
    ) {
        companion object {
            fun captureState(state: ArtistFormState) = Input(
                name = state.info.name.value.text.toString(),
                links = state.links.links.toList() +
                        LinkModel.parse(state.links.stateLinks.value.text.toString()),
                storeLinks = state.links.storeLinks.toList() +
                        LinkModel.parse(state.links.stateStoreLinks.value.text.toString()),
                catalogLinks = state.links.catalogLinks.toList() +
                        state.links.stateCatalogLinks.value.text.toString(),
            )
        }
    }

    data class PreviousYearData(
        val artistId: String,
        val name: String?,
        val summary: String?,
        val links: List<String>,
        val storeLinks: List<String>,
        val seriesInferred: List<String>,
        val merchInferred: List<String>,
    )

    private class PreviousYearProvider(
        private val dao: ArtistEntryDao,
        private val artistId: String,
    ) {
        private val animeExpo2023 = CompletableDeferred<ArtistEntry?>()
        private val animeExpo2024 = CompletableDeferred<ArtistEntry?>()
        private val animeExpo2025 = CompletableDeferred<ArtistEntry?>()
        private val animeNyc2024 = CompletableDeferred<ArtistEntry?>()
        private val animeNyc2025 = CompletableDeferred<ArtistEntry?>()

        private suspend fun animeExpo2023(): ArtistEntry? {
            if (!animeExpo2023.isCompleted) {
                animeExpo2023.complete(dao.getEntry(DataYear.ANIME_EXPO_2023, artistId)?.artist)
            }
            return animeExpo2023.getCompleted()
        }

        private suspend fun animeExpo2024(): ArtistEntry? {
            if (!animeExpo2024.isCompleted) {
                animeExpo2024.complete(dao.getEntry(DataYear.ANIME_EXPO_2024, artistId)?.artist)
            }
            return animeExpo2024.getCompleted()
        }

        private suspend fun animeExpo2025(): ArtistEntry? {
            if (!animeExpo2025.isCompleted) {
                animeExpo2025.complete(dao.getEntry(DataYear.ANIME_EXPO_2025, artistId)?.artist)
            }
            return animeExpo2025.getCompleted()
        }

        private suspend fun animeNyc2024(): ArtistEntry? {
            if (!animeNyc2024.isCompleted) {
                animeNyc2024.complete(dao.getEntry(DataYear.ANIME_NYC_2024, artistId)?.artist)
            }
            return animeNyc2024.getCompleted()
        }

        private suspend fun animeNyc2025(): ArtistEntry? {
            if (!animeNyc2025.isCompleted) {
                animeNyc2025.complete(dao.getEntry(DataYear.ANIME_NYC_2025, artistId)?.artist)
            }
            return animeNyc2025.getCompleted()
        }

        private inline fun List<String>?.ifNullOrEmpty(defaultValue: () -> List<String>?) =
            if (this.isNullOrEmpty()) defaultValue() else this

        private suspend fun cascadeLists(value: ArtistEntry.() -> List<String>): List<String> =
            // In order of data quality
            animeExpo2025()?.value()
                .ifNullOrEmpty { animeExpo2024()?.value() }
                .ifNullOrEmpty { animeNyc2025()?.value() }
                .ifNullOrEmpty { animeNyc2024()?.value() }
                .ifNullOrEmpty { animeExpo2023()?.value() }
                .orEmpty()

        private suspend fun cascadeString(value: ArtistEntry.() -> String?): String? =
            // In order of data quality
            animeExpo2025()?.value()?.takeIf { it.isNotBlank() }
                ?.ifEmpty { animeExpo2024()?.value()?.takeIf { it.isNotBlank() } }
                ?.ifEmpty { animeNyc2025()?.value()?.takeIf { it.isNotBlank() } }
                ?.ifEmpty { animeNyc2024()?.value()?.takeIf { it.isNotBlank() } }
                ?.ifEmpty { animeExpo2023()?.value()?.takeIf { it.isNotBlank() } }

        suspend fun getData() = PreviousYearData(
            artistId = artistId,
            name = cascadeString { name },
            summary = cascadeString { summary },
            links = cascadeLists { links },
            storeLinks = cascadeLists { storeLinks },
            seriesInferred = cascadeLists { seriesConfirmed.ifEmpty { seriesInferred } },
            merchInferred = cascadeLists { merchConfirmed.ifEmpty { merchInferred } },
        )
    }
}
