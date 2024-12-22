@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class MediaHistoryViewModel<MediaEntry : Any>(
    aniListApi: AuthedAniListApi,
    historySettings: HistorySettings,
    mediaDataSettings: MediaDataSettings,
    mediaListStatusController: MediaListStatusController,
    val ignoreController: IgnoreController,
    private val historyDao: AnimeHistoryDao,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaEntry>,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<HistoryDestinations.MediaHistory>(navigationTypeMap)
    val enabled = historySettings.mediaHistoryEnabled
    var selectedType =
        MutableStateFlow(destination.mediaType ?: mediaDataSettings.preferredMediaType.value)
    var mediaViewOption = MutableStateFlow(mediaDataSettings.mediaViewOption.value)
    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    val content = MutableStateFlow(PagingData.empty<MediaEntry>())

//    private val localContentAnime =
//        mutableStateMapOf<Int, Optional<WeakReference<MediaPreviewWithDescriptionEntry>>>()
//
//    private val localContentManga =
//        mutableStateMapOf<Int, Optional<WeakReference<MediaPreviewWithDescriptionEntry>>>()

    // TODO: Re-enable network fetch by migrating to custom paginator
//    private val mediaRequestBatcher = RequestBatcher(
//        scope = viewModelScope,
//        apiCall = {
//            aniListApi.mediaByIds(
//                it.map { it.toInt() },
//                includeDescription = true
//            )
//        },
//        resultToId = { it.id.toString() },
//    )

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            selectedType
                .flatMapLatest {
                    Pager(config = PagingConfig(pageSize = 10)) {
                        historyDao.getEntries(it)
//                        MediaHistoryPagingSource(historyDao, it)
                    }.flow
                }
                .map {
                    it.mapNotNull {
                        mediaEntryProvider.mediaEntry(PlaceholderMediaEntry(it))
//                        mediaRequestBatcher.fetch(it.id)?.let(::MediaPreviewWithDescriptionEntry)
                    }
                }
                .enforceUniqueIds(mediaEntryProvider::id)
                .cachedIn(viewModelScope)
                .applyMediaStatusChanges(
                    statusController = mediaListStatusController,
                    ignoreController = ignoreController,
                    mediaFilteringData = mediaDataSettings.mediaFilteringData(false),
                    mediaFilterable = mediaEntryProvider::mediaFilterable,
                    copy = { mediaEntryProvider.copyMediaEntry(this, it) },
                )
                .collectLatest(content::emit)
        }
    }

    fun placeholder(index: Int, mediaType: MediaType): MediaEntry? {
        return null
//        val localContent = if (mediaType == MediaType.ANIME) {
//            localContentAnime
//        } else {
//            localContentManga
//        }
//
//        val optional = localContent[index]
//        if (optional != null) {
//            // Check if still loading
//            if (!optional.isPresent) return null
//            optional.getOrNull()?.get()?.let { return it }
//        }
//        localContent[index] = Optional.empty()
//        viewModelScope.launch(CustomDispatchers.IO) {
//            val entry = try {
//                historyDao.getEntryAtIndex(index)
//            } catch (ignored: Throwable) {
//                return@launch
//            } ?: return@launch
//
//            val result = Optional.of(
//                WeakReference(
//                    MediaPreviewWithDescriptionEntry(
//                        media = PlaceholderMediaEntry(entry)
//                    )
//                )
//            )
//            withContext(CustomDispatchers.Main) {
//                localContent[index] = result
//            }
//        }
//        return null
    }

    data class PlaceholderMediaEntry(
        private val entry: AnimeMediaHistoryEntry,
    ) : MediaPreviewWithDescription {
        override val __typename = "Default"
        override val id = entry.id.toInt()
        override val title = object : MediaPreviewWithDescription.Title {
            override val __typename = "Default"

            // TODO: User preferred title language
            override val userPreferred = entry.title.romaji
            override val romaji = entry.title.romaji
            override val english = entry.title.english
            override val native = entry.title.native

        }
        override val coverImage =
            object : MediaPreviewWithDescription.CoverImage {
                override val extraLarge = entry.coverImage
                override val color = null

            }
        override val type = entry.type
        override val isAdult = entry.isAdult
        override val bannerImage = entry.bannerImage
        override val format = null
        override val status = null
        override val season = null
        override val seasonYear = null
        override val averageScore = null
        override val popularity = null
        override val nextAiringEpisode = null
        override val isFavourite = false
        override val mediaListEntry = null
        override val tags = null
        override val episodes = null
        override val chapters = null
        override val volumes = null
        override val genres = null
        override val source = null
        override val startDate = null
        override val externalLinks = null
        override val description = null
    }

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val historySettings: HistorySettings,
        private val mediaDataSettings: MediaDataSettings,
        private val mediaListStatusController: MediaListStatusController,
        private val ignoreController: IgnoreController,
        private val historyDao: AnimeHistoryDao,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {
        fun <MediaEntry : Any> create(
            mediaEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaEntry>,
        ) = MediaHistoryViewModel(
            aniListApi = aniListApi,
            historySettings = historySettings,
            mediaDataSettings = mediaDataSettings,
            mediaListStatusController = mediaListStatusController,
            ignoreController = ignoreController,
            historyDao = historyDao,
            navigationTypeMap = navigationTypeMap,
            savedStateHandle = savedStateHandle,
            mediaEntryProvider = mediaEntryProvider,
        )
    }
}
