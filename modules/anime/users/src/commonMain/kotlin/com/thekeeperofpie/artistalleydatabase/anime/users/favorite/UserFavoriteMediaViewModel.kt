package com.thekeeperofpie.artistalleydatabase.anime.users.favorite

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.anilist.data.fragment.UserFavoriteMedia
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaSource
import com.anilist.data.type.MediaStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.users.UserDestinations
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class UserFavoriteMediaViewModel<MediaEntry : Any>(
    aniListApi: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: MediaDataSettings,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaEntry>,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<UserDestinations.UserFavoriteMedia>(navigationTypeMap)
    val userId = destination.userId
    val mediaType = destination.mediaType

    val viewer = aniListApi.authedUser
    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)
    val media = MutableStateFlow(PagingData.empty<MediaEntry>())

    private val refresh = RefreshFlow()

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                MediaDataUtils.mediaViewOptionIncludeDescriptionFlow(snapshotFlow { mediaViewOption }),
                viewer,
                refresh.updates,
                ::Triple,
            ).flatMapLatest { (includeDescription, viewer) ->
                val userId = userId ?: viewer?.id
                AniListPager {
                    if (mediaType == MediaType.ANIME) {
                        val result = aniListApi.userFavoritesAnime(
                            userId = userId!!,
                            includeDescription = includeDescription,
                            page = it,
                        )
                        result.user?.favourites?.anime?.pageInfo to
                                result.user?.favourites?.anime?.nodes?.filterNotNull().orEmpty()
                    } else {
                        val result = aniListApi.userFavoritesManga(
                            userId = userId!!,
                            includeDescription = includeDescription,
                            page = it,
                        )
                        result.user?.favourites?.manga?.pageInfo to
                                result.user?.favourites?.manga?.nodes?.filterNotNull().orEmpty()
                    }
                }
            }
                .mapLatest { it.mapOnIO { mediaEntryProvider.mediaEntry(MediaWrapper(it)) } }
                .enforceUniqueIds(mediaEntryProvider::id)
                .cachedIn(viewModelScope)
                .applyMediaStatusChanges(
                    statusController = mediaListStatusController,
                    ignoreController = ignoreController,
                    mediaFilteringData = settings.mediaFilteringData(false),
                    mediaFilterable = mediaEntryProvider::mediaFilterable,
                    copy = { mediaEntryProvider.copyMediaEntry(this, it) },
                )
                .cachedIn(viewModelScope)
                .collectLatest(media::emit)
        }
    }

    fun refresh() = refresh.refresh()

    data class MediaWrapper(
        val media: UserFavoriteMedia,
        override val __typename: String = media.__typename,
        override val id: Int = media.id,
        override val title: MediaPreviewWithDescription.Title? = media.title?.let {
            object : MediaPreviewWithDescription.Title {
                override val __typename = it.__typename
                override val userPreferred = it.userPreferred
                override val romaji = it.romaji
                override val english = it.english
                override val native = it.native
            }
        },
        override val coverImage: MediaPreviewWithDescription.CoverImage? = media.coverImage?.let {
            object : MediaPreviewWithDescription.CoverImage {
                override val extraLarge = it.extraLarge
                override val color = it.color
            }
        },
        override val type: MediaType? = media.type,
        override val isAdult: Boolean? = media.isAdult,
        override val bannerImage: String? = media.bannerImage,
        override val format: MediaFormat? = media.format,
        override val status: MediaStatus? = media.status,
        override val season: MediaSeason? = media.season,
        override val seasonYear: Int? = media.seasonYear,
        override val episodes: Int? = media.episodes,
        override val averageScore: Int? = media.averageScore,
        override val popularity: Int? = media.popularity,
        override val nextAiringEpisode: MediaPreviewWithDescription.NextAiringEpisode? = media.nextAiringEpisode?.let {
            object : MediaPreviewWithDescription.NextAiringEpisode {
                override val episode = it.episode
                override val airingAt = it.airingAt
            }
        },
        override val isFavourite: Boolean = media.isFavourite,
        override val chapters: Int? = media.chapters,
        override val volumes: Int? = media.volumes,
        override val mediaListEntry: MediaPreviewWithDescription.MediaListEntry? = media.mediaListEntry?.let {
            object : MediaPreviewWithDescription.MediaListEntry {
                override val id = it.id
                override val status = it.status
                override val progressVolumes = it.progressVolumes
                override val progress = it.progress
                override val score = it.score
            }
        },
        override val tags: List<MediaPreviewWithDescription.Tag?>? = emptyList(),
        override val genres: List<String?>? = media.genres,
        override val source: MediaSource? = media.source,
        override val startDate: MediaPreviewWithDescription.StartDate? = media.startDate?.let {
            object : MediaPreviewWithDescription.StartDate {
                override val __typename = "Default"
                override val year = it.year
                override val month = it.month
                override val day = it.day
            }
        },
        override val externalLinks: List<MediaPreviewWithDescription.ExternalLink?>? = media.externalLinks?.map {
            object : MediaPreviewWithDescription.ExternalLink {
                override val siteId = it?.siteId
            }
        },
        override val description: String? = media.description,
    ) : MediaPreviewWithDescription

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val mediaListStatusController: MediaListStatusController,
        private val ignoreController: IgnoreController,
        private val settings: MediaDataSettings,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {
        fun <MediaEntry : Any> create(
            mediaEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaEntry>,
        ) = UserFavoriteMediaViewModel(
            aniListApi = aniListApi,
            mediaListStatusController = mediaListStatusController,
            ignoreController = ignoreController,
            settings = settings,
            navigationTypeMap = navigationTypeMap,
            savedStateHandle = savedStateHandle,
            mediaEntryProvider = mediaEntryProvider,
        )
    }
}
