package com.thekeeperofpie.artistalleydatabase.anime.user.favorite

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.fragment.UserFavoriteMedia
import com.anilist.type.MediaFormat
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges2
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserFavoriteMediaViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val userId = savedStateHandle.get<String?>("userId")
    val mediaType = savedStateHandle.get<String?>("mediaType")
        ?.let { MediaType.safeValueOf(it).takeUnless { it == MediaType.UNKNOWN__ } }
        ?: MediaType.ANIME

    val viewer = aniListApi.authedUser
    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)
    val media = MutableStateFlow(PagingData.empty<MediaPreviewWithDescriptionEntry>())

    private val refresh = MutableStateFlow(-1L)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                MediaUtils.mediaViewOptionIncludeDescriptionFlow { mediaViewOption },
                viewer,
                refresh,
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
                .mapLatest { it.mapOnIO { MediaPreviewWithDescriptionEntry(MediaWrapper(it)) } }
                .enforceUniqueIds { it.mediaId }
                .cachedIn(viewModelScope)
                .applyMediaStatusChanges2(
                    statusController = mediaListStatusController,
                    ignoreController = ignoreController,
                    settings = settings,
                )
                .cachedIn(viewModelScope)
                .collectLatest(media::emit)
        }
    }

    fun refresh() {
        refresh.value = SystemClock.uptimeMillis()
    }

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
}
