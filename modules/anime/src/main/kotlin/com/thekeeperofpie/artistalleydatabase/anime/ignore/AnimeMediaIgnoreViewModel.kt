@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.ignore

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.fragment.MediaPreview
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.utils.RequestBatcher
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeMediaIgnoreViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    private val statusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    private val ignoreDao: AnimeIgnoreDao,
) : ViewModel() {

    var selectedType by mutableStateOf(settings.preferredMediaType.value)
    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)
    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    val content = MutableStateFlow(PagingData.empty<MediaPreviewWithDescriptionEntry>())

    private val localContentAnime =
        mutableStateMapOf<Int, Optional<WeakReference<MediaPreviewWithDescriptionEntry>>>()

    private val localContentManga =
        mutableStateMapOf<Int, Optional<WeakReference<MediaPreviewWithDescriptionEntry>>>()

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    private val mediaRequestBatcher = RequestBatcher(
        scope = viewModelScope,
        apiCall = {
            aniListApi.mediaByIds(
                it.map { it.toInt() },
                includeDescription = true
            )
        },
        resultToId = { it.id.toString() },
    )

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            combine(
                snapshotFlow { selectedType },
                refreshUptimeMillis,
                ignoreController.updates(),
                ::Triple,
            )
                .flatMapLatest { (mediaType) ->
                    Pager(PagingConfig(pageSize = 10, enablePlaceholders = true)) {
                        MediaIgnorePagingSource(ignoreDao, mediaType)
                    }.flow
                }
                .map {
                    it.mapNotNull {
                        mediaRequestBatcher.fetch(it.id)?.let(::MediaPreviewWithDescriptionEntry)
                    }
                }
                .enforceUniqueIds { it.id.valueId }
                .cachedIn(viewModelScope)
                .applyMediaStatusChanges(
                    statusController = statusController,
                    ignoreController = ignoreController,
                    settings = settings,
                    media = { it.media },
                    forceShowIgnored = true,
                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                        copy(
                            media = media,
                            mediaListStatus = mediaListStatus,
                            progress = progress,
                            progressVolumes = progressVolumes,
                            scoreRaw = scoreRaw,
                            ignored = ignored,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                        )
                    },
                )
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(content::emit)
        }
    }

    fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }

    fun onMediaLongClick(media: MediaPreview) = ignoreController.toggle(media)

    fun placeholder(index: Int, mediaType: MediaType): MediaPreviewWithDescriptionEntry? {
        val localContent = if (mediaType == MediaType.ANIME) {
            localContentAnime
        } else {
            localContentManga
        }

        val optional = localContent[index]
        if (optional != null) {
            // Check if still loading
            if (!optional.isPresent) return null
            optional.getOrNull()?.get()?.let { return it }
        }
        localContent[index] = Optional.empty()
        viewModelScope.launch(CustomDispatchers.IO) {
            val entry = try {
                ignoreDao.getEntryAtIndex(index, mediaType)
            } catch (ignored: Throwable) {
                return@launch
            } ?: return@launch

            val result = Optional.of(
                WeakReference(MediaPreviewWithDescriptionEntry(PlaceholderMediaEntry(entry)))
            )
            withContext(CustomDispatchers.Main) {
                localContent[index] = result
            }
        }
        return null
    }

    data class PlaceholderMediaEntry(
        private val entry: AnimeMediaIgnoreEntry,
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
}
