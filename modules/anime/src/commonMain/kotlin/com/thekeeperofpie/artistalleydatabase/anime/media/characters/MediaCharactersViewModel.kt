package com.thekeeperofpie.artistalleydatabase.anime.media.characters

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_characters_error_loading
import com.anilist.data.fragment.CharacterWithRole
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilteredViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.Flow

@AssistedInject
class MediaCharactersViewModel(
    private val aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaCharactersSortFilterViewModel: MediaCharactersSortFilterViewModel,
) : SortFilteredViewModel<MediaCharactersScreen.Entry, CharacterWithRole, CharacterDetails, MediaCharactersSortFilterViewModel.FilterParams>(
    loadingErrorTextRes = Res.string.anime_characters_error_loading,
) {
    private val destination =
        savedStateHandle.toDestination<AnimeDestination.MediaCharacters>(navigationTypeMap)
    val mediaId = destination.mediaId
    val viewer = aniListApi.authedUser

    override val filterParams = mediaCharactersSortFilterViewModel.state.filterParams

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result } },
            entryToId = { it.media.id.toString() },
            entryToType = { it.media.type.toFavoriteType() },
            entryToFavorite = { it.media.isFavourite },
        )
    }

    override fun makeEntry(item: CharacterWithRole) =
        CharacterUtils.toDetailsCharacter(item) { item.role }

    override fun entryId(entry: CharacterDetails) = entry.id

    override suspend fun initialRequest(
        filterParams: MediaCharactersSortFilterViewModel.FilterParams?,
    ) = MediaCharactersScreen.Entry(
        aniListApi.mediaAndCharacters(mediaId = mediaId)
    )

    override suspend fun request(
        filterParams: MediaCharactersSortFilterViewModel.FilterParams?,
    ): Flow<PagingData<CharacterWithRole>> =
        AniListPager { page ->
            aniListApi.mediaAndCharactersPage(
                mediaId = mediaId,
                page = page,
                sort = filterParams!!.sort.toApiValue(filterParams.sortAscending),
                role = filterParams.role,
            ).media.characters.run { pageInfo to edges }
        }


    @AssistedFactory
    interface Factory {
        fun create(
            savedStateHandle: SavedStateHandle,
            mediaCharactersSortFilterViewModel: MediaCharactersSortFilterViewModel,
        ): MediaCharactersViewModel
    }
}
