package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.CharacterWithRole
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
) : HeaderAndListViewModel<CharactersScreen.Entry, CharacterWithRole, DetailsCharacter, CharacterSortOption>(
    aniListApi = aniListApi,
    sortOptionEnum = CharacterSortOption::class,
    sortOptionEnumDefault = CharacterSortOption.ROLE,
    loadingErrorTextRes = R.string.anime_characters_error_loading,
) {
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    override fun initialize(headerId: String) {
        super.initialize(headerId)
        favoritesToggleHelper.initializeTracking(
            viewModel = this,
            entry = { snapshotFlow { entry } },
            entryToId = { it.media.id.toString() },
            entryToType = { it.media.type.toFavoriteType() },
            entryToFavorite = { it.media.isFavourite },
        )
    }

    override fun makeEntry(item: CharacterWithRole) =
        CharacterUtils.toDetailsCharacter(item) { item.role }

    override fun entryId(entry: DetailsCharacter) = entry.id

    override suspend fun initialRequest(
        headerId: String,
        sortOption: CharacterSortOption,
        sortAscending: Boolean,
    ) = CharactersScreen.Entry(
        // TODO: Sort
        aniListApi.mediaAndCharacters(mediaId = headerId)
    )

    override suspend fun pagedRequest(
        entry: CharactersScreen.Entry,
        page: Int,
        sortOption: CharacterSortOption,
        sortAscending: Boolean,
    ) = if (page == 1) {
        val result = entry.media.characters
        result?.pageInfo to result?.edges?.filterNotNull().orEmpty()
    } else {
        val result = aniListApi.mediaAndCharactersPage(
            mediaId = entry.media.id.toString(),
            page = page,
        ).media.characters
        result?.pageInfo to result?.edges?.filterNotNull().orEmpty()
    }
}
