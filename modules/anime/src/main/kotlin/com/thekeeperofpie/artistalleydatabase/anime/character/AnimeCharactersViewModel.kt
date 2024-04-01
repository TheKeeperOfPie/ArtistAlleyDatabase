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
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeCharactersViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val charactersDeferred = MutableStateFlow(PagingData.empty<DetailsCharacter>())

    private val mediaId = savedStateHandle.get<String>("mediaId")!!
    private var initialized = false

    var charactersInitial by mutableStateOf<List<DetailsCharacter>>(emptyList())
        private set

    fun initialize(mediaDetailsViewModel: AnimeMediaDetailsViewModel) {
        if (initialized) return
        initialized = true
        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { mediaDetailsViewModel.entry.result?.media?.characters }
                .collectLatest {
                    charactersInitial = CharacterUtils.toDetailsCharacters(
                        it?.edges?.filterNotNull().orEmpty()
                    )
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { mediaDetailsViewModel.entry.result?.media?.characters }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { characters ->
                    AniListPager(perPage = 6, prefetchDistance = 1) { page ->
                        if (page == 1) {
                            characters.run { pageInfo to edges }
                        } else {
                            aniListApi.mediaDetailsCharactersPage(mediaId, page).characters
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
