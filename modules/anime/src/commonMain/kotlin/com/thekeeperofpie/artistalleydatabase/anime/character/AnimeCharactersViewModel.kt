package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager2
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeCharactersViewModel(
    private val aniListApi: AuthedAniListApi,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaDetailsViewModel: AnimeMediaDetailsViewModel,
) : ViewModel() {

    val charactersDeferred = MutableStateFlow(PagingData.empty<DetailsCharacter>())

    private val mediaId = savedStateHandle.get<String>("mediaId")!!

    var charactersInitial by mutableStateOf<List<DetailsCharacter>>(emptyList())
        private set

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { mediaDetailsViewModel.state.mediaEntry.result?.media?.characters }
                .collectLatest {
                    charactersInitial = CharacterUtils.toDetailsCharacters(
                        it?.edges?.filterNotNull().orEmpty()
                    )
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                mediaDetailsViewModel.refresh.updates,
                snapshotFlow { mediaDetailsViewModel.state.mediaEntry.result?.media?.characters }
                    .filterNotNull(),
                ::Pair,
            )
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { (refresh, characters) ->
                    val perPage = 6
                    AniListPager2(
                        perPage = perPage,
                        prefetchDistance = 1,
                        skipCache = refresh.fromUser,
                    ) { (page, skipCache) ->
                        if (page == 1) {
                            characters.run { pageInfo to edges }
                        } else {
                            aniListApi.mediaDetailsCharactersPage(mediaId, page, perPage, skipCache)
                                .characters
                                .run { pageInfo to edges }
                        }
                    }
                        .mapLatest { it.mapOnIO(CharacterUtils::toDetailsCharacter) }
                }
                .enforceUniqueIds { it.id }
                .cachedIn(viewModelScope)
                .collectLatest(charactersDeferred::emit)
        }
    }
}
