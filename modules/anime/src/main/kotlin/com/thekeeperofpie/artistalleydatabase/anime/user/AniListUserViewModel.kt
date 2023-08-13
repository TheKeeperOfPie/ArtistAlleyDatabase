package com.thekeeperofpie.artistalleydatabase.anime.user

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.anilist.MediaTitlesAndImagesQuery
import com.anilist.ToggleFollowMutation
import com.anilist.fragment.PaginationInfo
import com.anilist.fragment.UserFavoriteMediaNode
import com.anilist.fragment.UserMediaStatistics
import com.anilist.type.MediaListStatus
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.character.DetailsCharacter
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AniListUserViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    private val ignoreList: AnimeMediaIgnoreList,
    private val settings: AnimeSettings,
) : ViewModel() {

    val screenKey = "${AnimeNavDestinations.USER.id}-${UUID.randomUUID()}"
    private var initialized = false
    var userId: String? = null

    var entry by mutableStateOf<AniListUserScreen.Entry?>(null)
    val viewer = aniListApi.authedUser
    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    val anime = MutableStateFlow(PagingData.empty<MediaEntry>())
    val manga = MutableStateFlow(PagingData.empty<MediaEntry>())
    val characters = MutableStateFlow(PagingData.empty<DetailsCharacter>())
    val staff = MutableStateFlow(PagingData.empty<DetailsStaff>())
    val studios = MutableStateFlow(PagingData.empty<StudioListRow.Entry>())

    val animeStats = States.Anime(viewModelScope, aniListApi)
    val mangaStats = States.Manga(viewModelScope, aniListApi)

    private var toggleFollowRequestMillis = MutableStateFlow(-1L)
    private var initialFollowState by mutableStateOf<Boolean?>(null)
    private var toggleFollowingResult by mutableStateOf<ToggleFollowMutation.Data.ToggleFollow?>(
        null
    )

    val isFollowing: Boolean
        get() = initialFollowState ?: toggleFollowingResult?.isFollowing ?: entry?.user?.isFollowing
        ?: false

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(userId: String?) {
        if (initialized) return
        initialized = true
        this.userId = userId

        viewModelScope.launch(CustomDispatchers.IO) {
            combine(refreshUptimeMillis, aniListApi.authedUser, ::Pair)
                .collectLatest { (_, viewer) ->
                    withContext(CustomDispatchers.Main) {
                        entry = null
                    }
                    try {
                        val userOrViewerId = userId ?: viewer?.id?.toString()
                        if (userOrViewerId == null) {
                            withContext(CustomDispatchers.Main) {
                                errorResource = R.string.anime_media_list_error_loading to null
                            }
                        } else {
                            entry = aniListApi.user(userOrViewerId)
                                ?.let(AniListUserScreen::Entry)
                        }
                    } catch (e: Exception) {
                        withContext(CustomDispatchers.Main) {
                            errorResource = R.string.anime_media_list_error_loading to e
                        }
                    }
                }
        }

        // TODO: Better placeholders for loading horizontal scrolling rows
        collectFavoritesPage(
            request = { entry, page ->
                if (page == 1) {
                    val result = entry.user.favourites?.anime
                    result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
                } else {
                    val result = aniListApi.userDetailsAnimePage(
                        userId = entry.user.id.toString(),
                        page = page,
                    )
                    result.pageInfo to result.nodes.filterNotNull()
                }
            },
            map = ::MediaEntry,
            id = { it.media.id.toString() },
            property = anime,
            transformFlow = {
                applyMediaStatusChanges(
                    statusController = statusController,
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
            }
        )

        collectFavoritesPage(
            request = { entry, page ->
                if (page == 1) {
                    val result = entry.user.favourites?.manga
                    result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
                } else {
                    val result = aniListApi.userDetailsMangaPage(
                        userId = entry.user.id.toString(),
                        page = page,
                    )
                    result.pageInfo to result.nodes.filterNotNull()
                }
            },
            map = ::MediaEntry,
            id = { it.media.id.toString() },
            property = manga,
            transformFlow = {
                applyMediaStatusChanges(
                    statusController = statusController,
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
            }
        )

        collectFavoritesPage(
            request = { entry, page ->
                if (page == 1) {
                    val result = entry.user.favourites?.characters
                    result?.pageInfo to result?.edges?.filterNotNull().orEmpty()
                } else {
                    val result = aniListApi.userDetailsCharactersPage(
                        userId = entry.user.id.toString(),
                        page = page,
                    )
                    result.pageInfo to result.edges.filterNotNull()
                }
            },
            map = CharacterUtils::toDetailsCharacter,
            id = { it.id },
            property = characters,
        )

        collectFavoritesPage(
            request = { entry, page ->
                if (page == 1) {
                    val result = entry.user.favourites?.staff
                    result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
                        .map { it to it.primaryOccupations }
                } else {
                    val result = aniListApi.userDetailsStaffPage(
                        userId = entry.user.id.toString(),
                        page = page,
                    )
                    result.pageInfo to result.nodes.filterNotNull()
                        .map { it to it.primaryOccupations }
                }
            },
            map = { (staff, primaryOccupations) ->
                DetailsStaff(
                    id = staff.id.toString(),
                    name = staff.name,
                    image = staff.image?.large,
                    role = primaryOccupations?.filterNotNull()?.firstOrNull(),
                    staff = staff,
                )
            },
            id = { it.idWithRole },
            property = staff,
        )

        collectFavoritesPage(
            request = { entry, page ->
                if (page == 1) {
                    val result = entry.user.favourites?.studios
                    result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
                } else {
                    val result = aniListApi.userDetailsStudiosPage(
                        userId = entry.user.id.toString(),
                        page = page,
                    )
                    result.pageInfo to result.nodes.filterNotNull()
                }
            },
            map = {
                StudioListRow.Entry(it, it.media?.nodes?.filterNotNull()?.map {
                    StudioListRow.Entry.MediaEntry(it, it.isAdult)
                }.orEmpty())
            },
            id = { it.studio.id.toString() },
            property = studios,
        )

        viewModelScope.launch(CustomDispatchers.IO) {
            toggleFollowRequestMillis.filter { it > 0 }
                .mapLatest { aniListApi.toggleFollow(userId!!.toInt()) }
                .catch {
                    // TODO: Error message
                }
                .collect {
                    withContext(CustomDispatchers.Main) {
                        initialFollowState = null
                        toggleFollowingResult = it
                    }
                }
        }
    }

    private fun <ResponseType : Any, ResultType : Any> collectFavoritesPage(
        request: suspend (AniListUserScreen.Entry, page: Int) -> Pair<PaginationInfo?, List<ResponseType>>,
        map: (ResponseType) -> ResultType,
        id: (ResultType) -> String,
        property: MutableStateFlow<PagingData<ResultType>>,
        transformFlow: (Flow<PagingData<ResultType>>.() -> Flow<PagingData<ResultType>>)? = null,
    ) {
        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { entry ->
                    Pager(config = PagingConfig(10)) {
                        AniListPagingSource { request(entry, it) }
                    }.flow
                }
                .mapLatest { it.map(map) }
                .enforceUniqueIds(id)
                .cachedIn(viewModelScope)
                .run {
                    if (transformFlow == null) this else {
                        transformFlow().cachedIn(viewModelScope)
                    }
                }
                .collectLatest(property::emit)
        }
    }

    fun refresh() = refreshUptimeMillis.tryEmit(System.currentTimeMillis())

    fun logOut() = aniListApi.logOut()

    fun toggleFollow() {
        userId ?: return
        initialFollowState = entry?.user?.isFollowing?.not() ?: true
        toggleFollowRequestMillis.value = SystemClock.uptimeMillis()
    }

    sealed class States(
        private val viewModelScope: CoroutineScope,
        private val aniListApi: AuthedAniListApi,
    ) {
        val genresState = State<UserMediaStatistics.Genre>(
            { it.genre.orEmpty() },
            { it.mediaIds.filterNotNull() },
        )

        val tagsState = State<UserMediaStatistics.Tag>(
            { it.tag?.id.toString() },
            { it.mediaIds.filterNotNull() },
        )

        val staffState = State<UserMediaStatistics.Staff>(
            { it.staff?.id.toString() },
            { it.mediaIds.filterNotNull() },
        )

        @Composable
        fun <Value> getMedia(
            value: Value,
            state: State<Value>,
        ): Result<Map<Int, MediaTitlesAndImagesQuery.Data.Page.Medium>?> {
            val key = state.valueToKey(value)
            return state.mediaFlows.getOrPut(key) {
                state.refreshRequest.filter { it == key }
                    .startWith(key)
                    .flowOn(CustomDispatchers.IO)
                    .map {
                        Result.success(
                            aniListApi.mediaTitlesAndImages(state.valueToMediaIds(value))
                                .associateBy { it.id }
                        )
                    }
                    .catch { emit(Result.failure(it)) }
                    .shareIn(viewModelScope, started = SharingStarted.Lazily, replay = 1)
            }
                .collectAsState(initial = Result.success(null))
                .value
        }

        class Anime(viewModelScope: CoroutineScope, aniListApi: AuthedAniListApi) :
            States(viewModelScope, aniListApi) {
            val voiceActorsState = State<UserMediaStatistics.VoiceActor>(
                { it.voiceActor?.id.toString() },
                { it.mediaIds.filterNotNull() },
            )

            val studiosState = State<UserMediaStatistics.Studio>(
                { it.studio?.id.toString() },
                { it.mediaIds.filterNotNull() },
            )
        }

        class Manga(viewModelScope: CoroutineScope, aniListApi: AuthedAniListApi) :
            States(viewModelScope, aniListApi)

        inner class State<Value>(
            val valueToKey: (Value) -> String,
            val valueToMediaIds: (Value) -> List<Int>,
        ) {
            val refreshRequest = MutableStateFlow("")
            val mediaFlows =
                mutableMapOf<String, Flow<Result<Map<Int, MediaTitlesAndImagesQuery.Data.Page.Medium>>>>()

            @Composable
            fun getMedia(value: Value) = getMedia(value, this)
        }
    }

    data class MediaEntry(
        val userMedia: UserFavoriteMediaNode,
        override val mediaListStatus: MediaListStatus? = userMedia.mediaListEntry?.status,
        override val progress: Int? = MediaUtils.maxProgress(
            type = userMedia.type,
            chapters = userMedia.chapters,
            episodes = userMedia.episodes,
            nextAiringEpisode = userMedia.nextAiringEpisode?.episode,
        ),
        override val progressVolumes: Int? = userMedia.volumes,
        override val scoreRaw: Double? = userMedia.mediaListEntry?.score,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
    ) : MediaGridCard.Entry {
        override val media = userMedia
        override val type = userMedia.type
        override val maxProgress = MediaUtils.maxProgress(
            type = userMedia.type,
            chapters = userMedia.chapters,
            episodes = userMedia.episodes,
            nextAiringEpisode = userMedia.nextAiringEpisode?.episode,
        )
        override val maxProgressVolumes = userMedia.volumes
        override val averageScore = userMedia.averageScore
        override val color = userMedia.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    }
}
