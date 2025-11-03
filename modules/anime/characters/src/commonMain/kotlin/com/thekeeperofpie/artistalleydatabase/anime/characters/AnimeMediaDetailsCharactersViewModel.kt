package com.thekeeperofpie.artistalleydatabase.anime.characters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.MediaDetailsQuery
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager2
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class AnimeMediaDetailsCharactersViewModel(
    private val aniListApi: AuthedAniListApi,
    @Assisted mediaId: String,
    @Assisted characters: Flow<MediaDetailsQuery.Data.Media.Characters?>,
) : ViewModel() {

    val charactersDeferred = MutableStateFlow(PagingData.empty<CharacterDetails>())

    var charactersInitial by mutableStateOf<List<CharacterDetails>>(emptyList())
        private set

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            characters
                .collectLatest {
                    charactersInitial = CharacterUtils.toDetailsCharacters(
                        it?.edges?.filterNotNull().orEmpty()
                    )
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            characters
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { characters ->
                    if (characters == null) {
                        return@flatMapLatest flowOf(PagingData.empty<CharacterDetails>())
                    }
                    val perPage = 6
                    AniListPager2(
                        perPage = perPage,
                        prefetchDistance = 1,
                        // TODO: Re-add skipCache
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

    @AssistedFactory
    interface Factory {
        fun create(
            mediaId: String,
            characters: Flow<MediaDetailsQuery.Data.Media.Characters?>,
        ): AnimeMediaDetailsCharactersViewModel
    }
}
