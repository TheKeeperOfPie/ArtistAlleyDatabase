package com.thekeeperofpie.artistalleydatabase.anime.staff.character

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.anilist.fragment.CharacterWithRoleAndFavorites
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.compose.navigation.toDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StaffCharactersViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    favoritesController: FavoritesController,
    featureOverrideProvider: FeatureOverrideProvider,
    savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : HeaderAndListViewModel<StaffCharactersScreen.Entry, CharacterWithRoleAndFavorites, CharacterListRow.Entry, CharacterSortOption, StaffCharactersSortFilterController.FilterParams>(
    aniListApi = aniListApi,
    loadingErrorTextRes = R.string.anime_staff_characters_error_loading,
) {
    private val destination = savedStateHandle.toDestination<AnimeDestinations.StaffCharacters>(navigationTypeMap)
    val staffId = destination.staffId
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    override val sortFilterController =
        StaffCharactersSortFilterController(viewModelScope, settings, featureOverrideProvider)

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result } },
            entryToId = { it.staff.id.toString() },
            entryToType = { FavoriteType.STAFF },
            entryToFavorite = { it.staff.isFavourite },
        )
    }

    override fun makeEntry(item: CharacterWithRoleAndFavorites) = CharacterListRow.Entry(
        item,
        media = item.node.media?.nodes?.filterNotNull().orEmpty().distinctBy { it.id }
            .map(::MediaWithListStatusEntry),
    )

    override fun entryId(entry: CharacterListRow.Entry) = entry.character.id.toString()

    override suspend fun initialRequest(
        filterParams: StaffCharactersSortFilterController.FilterParams?,
    ) = StaffCharactersScreen.Entry(aniListApi.staffAndCharacters(staffId = staffId))

    override suspend fun pagedRequest(
        page: Int,
        filterParams: StaffCharactersSortFilterController.FilterParams?,
    ) = aniListApi.staffAndCharactersPage(
        staffId = staffId,
        sort = filterParams!!.sort.selectedOption(CharacterSortOption.FAVORITES)
            .toApiValue(filterParams.sortAscending),
        page = page,
    ).staff.characters.run { pageInfo to edges }

    override fun Flow<PagingData<CharacterListRow.Entry>>.transformFlow() =
        flatMapLatest {
            combine(
                mediaListStatusController.allChanges(),
                ignoreController.updates(),
                settings.showIgnored,
                settings.showAdult,
                settings.showLessImportantTags,
                settings.showSpoilerTags,
            ) { statuses, _, showIgnored, showAdult, showLessImportantTags, showSpoilerTags ->
                it.mapNotNull {
                    it.copy(media = it.media.mapNotNull {
                        applyMediaFiltering(
                            statuses = statuses,
                            ignoreController = ignoreController,
                            showAdult = showAdult,
                            showIgnored = showIgnored,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                            entry = it,
                            transform = { it },
                            media = it.media,
                            copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                copy(
                                    mediaListStatus = mediaListStatus,
                                    progress = progress,
                                    progressVolumes = progressVolumes,
                                    scoreRaw = scoreRaw,
                                    ignored = ignored,
                                    showLessImportantTags = showLessImportantTags,
                                    showSpoilerTags = showSpoilerTags,
                                )
                            },
                        )
                    })
                }
            }
        }
}
