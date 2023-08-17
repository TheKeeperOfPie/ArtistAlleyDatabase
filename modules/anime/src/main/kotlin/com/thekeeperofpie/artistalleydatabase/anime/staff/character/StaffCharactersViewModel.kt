package com.thekeeperofpie.artistalleydatabase.anime.staff.character

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.anilist.fragment.CharacterWithRoleAndFavorites
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StaffCharactersViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    private val ignoreList: AnimeMediaIgnoreList,
    private val settings: AnimeSettings,
    favoritesController: FavoritesController,
) : HeaderAndListViewModel<StaffCharactersScreen.Entry, CharacterWithRoleAndFavorites, CharacterListRow.Entry, CharacterSortOption>(
    aniListApi = aniListApi,
    sortOptionEnum = CharacterSortOption::class,
    sortOptionEnumDefault = CharacterSortOption.FAVORITES,
    loadingErrorTextRes = R.string.anime_staff_characters_error_loading,
) {
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    override fun initialize(headerId: String) {
        super.initialize(headerId)
        favoritesToggleHelper.initializeTracking(
            viewModel = this,
            entry = { snapshotFlow { entry } },
            entryToId = { it.staff.id.toString() },
            entryToType = { FavoriteType.STAFF },
            entryToFavorite = { it.staff.isFavourite },
        )
    }

    override fun makeEntry(item: CharacterWithRoleAndFavorites) = CharacterListRow.Entry(item)

    override fun entryId(entry: CharacterListRow.Entry) = entry.character.id.toString()

    override suspend fun initialRequest(
        headerId: String,
        sortOption: CharacterSortOption,
        sortAscending: Boolean,
    ) = StaffCharactersScreen.Entry(
        aniListApi.staffAndCharacters(staffId = headerId, sortOption.toApiValue(sortAscending))
    )

    override suspend fun pagedRequest(
        entry: StaffCharactersScreen.Entry,
        page: Int,
        sortOption: CharacterSortOption,
        sortAscending: Boolean,
    ) = if (page == 1) {
        val result = entry.staff.characters
        result?.pageInfo to result?.edges?.filterNotNull().orEmpty()
    } else {
        val result = aniListApi.staffAndCharactersPage(
            staffId = entry.staff.id.toString(),
            sort = sortOption.toApiValue(sortAscending),
            page = page,
        ).staff.characters
        result?.pageInfo to result?.edges?.filterNotNull().orEmpty()
    }

    override fun Flow<PagingData<CharacterListRow.Entry>>.transformFlow() =
        flatMapLatest { pagingData ->
            combine(
                ignoreList.updates,
                settings.showIgnored,
                settings.showAdult,
            ) { ignoredIds, showIgnored, showAdult ->
                pagingData.mapOnIO {
                    it.copy(
                        media = it.media
                            .filter { showAdult || it.isAdult == false }
                            .filter { showIgnored || !ignoredIds.contains(it.media.id) }
                    )
                }
            }
        }
}
