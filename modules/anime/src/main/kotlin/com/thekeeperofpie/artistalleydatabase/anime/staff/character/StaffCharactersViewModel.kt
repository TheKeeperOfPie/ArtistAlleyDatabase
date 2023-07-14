package com.thekeeperofpie.artistalleydatabase.anime.staff.character

import com.anilist.fragment.CharacterWithRoleAndFavorites
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StaffCharactersViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
) : HeaderAndListViewModel<StaffCharactersScreen.Entry, CharacterWithRoleAndFavorites, CharacterListRow.Entry, CharacterSortOption>(
    sortOptionEnum = CharacterSortOption::class,
    sortOptionEnumDefault = CharacterSortOption.FAVORITES,
    loadingErrorTextRes = R.string.anime_staff_characters_error_loading,
) {
    override fun makeEntry(item: CharacterWithRoleAndFavorites) = CharacterListRow.Entry(item)

    override fun entryId(entry: CharacterListRow.Entry) = entry.character.id.toString()

    override suspend fun initialRequest(
        headerId: String,
        sortOption: CharacterSortOption,
        sortAscending: Boolean
    ) = StaffCharactersScreen.Entry(
        aniListApi.staffAndCharacters(staffId = headerId, sortOption.toApiValue(sortAscending))
    )

    override suspend fun pagedRequest(
        entry: StaffCharactersScreen.Entry,
        page: Int,
        sortOption: CharacterSortOption,
        sortAscending: Boolean
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
}
