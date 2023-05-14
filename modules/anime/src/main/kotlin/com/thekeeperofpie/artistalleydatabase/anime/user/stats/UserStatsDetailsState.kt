package com.thekeeperofpie.artistalleydatabase.anime.user.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.anilist.MediaTitlesAndImagesQuery.Data.Page.Medium
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
import kotlinx.coroutines.flow.shareIn

class UserStatsDetailsState<Value>(
    private val scope: CoroutineScope,
    private val aniListApi: AuthedAniListApi,
    val valueToKey: (Value) -> String,
    val valueToMediaIds: (Value) -> List<Int>,
    val isAnime: Boolean,
) {

    private val refreshRequest = MutableStateFlow("")
    private val mediaFlows = mutableMapOf<String, Flow<Result<Map<Int, Medium>>>>()

    @Composable
    fun getMedia(value: Value): Result<Map<Int, Medium>?> {
        val key = valueToKey(value)
        return mediaFlows.getOrPut(key) {
            refreshRequest.filter { it == key }
                .startWith(key)
                .flowOn(CustomDispatchers.IO)
                .map {
                    Result.success(
                        aniListApi.mediaTitlesAndImages(valueToMediaIds(value))
                            .associateBy { it.id }
                    )
                }
                .catch { emit(Result.failure(it)) }
                .shareIn(scope, started = SharingStarted.Lazily, replay = 1)
        }
            .collectAsState(initial = Result.success(null))
            .value
    }
}
