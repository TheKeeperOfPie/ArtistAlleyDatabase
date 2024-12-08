package com.thekeeperofpie.artistalleydatabase.anime.staff.details

import com.anilist.data.fragment.StaffDetailsCharacterMediaPage
import com.anilist.data.type.CharacterRole

data class StaffMediaTimeline(
    val yearsToCharacters: List<Pair<Int?, List<Character>>> = emptyList(),
    val loadMoreState: LoadMoreState = LoadMoreState.None,
) {
    data class Character(
        val id: String,
        val character: StaffDetailsCharacterMediaPage.Edge.Character,
        val role: CharacterRole?,
        val media: StaffDetailsCharacterMediaPage.Edge.Node?,
    )

    sealed interface LoadMoreState {
        data object None : LoadMoreState
        data object Loading : LoadMoreState
        data class Error(val throwable: Throwable) : LoadMoreState
    }
}
