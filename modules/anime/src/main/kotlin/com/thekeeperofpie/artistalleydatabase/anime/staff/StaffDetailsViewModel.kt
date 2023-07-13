package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.StaffDetailsCharacterMediaPage
import com.anilist.fragment.StaffDetailsStaffMediaPage
import com.anilist.type.CharacterRole
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StaffDetailsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val animeSettings: AnimeSettings,
) : ViewModel() {

    lateinit var staffId: String

    var entry by mutableStateOf<StaffDetailsScreen.Entry?>(null)
    var loading by mutableStateOf(true)
    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()
    val showAdult get() = animeSettings.showAdult

    val mediaTimeline = MutableStateFlow(MediaTimeline())
    private val mediaTimelineLastRequestedYear = MutableStateFlow<Int?>(null)
    private var mediaTimelineResults = MutableStateFlow(emptyList<StaffDetailsCharacterMediaPage>())

    var staffTimeline = MutableStateFlow(StaffTimeline())
    private val staffTimelineLastRequestedYear = MutableStateFlow<Int?>(null)
    private var staffTimelineResults = MutableStateFlow(emptyList<StaffDetailsStaffMediaPage>())

    fun initialize(staffId: String) {
        if (::staffId.isInitialized) return
        this.staffId = staffId

        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                val staff = aniListApi.staffDetails(staffId)
                showAdult.collectLatest {
                    val entry = StaffDetailsScreen.Entry(staff, it)
                    withContext(CustomDispatchers.Main) {
                        this@StaffDetailsViewModel.entry = entry
                    }
                }
            } catch (exception: Exception) {
                withContext(CustomDispatchers.Main) {
                    errorResource = R.string.anime_staff_error_loading to exception
                }
            } finally {
                withContext(CustomDispatchers.Main) {
                    loading = false
                }
            }
        }

        // TODO: More robust pagination
        // TODO: Handle ignored
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                mediaTimeline,
                mediaTimelineLastRequestedYear,
                mediaTimelineResults,
                animeSettings.showAdult,
                ::MediaTimelineRefreshParams
            )
                .filter { (timeline) -> timeline.loadMoreState == MediaTimeline.LoadMoreState.None }
                .filter { (_, _, existingResults) ->
                    existingResults.isEmpty() || existingResults.last().pageInfo?.hasNextPage == true
                }
                .filter { (timeline, requestedYear) ->
                    val yearsToCharacters = timeline.yearsToCharacters
                    yearsToCharacters.indexOfFirst { it.first == requestedYear } ==
                            yearsToCharacters.lastIndex
                }
                .onEach { (timeline) ->
                    withContext(CustomDispatchers.Main) {
                        mediaTimeline.value = timeline.copy(
                            loadMoreState = MediaTimeline.LoadMoreState.Loading
                        )
                    }
                }
                .map { (timeline, _, existingResults, showAdult) ->
                    val nextPage = existingResults.size + 1
                    try {
                        val result =
                            aniListApi.staffDetailsCharacterMediaPagination(staffId, nextPage)
                        val newResults = existingResults + result
                        timeline.copy(
                            yearsToCharacters = calculateMediaTimeline(showAdult, newResults),
                            loadMoreState = MediaTimeline.LoadMoreState.None
                        ) to newResults
                    } catch (throwable: Throwable) {
                        timeline.copy(
                            loadMoreState = MediaTimeline.LoadMoreState.Error(
                                throwable
                            )
                        ) to existingResults
                    }
                }
                .collectLatest { (timeline, newResults) ->
                    mediaTimelineResults.value = newResults
                    withContext(CustomDispatchers.Main) {
                        mediaTimeline.value = timeline
                    }
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                staffTimeline,
                staffTimelineLastRequestedYear,
                staffTimelineResults,
                animeSettings.showAdult,
                ::StaffTimelineRefreshParams
            )
                .filter { (timeline) -> timeline.loadMoreState == StaffTimeline.LoadMoreState.None }
                .filter { (_, _, existingResults) ->
                    existingResults.isEmpty() || existingResults.last().pageInfo?.hasNextPage == true
                }
                .filter { (timeline, requestedYear) ->
                    val yearsToMedia = timeline.yearsToMedia
                    yearsToMedia.indexOfFirst { it.first == requestedYear } ==
                            yearsToMedia.lastIndex
                }
                .onEach { (timeline) ->
                    withContext(CustomDispatchers.Main) {
                        staffTimeline.value = timeline.copy(
                            loadMoreState = StaffTimeline.LoadMoreState.Loading
                        )
                    }
                }
                .map { (timeline, _, existingResults, showAdult) ->
                    val nextPage = existingResults.size + 1
                    try {
                        val result =
                            aniListApi.staffDetailsStaffMediaPagination(staffId, nextPage)
                        val newResults = existingResults + result
                        timeline.copy(
                            yearsToMedia = calculateStaffTimeline(showAdult, newResults),
                            loadMoreState = StaffTimeline.LoadMoreState.None
                        ) to newResults
                    } catch (throwable: Throwable) {
                        timeline.copy(
                            loadMoreState = StaffTimeline.LoadMoreState.Error(
                                throwable
                            )
                        ) to existingResults
                    }
                }
                .collectLatest { (timeline, newResults) ->
                    staffTimelineResults.value = newResults
                    withContext(CustomDispatchers.Main) {
                        staffTimeline.value = timeline
                    }
                }
        }
    }

    fun onRequestMediaYear(year: Int?) {
        val existingValue = mediaTimelineLastRequestedYear.value
        if (existingValue == null || (year != null && year < existingValue)) {
            mediaTimelineLastRequestedYear.value = year
        }
    }

    fun onRequestStaffYear(year: Int?) {
        val existingValue = staffTimelineLastRequestedYear.value
        if (existingValue == null || (year != null && year < existingValue)) {
            staffTimelineLastRequestedYear.value = year
        }
    }

    private fun calculateMediaTimeline(
        showAdult: Boolean,
        results: List<StaffDetailsCharacterMediaPage>,
    ) = results.flatMap { it.edges?.filterNotNull().orEmpty() }
        .groupBy { it.node?.startDate?.year }
        .mapValues {
            it.value.sortedWith(
                // TODO: Offer option to sort by favorites instead
                compareBy<StaffDetailsCharacterMediaPage.Edge, Int?>(nullsLast()) {
                    it.node?.startDate?.year
                }
                    .thenComparing(compareBy(nullsLast()) { it.node?.startDate?.month })
                    .thenComparing(compareBy(nullsLast()) { it.node?.startDate?.day })
                    .reversed()
            )
                .filter { if (showAdult) true else it.node?.isAdult == false }
                .flatMap { edge ->
                    edge.characters?.filterNotNull().orEmpty()
                        .map {
                            MediaTimeline.Character(
                                id = "${edge.node?.id}_${it.id}",
                                character = it,
                                role = edge.characterRole,
                                media = edge.node,
                            )
                        }
                }
                .distinctBy { it.id }
        }
        .entries
        .sortedWith(compareByDescending(nullsLast()) { it.key })
        .map { it.toPair() }

    private fun calculateStaffTimeline(
        showAdult: Boolean,
        results: List<StaffDetailsStaffMediaPage>,
    ) = results.flatMap { it.edges?.filterNotNull().orEmpty() }
        .groupBy { it.node?.startDate?.year }
        .mapValues {
            it.value.sortedWith(
                compareBy<StaffDetailsStaffMediaPage.Edge, Int?>(nullsLast()) {
                    it.node?.startDate?.year
                }
                    .thenComparing(compareBy(nullsLast()) { it.node?.startDate?.month })
                    .thenComparing(compareBy(nullsLast()) { it.node?.startDate?.day })
                    .reversed()
            )
                .filter { if (showAdult) true else it.node?.isAdult == false }
                .mapNotNull { edge ->
                    val node = edge.node ?: return@mapNotNull null
                    StaffTimeline.Media(
                        id = "${edge.id}_${node.id}",
                        media = node,
                        role = edge.staffRole,
                        character = edge.characters?.firstOrNull(),
                    )
                }
                .distinctBy { it.id }
        }
        .entries
        .sortedWith(compareByDescending(nullsLast()) { it.key })
        .map { it.toPair() }

    data class MediaTimelineRefreshParams(
        val timeline: MediaTimeline,
        val requestedYear: Int?,
        val existingResults: List<StaffDetailsCharacterMediaPage>,
        val showAdult: Boolean,
    )

    data class MediaTimeline(
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
            object None : LoadMoreState
            object Loading : LoadMoreState
            data class Error(val throwable: Throwable) : LoadMoreState
        }
    }

    data class StaffTimelineRefreshParams(
        val timeline: StaffTimeline,
        val requestedYear: Int?,
        val existingResults: List<StaffDetailsStaffMediaPage>,
        val showAdult: Boolean,
    )

    data class StaffTimeline(
        val yearsToMedia: List<Pair<Int?, List<Media>>> = emptyList(),
        val loadMoreState: LoadMoreState = LoadMoreState.None,
    ) {
        data class Media(
            val id: String,
            val media: StaffDetailsStaffMediaPage.Edge.Node,
            val role: String?,
            val character: StaffDetailsStaffMediaPage.Edge.Character?,
        )

        sealed interface LoadMoreState {
            object None : LoadMoreState
            object Loading : LoadMoreState
            data class Error(val throwable: Throwable) : LoadMoreState
        }
    }
}
