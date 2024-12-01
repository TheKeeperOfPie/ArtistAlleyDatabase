package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import androidx.compose.runtime.snapshotFlow
import com.anilist.data.Anime2AnimeCountQuery
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class GameVariantCustom(
    api: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    userMediaListController: UserMediaListController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    scope: CoroutineScope,
    animeCountResponse: suspend () -> Anime2AnimeCountQuery.Data.SiteStatistics.Anime.Node?,
    onClearText: () -> Unit,
    private val aniListAutocompleter: AniListAutocompleter,
) : GameVariant<Unit>(
    api,
    mediaListStatusController,
    userMediaListController,
    ignoreController,
    settings,
    scope,
    animeCountResponse,
    onClearText
) {

    init {
        subscribeMediaState(state.startMedia, refreshStart)
        subscribeMediaState(state.targetMedia, refreshTarget)
    }

    override fun options() = Unit

    override suspend fun loadStartId(options: Unit) = loadMediaId(state.startMedia)
    override suspend fun loadTargetId(options: Unit) = loadMediaId(state.targetMedia)

    override fun resetStartMedia() {
        state.startMedia.customMediaId.value = null
    }

    override fun resetTargetMedia() {
        state.targetMedia.customMediaId.value = null
    }

    private suspend fun loadMediaId(mediaState: GameState.MediaState): LoadingResult<Int> {
        return mediaState.customMediaId.value?.toIntOrNull()
            ?.let(LoadingResult.Companion::success)
            ?: return LoadingResult.empty()
    }

    private fun subscribeMediaState(mediaState: GameState.MediaState, refresh: MutableStateFlow<Long>) {
        scope.launch(CustomDispatchers.Main) {
            mediaState.customMediaId
                .collectLatest { refresh.emit(Clock.System.now().toEpochMilliseconds()) }
        }
        scope.launch(CustomDispatchers.Main) {
            snapshotFlow { mediaState.customText }
                .debounce(500.milliseconds)
                .filter(String::isNotBlank)
                .flatMapLatest {
                    aniListAutocompleter.querySeriesNetwork(it, type = MediaType.ANIME)
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { mediaState.customPredictions = it }
        }
    }
}
