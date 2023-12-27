package com.thekeeperofpie.artistalleydatabase.anime2anime

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.Anime2AnimeCountQuery
import com.anilist.fragment.AniListMedia
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime2anime.game.GameVariantDaily
import com.thekeeperofpie.artistalleydatabase.anime2anime.game.GameVariantRandom
import com.thekeeperofpie.artistalleydatabase.anime2anime.game.GameVariantUserList
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class Anime2AnimeViewModel @Inject constructor(
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

    private val refresh = MutableStateFlow(-1L)
    private var animeCountResponse: Anime2AnimeCountQuery.Data.SiteStatistics.Anime.Node? = null

    private val gameDaily by lazy {
        GameVariantDaily(
            api,
            mediaListStatusController,
            userMediaListController,
            ignoreController,
            settings,
            viewModelScope,
            refresh,
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
            refresh,
            animeCountResponse = ::fetchAnimeCount,
            onClearText = { text = "" },
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
            refresh,
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
        refresh.value = SystemClock.uptimeMillis()
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
        Anime2AnimeScreen.GameTab.USER_LIST -> gameUserList
    }
}
