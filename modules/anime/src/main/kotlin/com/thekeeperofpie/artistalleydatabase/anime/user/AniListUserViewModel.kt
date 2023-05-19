package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.MediaTitlesAndImagesQuery
import com.anilist.fragment.UserMediaStatistics
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AniListUserViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
) : ViewModel() {

    private var initialized = false
    private var userId: String? = null

    var entry by mutableStateOf<AniListUserScreen.Entry?>(null)
    val viewer = aniListApi.authedUser
    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    val animeStates = States.Anime(viewModelScope, aniListApi)
    val mangaStates = States.Manga(viewModelScope, aniListApi)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(userId: String?) {
        if (initialized) return
        initialized = true
        this.userId = userId

        viewModelScope.launch(CustomDispatchers.IO) {
            refreshUptimeMillis.collectLatest {
                try {
                    entry =
                        aniListApi.user((userId ?: aniListApi.authedUser.value?.id?.toString())!!)
                            ?.let(AniListUserScreen::Entry)
                } catch (e: Exception) {
                    errorResource = R.string.anime_media_list_error_loading to e
                }
            }
        }
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
            state: State<Value>
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
}
