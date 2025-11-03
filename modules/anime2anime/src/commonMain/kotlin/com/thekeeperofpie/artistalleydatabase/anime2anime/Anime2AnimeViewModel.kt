package com.thekeeperofpie.artistalleydatabase.anime2anime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.data.Anime2AnimeCountQuery
import com.anilist.data.fragment.AniListMedia
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime2anime.game.GameVariantCustom
import com.thekeeperofpie.artistalleydatabase.anime2anime.game.GameVariantDaily
import com.thekeeperofpie.artistalleydatabase.anime2anime.game.GameVariantRandom
import com.thekeeperofpie.artistalleydatabase.anime2anime.game.GameVariantUserList
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Inject
class Anime2AnimeViewModel(
    private val api: AuthedAniListApi,
    private val aniListAutocompleter: AniListAutocompleter,
    private val mediaListStatusController: MediaListStatusController,
    private val userMediaListController: UserMediaListController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
) : ViewModel() {

    companion object {
        private const val TAG = "Anime2AnimeViewModel"
    }

    val viewer = api.authedUser
    var text by mutableStateOf("")
    var predictions by mutableStateOf(emptyList<EntrySection.MultiText.Entry.Prefilled<AniListMedia>>())

    // TODO: Handle revival
    var selectedTab by mutableStateOf(Anime2AnimeScreen.GameTab.DAILY)

    private var animeCountResponse: Anime2AnimeCountQuery.Data.SiteStatistics.Anime.Node? = null

    private val gameDaily by lazy {
        GameVariantDaily(
            api,
            mediaListStatusController,
            userMediaListController,
            ignoreController,
            settings,
            viewModelScope,
            animeCountResponse = ::fetchAnimeCount,
            onClearText = { text = "" },
        )
    }

    private val gameRandom by lazy {
        GameVariantRandom(
            api,
            mediaListStatusController,
            userMediaListController,
            ignoreController,
            settings,
            viewModelScope,
            animeCountResponse = ::fetchAnimeCount,
            onClearText = { text = "" },
        )
    }

    private val gameCustom by lazy {
        GameVariantCustom(
            api,
            mediaListStatusController,
            userMediaListController,
            ignoreController,
            settings,
            viewModelScope,
            animeCountResponse = ::fetchAnimeCount,
            onClearText = { text = "" },
            aniListAutocompleter = aniListAutocompleter,
        )
    }

    private val gameUserList by lazy {
        GameVariantUserList(
            api,
            mediaListStatusController,
            userMediaListController,
            ignoreController,
            settings,
            viewModelScope,
            animeCountResponse = ::fetchAnimeCount,
            onClearText = { text = "" },
        )
    }

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { text }
                .debounce(500.milliseconds)
                .filter(String::isNotBlank)
                .flatMapLatest {
                    aniListAutocompleter.querySeriesNetwork(it, type = MediaType.ANIME)
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { predictions = it }
        }
    }

    fun onRefresh() {
        currentGame().run {
            refreshStart()
            refreshTarget()
        }
    }

    fun onSwitchStartTargetClick() {
        currentGame().switchStartTarget()
    }

    fun onSubmit() {
        val entry = predictions.firstOrNull()
        if (entry == null) {
            currentGame().state.lastSubmitResult = Anime2AnimeSubmitResult.MediaNotFound(text)
            return
        }

        currentGame().submitMedia(entry.value)
    }

    fun onChooseMedia(aniListMedia: AniListMedia) {
        currentGame().submitMedia(aniListMedia)
    }

    fun onRestart() {
        currentGame().restart()
    }

    private suspend fun fetchAnimeCount(): Anime2AnimeCountQuery.Data.SiteStatistics.Anime.Node? {
        if (animeCountResponse == null) {
            animeCountResponse = api.anime2AnimeCount().getOrNull()
        }
        return animeCountResponse
    }

    fun currentGame() = when (selectedTab) {
        Anime2AnimeScreen.GameTab.DAILY -> gameDaily
        Anime2AnimeScreen.GameTab.RANDOM -> gameRandom
        Anime2AnimeScreen.GameTab.CUSTOM -> gameCustom
        Anime2AnimeScreen.GameTab.USER_LIST -> gameUserList
    }
}
