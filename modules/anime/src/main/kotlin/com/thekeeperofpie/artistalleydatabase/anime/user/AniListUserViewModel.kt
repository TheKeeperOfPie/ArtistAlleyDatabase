package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.UserMediaStatistics
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.user.stats.UserStatsDetailsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AniListUserViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
) : ViewModel() {

    private var initialized = false
    private var userId: String? = null

    val entry = MutableStateFlow<AniListUserScreen.Entry?>(null)
    val viewer = aniListApi.authedUser
    var errorResource = MutableStateFlow<Pair<Int, Exception?>?>(null)

    val animeGenresState = UserStatsDetailsState<UserMediaStatistics.Genre>(
        viewModelScope,
        aniListApi,
        { it.genre.orEmpty() },
        { it.mediaIds.filterNotNull() },
        isAnime = true,
    )

    val animeTagsState = UserStatsDetailsState<UserMediaStatistics.Tag>(
        viewModelScope,
        aniListApi,
        { it.tag?.id.toString() },
        { it.mediaIds.filterNotNull() },
        isAnime = true,
    )

    val animeVoiceActorsState = UserStatsDetailsState<UserMediaStatistics.VoiceActor>(
        viewModelScope,
        aniListApi,
        { it.voiceActor?.id.toString() },
        { it.mediaIds.filterNotNull() },
        isAnime = true,
    )

    val animeStudiosState = UserStatsDetailsState<UserMediaStatistics.Studio>(
        viewModelScope,
        aniListApi,
        { it.studio?.id.toString() },
        { it.mediaIds.filterNotNull() },
        isAnime = true,
    )

    val animeStaffState = UserStatsDetailsState<UserMediaStatistics.Staff>(
        viewModelScope,
        aniListApi,
        { it.staff?.id.toString() },
        { it.mediaIds.filterNotNull() },
        isAnime = true,
    )

    val mangaGenresState = UserStatsDetailsState<UserMediaStatistics.Genre>(
        viewModelScope,
        aniListApi,
        { it.genre.orEmpty() },
        { it.mediaIds.filterNotNull() },
        isAnime = false,
    )

    val mangaTagsState = UserStatsDetailsState<UserMediaStatistics.Tag>(
        viewModelScope,
        aniListApi,
        { it.tag?.id.toString() },
        { it.mediaIds.filterNotNull() },
        isAnime = false,
    )

    val mangaVoiceActorsState = UserStatsDetailsState<UserMediaStatistics.VoiceActor>(
        viewModelScope,
        aniListApi,
        { it.voiceActor?.id.toString() },
        { it.mediaIds.filterNotNull() },
        isAnime = false,
    )

    val mangaStudiosState = UserStatsDetailsState<UserMediaStatistics.Studio>(
        viewModelScope,
        aniListApi,
        { it.studio?.id.toString() },
        { it.mediaIds.filterNotNull() },
        isAnime = false,
    )

    val mangaStaffState = UserStatsDetailsState<UserMediaStatistics.Staff>(
        viewModelScope,
        aniListApi,
        { it.staff?.id.toString() },
        { it.mediaIds.filterNotNull() },
        isAnime = false,
    )

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(userId: String?) {
        if (initialized) return
        initialized = true
        this.userId = userId

        viewModelScope.launch(CustomDispatchers.IO) {
            refreshUptimeMillis.collectLatest {
                try {
                    entry.value =
                        aniListApi.user((userId ?: aniListApi.authedUser.value?.id?.toString())!!)
                            ?.let(AniListUserScreen::Entry)
                } catch (e: Exception) {
                    errorResource.value = R.string.anime_user_error_loading to e
                }
            }
        }
    }
}
