@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.fragment.MediaPreviewWithDescription
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.chunked
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class MediaHistoryViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    val ignoreList: AnimeMediaIgnoreList,
    mediaListStatusController: MediaListStatusController,
    private val historyDao: AnimeHistoryDao,
) : ViewModel() {

    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)
    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    val content = MutableStateFlow(PagingData.empty<MediaPreviewWithDescriptionEntry>())

    private val localContent =
        mutableStateMapOf<Int, Optional<WeakReference<MediaPreviewWithDescriptionEntry>>>()

    private val mediaRequestChannel = Channel<Request<MediaPreviewWithDescription>>()

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            mediaRequestChannel.consumeAsFlow()
                .chunked(10, 1.seconds)
                .flatMapMerge { requests ->
                    flow<Pair<Request<MediaPreviewWithDescription>, MediaPreviewWithDescription?>> {
                        aniListApi.mediaByIds(requests.map { it.id.toInt() }, includeDescription = true)
                            .map { media -> requests.first { it.id.toInt() == media.id } to media }
                            .forEach { emit(it) }
                    }.catch {
                        requests.map { it to null }.forEach { emit(it) }
                    }
                }
                .collectLatest { it.first.result.complete(it.second) }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            Pager(config = PagingConfig(pageSize = 10, jumpThreshold = 10)) {
                MediaHistoryPagingSource(historyDao)
            }.flow
                .map {
                    it.mapNotNull {
                        getMedia(it.id)?.let(::MediaPreviewWithDescriptionEntry)
                    }
                }
                .enforceUniqueIds { it.id.valueId }
                .cachedIn(viewModelScope)
                .applyMediaStatusChanges(
                    statusController = mediaListStatusController,
                    ignoreList = ignoreList,
                    settings = settings,
                    media = { it.media },
                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                        copy(
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
                .collectLatest(content::emit)
        }
    }

    fun placeholder(index: Int): MediaPreviewWithDescriptionEntry? {
        val optional = localContent[index]
        if (optional != null) {
            // Check if still loading
            if (!optional.isPresent) return null
            optional.getOrNull()?.get()?.let { return it }
        }
        localContent[index] = Optional.empty()
        viewModelScope.launch(CustomDispatchers.IO) {
            val entry = try {
                historyDao.getEntryAtIndex(index)
            } catch (ignored: Throwable) {
                return@launch
            } ?: return@launch

            val result = Optional.of(
                WeakReference(
                    MediaPreviewWithDescriptionEntry(
                        media = PlaceholderMediaEntry(entry)
                    )
                )
            )
            withContext(CustomDispatchers.Main) {
                localContent[index] = result
            }
        }
        return null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getMedia(id: String): MediaPreviewWithDescription? {
        val request = Request<MediaPreviewWithDescription>(id)
        mediaRequestChannel.send(request)
        return select {
            request.result.onAwait { it }
            onTimeout(1.minutes) { null }
        }
    }

    // TODO: Refactor and share this logic
    private data class Request<T>(
        val id: String,
        val result: CompletableDeferred<T?> = CompletableDeferred(),
    )

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
}
