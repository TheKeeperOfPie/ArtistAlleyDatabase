package com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference

import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
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
                    socialLinks = it.value.flatMap { it.socialLinks }
                        .map(LinkModel.Companion::parse).toSet(),
                    storeLinks = it.value.flatMap { it.storeLinks }.map(LinkModel.Companion::parse)
                        .toSet(),
                    portfolioLinks = it.value.flatMap { it.portfolioLinks }
                        .map(LinkModel.Companion::parse)
                        .toSet(),
                    catalogLinks = it.value.flatMap { it.catalogLinks }.toSet(),
                )
            }
            .values
    }.shareIn(applicationScope, SharingStarted.Companion.Lazily, replay = 1)

    suspend fun hasPreviousYear(artistId: String): Boolean = DataYear.entries.any {
        artistEntryDao.getEntry(it, artistId) != null
    }

    suspend fun inferArtist(input: Input): List<MatchResult> {
        if (input.name.length <= 3 &&
            input.socialLinks.isEmpty() &&
            input.storeLinks.isEmpty() &&
            input.portfolioLinks.isEmpty() &&
            input.catalogLinks.isEmpty()
        ) {
            return emptyList()
        }
        return artistInferenceData.first()
            .flatMapIndexed { index, data ->
                if (index % 20 == 0) {
                    yield()
                }

                matchingLinks(data, data.socialLinks, input.socialLinks) +
                        matchingLinks(data, data.storeLinks, input.storeLinks) +
                        matchingLinks(data, data.portfolioLinks, input.portfolioLinks) +
                        matchingCatalogLinks(data, data.catalogLinks, input.catalogLinks) +
                        data.names.map {
                            MatchResult.Name(data, StringUtils.compareSimilarity(it, input.name))
                        }
            }
            .filter { it.score > 0.6f }
            .sortedByDescending { it.score }
            .distinctBy { it.data.id }
            .take(5)
    }

    suspend fun getPreviousYearData(artistId: Uuid) =
        PreviousYearProvider(artistEntryDao, artistId.toString()).getData()
            .takeIf {
                !it.summary.isNullOrBlank() ||
                        it.socialLinks.isNotEmpty() ||
                        it.storeLinks.isNotEmpty() ||
                        it.seriesInferred.isNotEmpty() ||
                        it.merchInferred.isNotEmpty()
            }

    private fun matchingLinks(
        data: ArtistData,
        artistLinks: Set<LinkModel>,
        inputLinks: List<LinkModel>,
    ): List<MatchResult.Link> {
        val results = mutableListOf<MatchResult.Link>()
        artistLinks.forEach { artistLink ->
            inputLinks.forEach { inputLink ->
                if (artistLink.logo != inputLink.logo) return@forEach
                val score = StringUtils.compareSimilarity(
                    artistLink.identifier.removePrefix("https"),
                    inputLink.identifier.removePrefix("https"),
                )
                val result = MatchResult.Link(data, score, artistLink.link, inputLink.link)

                // If any high score, just exit and return
                if (score >= 0.8f) return listOf(result)
                results += result
            }
        }
        return results
    }

    private fun matchingCatalogLinks(
        data: ArtistData,
        artistLinks: Set<String>,
        inputLinks: List<String>,
    ): List<MatchResult.Link> {
        val results = mutableListOf<MatchResult.Link>()
        artistLinks.forEach { artistLink ->
            inputLinks.forEach { inputLink ->
                val score = StringUtils.compareSimilarity(
                    artistLink.removePrefix("https"),
                    inputLink.removePrefix("https"),
                )
                val result = MatchResult.Link(data, score, artistLink, inputLink)

                // If any high score, just exit and return
                if (score >= 0.8f) return listOf(result)
                results += result
            }
        }
        return results
    }

    sealed interface MatchResult {
        val data: ArtistData
        val score: Float

        val name get() = data.names.first()
        val via
            get() = when (this) {
                is Link -> "$artistLink -> $matchingInputLink"
                is Name -> name
            }

        data class Name(override val data: ArtistData, override val score: Float) : MatchResult
        data class Link(
            override val data: ArtistData,
            override val score: Float,
            val artistLink: String,
            val matchingInputLink: String,
        ) : MatchResult
    }

    data class ArtistData(
        val id: Uuid,
        val names: Set<String>,
        val socialLinks: Set<LinkModel>,
        val storeLinks: Set<LinkModel>,
        val portfolioLinks: Set<LinkModel>,
        val catalogLinks: Set<String>,
    )

    data class Input(
        val name: String,
        val socialLinks: List<LinkModel>,
        val storeLinks: List<LinkModel>,
        val portfolioLinks: List<LinkModel>,
        val catalogLinks: List<String>,
    ) {
        companion object {
            fun captureState(state: ArtistFormState) = Input(
                name = state.info.name.value.text.toString(),
                socialLinks = state.links.socialLinks.toList() +
                        LinkModel.Companion.parse(state.links.stateSocialLinks.value.text.toString()),
                storeLinks = state.links.storeLinks.toList() +
                        LinkModel.Companion.parse(state.links.stateStoreLinks.value.text.toString()),
                portfolioLinks = state.links.portfolioLinks.toList() +
                        LinkModel.Companion.parse(state.links.statePortfolioLinks.value.text.toString()),
                catalogLinks = state.links.catalogLinks.toList() +
                        state.links.stateCatalogLinks.value.text.toString(),
            )
        }
    }

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

        suspend fun getData() = ArtistPreviousYearData(
            artistId = artistId,
            name = cascadeString { name },
            summary = cascadeString { summary },
            socialLinks = cascadeLists { socialLinks },
            storeLinks = cascadeLists { storeLinks },
            seriesInferred = cascadeLists { seriesConfirmed.ifEmpty { seriesInferred } },
            merchInferred = cascadeLists { merchConfirmed.ifEmpty { merchInferred } },
        )
    }
}
