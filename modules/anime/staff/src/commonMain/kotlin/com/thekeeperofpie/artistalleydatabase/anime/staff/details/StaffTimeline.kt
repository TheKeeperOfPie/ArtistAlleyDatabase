package com.thekeeperofpie.artistalleydatabase.anime.staff.details

data class StaffTimeline<MediaEntry>(
    val yearsToMedia: List<Pair<Int?, List<MediaWithRole<MediaEntry>>>> = emptyList(),
    val loadMoreState: LoadMoreState = LoadMoreState.None,
) {
    data class MediaWithRole<MediaEntry>(
        val id: String,
        val role: String?,
        val mediaEntry: MediaEntry,
    )

    sealed interface LoadMoreState {
        data object None : LoadMoreState
        data object Loading : LoadMoreState
        data class Error(val throwable: Throwable) : LoadMoreState
    }
}
