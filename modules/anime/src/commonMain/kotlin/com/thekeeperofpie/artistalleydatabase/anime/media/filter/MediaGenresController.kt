package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@SingletonScope
@Inject
class MediaGenresController(
    scope: ApplicationScope,
    aniListApi: AuthedAniListApi,
    settings: MediaDataSettings,
) {

    val refresh = RefreshFlow()

    val genres = refresh
        .updates
        .mapLatest {
            aniListApi.genres()
                .genreCollection
                ?.filterNotNull()
                .orEmpty()
        }
        .catch { emit(emptyList()) }
        .flatMapLatest { genres ->
            settings.showAdult.mapLatest { showAdult ->
                genres.filter { showAdult || it != "Hentai" }
            }
        }
        .distinctUntilChanged()
        .flowOn(CustomDispatchers.IO)
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    fun refresh() = refresh.refresh()
}
