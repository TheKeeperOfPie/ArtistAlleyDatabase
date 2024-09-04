package com.thekeeperofpie.artistalleydatabase.anime.media.characters

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.CharacterWithRole
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.character.DetailsCharacter
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaCharactersViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : HeaderAndListViewModel<MediaCharactersScreen.Entry, CharacterWithRole, DetailsCharacter, CharacterSortOption, MediaCharactersSortFilterController.FilterParams>(
    aniListApi = aniListApi,
    loadingErrorTextRes = R.string.anime_characters_error_loading,
) {
    private val destination = savedStateHandle.toDestination<AnimeDestination.MediaCharacters>(navigationTypeMap)
    val mediaId = destination.mediaId

    override val sortFilterController =
        MediaCharactersSortFilterController(viewModelScope, settings, featureOverrideProvider)

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

    override fun entryId(entry: DetailsCharacter) = entry.id

    override suspend fun initialRequest(
        filterParams: MediaCharactersSortFilterController.FilterParams?,
    ) = MediaCharactersScreen.Entry(
        aniListApi.mediaAndCharacters(mediaId = mediaId)
    )

    override suspend fun pagedRequest(
        page: Int,
        filterParams: MediaCharactersSortFilterController.FilterParams?,
    ) = aniListApi.mediaAndCharactersPage(
        mediaId = mediaId,
        page = page,
        sort = filterParams!!.sort.selectedOption(CharacterSortOption.RELEVANCE)
            .toApiValue(filterParams.sortAscending),
        role = filterParams.role,
    ).media.characters.run { pageInfo to edges }
}
