package com.thekeeperofpie.artistalleydatabase.anime.media.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_error_loading
import com.anilist.MediaDetailsQuery
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.foldPreviousResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeMediaDetailsViewModel(
    private val aniListApi: AuthedAniListApi,
    oAuthStore: AniListOAuthStore,
    val mediaListStatusController: MediaListStatusController,
    val ignoreController: IgnoreController,
    val settings: AnimeSettings,
    favoritesController: FavoritesController,
    private val historyController: HistoryController,
    private val markdown: Markdown,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    val viewer = aniListApi.authedUser
    val mediaId =
        savedStateHandle.toDestination<AnimeDestination.MediaDetails>(navigationTypeMap).mediaId

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    val hasAuth = oAuthStore.hasAuth

    val refresh = MutableStateFlow(-1L)
    private val refreshSecondary = MutableStateFlow(-1L)
    private val barrierMedia2 = MutableStateFlow(false)

    var entry by mutableStateOf<LoadingResult<AnimeMediaDetailsScreen.Entry>>(LoadingResult.loading())
    var entry2 by mutableStateOf<LoadingResult<AnimeMediaDetailsScreen.Entry2>>(LoadingResult.loading())
    var listStatus by mutableStateOf<MediaListStatusController.Update?>(null)

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result } },
            entryToId = { it.mediaId },
            entryToType = { it.media.type.toFavoriteType() },
            entryToFavorite = { it.media.isFavourite },
        )

        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.mapLatest { aniListApi.mediaDetails(mediaId, it > 0) }
                .mapLatest {
                    val result = it.result
                    if (result != null && result.media?.isAdult != false
                        && !settings.showAdult.value
                    ) {
                        LoadingResult.error(
                            Res.string.anime_media_details_error_loading,
                            IllegalStateException("Cannot load media")
                        )
                    } else {
                        it
                    }
                }
                .onEach {
                    it.result?.media?.let {
                        historyController.onVisitMediaDetails(
                            mediaId = it.id.toString(),
                            type = it.type,
                            isAdult = it.isAdult,
                            bannerImage = it.bannerImage,
                            coverImage = it.coverImage?.extraLarge,
                            titleRomaji = it.title?.romaji,
                            titleEnglish = it.title?.english,
                            titleNative = it.title?.native,
                        )
                    }
                }
                .runningFold(null as Pair<Pair<String?, MarkdownText?>?, LoadingResult<MediaDetailsQuery.Data>>?) { accumulator, loadingResult ->
                    val descriptionRaw = loadingResult.result?.media?.description
                    val previousDescriptionPair = accumulator?.first
                    val description =
                        if (previousDescriptionPair != null && descriptionRaw == previousDescriptionPair.first) {
                            previousDescriptionPair.second
                        } else {
                            descriptionRaw?.let(markdown::convertMarkdownText)
                        }
                    descriptionRaw to description to loadingResult
                }
                .filterNotNull()
                .flatMapLatest { (descriptionPair, loadingResult) ->
                    val media = loadingResult.result?.media
                    if (media == null) {
                        flowOf(loadingResult
                            .transformResult<AnimeMediaDetailsScreen.Entry> { null })
                    } else {
                        val relations = media.relations?.edges?.filterNotNull()
                            ?.mapNotNull {
                                val node = it.node ?: return@mapNotNull null
                                val relation = it.relationType ?: return@mapNotNull null
                                AnimeMediaDetailsScreen.Entry.Relation(
                                    it.id.toString(),
                                    relation,
                                    MediaPreviewEntry(node)
                                )
                            }
                            .orEmpty()
                            .sortedBy { AnimeMediaDetailsScreen.RELATION_SORT_ORDER.indexOf(it.relation) }

                        val mediaIds = setOf(media.id.toString()) +
                                relations.map { it.entry.media.id.toString() }

                        combine(
                            mediaListStatusController.allChanges(mediaIds),
                            ignoreController.updates(),
                            settings.mediaFilteringData(forceShowIgnored = true),
                        ) { mediaListUpdates, _, filteringData ->
                            loadingResult.transformResult {
                                AnimeMediaDetailsScreen.Entry(
                                    mediaId,
                                    media,
                                    relations = relations.mapNotNull {
                                        applyMediaFiltering(
                                            statuses = mediaListUpdates,
                                            ignoreController = ignoreController,
                                            filteringData = filteringData,
                                            entry = it,
                                            filterableData = it.entry.mediaFilterable,
                                            copy = {
                                                copy(entry = entry.copy(mediaFilterable = it))
                                            },
                                        )
                                    },
                                    description = descriptionPair?.second,
                                )
                            }
                        }
                    }
                }
                .foldPreviousResult()
                .catch {
                    emit(
                        LoadingResult.error(
                            Res.string.anime_media_details_error_loading,
                            it
                        )
                    )
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            combine(
                snapshotFlow { entry.result }.filterNotNull().flowOn(CustomDispatchers.Main),
                mediaListStatusController.allChanges(mediaId),
            ) { entry, update ->
                val mediaListEntry = entry.media.mediaListEntry
                MediaListStatusController.Update(
                    mediaId = entry.mediaId,
                    entry = if (update == null) mediaListEntry else update.entry,
                )
            }
                .catch {}
                .flowOn(CustomDispatchers.IO)
                .collectLatest { listStatus = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { entry.result }
                .filterNotNull()
                .take(1)
                .flatMapLatest { barrierMedia2.filter { it } }
                .flatMapLatest {
                    combine(refresh, refreshSecondary, ::Pair)
                        .mapLatest {
                            aniListApi.mediaDetails2(
                                id = mediaId,
                                skipCache = it.first > 0 || it.second > 0,
                            )
                        }
                        .flowOn(CustomDispatchers.IO)
                        .mapLatest { result ->
                            result.transformResult {
                                result.result?.media?.let {
                                    AnimeMediaDetailsScreen.Entry2(mediaId, it)
                                }
                            }
                        }
                }
                .foldPreviousResult(initialResult = LoadingResult.empty())
                .collectLatest { entry2 = it }
        }
    }

    fun refresh() {
        refresh.value = Clock.System.now().toEpochMilliseconds()
    }

    fun refreshSecondary() {
        refreshSecondary.value = Clock.System.now().toEpochMilliseconds()
    }

    fun requestLoadMedia2() = barrierMedia2.tryEmit(true)
}
