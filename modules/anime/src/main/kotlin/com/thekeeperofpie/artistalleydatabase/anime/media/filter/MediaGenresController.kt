package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class MediaGenresController(scope: ApplicationScope, aniListApi: AuthedAniListApi) {

    val refresh = MutableStateFlow(-1L)

    val genres = refresh.mapLatest {
        aniListApi.genres()
            .genreCollection
            ?.filterNotNull()
            .orEmpty()
    }
        .catch { emit(emptyList()) }
        .distinctUntilChanged()
        .flowOn(CustomDispatchers.IO)
        .shareIn(scope, SharingStarted.Lazily, replay = 1)

    fun refresh() {
        refresh.value = Clock.System.now().toEpochMilliseconds()
    }
}
