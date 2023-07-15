package com.thekeeperofpie.artistalleydatabase.anime.character

import com.anilist.fragment.CharacterWithRole
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
) : HeaderAndListViewModel<CharactersScreen.Entry, CharacterWithRole, DetailsCharacter, CharacterSortOption>(
    aniListApi = aniListApi,
    sortOptionEnum = CharacterSortOption::class,
    sortOptionEnumDefault = CharacterSortOption.ROLE,
    loadingErrorTextRes = R.string.anime_characters_error_loading,
) {
    override fun makeEntry(item: CharacterWithRole) = CharacterUtils.toDetailsCharacter(item) { item.role }

    override fun entryId(entry: DetailsCharacter) = entry.id

    override suspend fun initialRequest(
        headerId: String,
        sortOption: CharacterSortOption,
        sortAscending: Boolean
    ) = CharactersScreen.Entry(
        // TODO: Sort
        aniListApi.mediaAndCharacters(mediaId = headerId)
    )

    override suspend fun pagedRequest(
        entry: CharactersScreen.Entry,
        page: Int,
        sortOption: CharacterSortOption,
        sortAscending: Boolean
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
