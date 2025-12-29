package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.Logo
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.StringUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.yield
import kotlin.uuid.Uuid

@Inject
class ArtistInference(
    private val applicationScope: ApplicationScope,
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

    suspend fun getArtistForMerge(artistId: Uuid): ArtistEntry? =
        listOf(
            // In order of data quality
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_NYC_2025,
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_NYC_2024,
        ).firstNotNullOfOrNull {
            artistEntryDao.getEntry(it, artistId.toString())?.artist
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
            fun captureState(state: ArtistFormState) = ArtistInference.Input(
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
}
