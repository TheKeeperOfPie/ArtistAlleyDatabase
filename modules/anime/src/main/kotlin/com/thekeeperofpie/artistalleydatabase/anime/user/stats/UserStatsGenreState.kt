package com.thekeeperofpie.artistalleydatabase.anime.user.stats

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.anilist.MediaTitlesAndImagesQuery.Data.Page.Medium
import com.anilist.fragment.UserMediaStatistics
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

class UserStatsGenreState(
    private val scope: CoroutineScope,
    private val aniListApi: AuthedAniListApi,
    val isAnime: Boolean,
) {

    private val refreshRequest = MutableStateFlow("")
    private val mediaFlows = mutableMapOf<String, Flow<Result<Map<Int, Medium>>>>()

    @Composable
    fun getMedia(genre: UserMediaStatistics.Genre): Result<Map<Int, Medium>?> {
        val key = genre.genre.orEmpty()
        return mediaFlows.getOrPut(key) {
            refreshRequest.filter { it == key }
                .startWith(key)
                .flowOn(CustomDispatchers.IO)
                .map {
                    Result.success(
                        aniListApi.mediaTitlesAndImages(genre.mediaIds?.filterNotNull().orEmpty())
                            .associateBy { it.id }
                    )
                }
                .catch { emit(Result.failure(it)) }
                .onEach {
                    if (it.isFailure) {
                        Log.d("GenreDebug", "Failure loading $key", it.exceptionOrNull())
                    }
                }
                .shareIn(scope, started = SharingStarted.Lazily, replay = 1)
        }
            .collectAsState(initial = Result.success(null))
            .value
    }
}
