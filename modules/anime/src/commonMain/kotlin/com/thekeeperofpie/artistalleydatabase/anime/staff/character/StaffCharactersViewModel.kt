package com.thekeeperofpie.artistalleydatabase.anime.staff.character

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_staff_characters_error_loading
import com.anilist.data.fragment.CharacterWithRoleAndFavorites
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class StaffCharactersViewModel(
    aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    favoritesController: FavoritesController,
    featureOverrideProvider: FeatureOverrideProvider,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : HeaderAndListViewModel<StaffCharactersScreen.Entry, CharacterWithRoleAndFavorites, CharacterListRow.Entry, CharacterSortOption, StaffCharactersSortFilterController.FilterParams>(
    aniListApi = aniListApi,
    loadingErrorTextRes = Res.string.anime_staff_characters_error_loading,
) {
    private val destination =
        savedStateHandle.toDestination<AnimeDestination.StaffCharacters>(navigationTypeMap)
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
                settings.mediaFilteringData(),
            ) { statuses, _, filteringData ->
                it.mapNotNull {
                    it.copy(media = it.media.mapNotNull {
                        applyMediaFiltering(
                            statuses = statuses,
                            ignoreController = ignoreController,
                            filteringData = filteringData,
                            entry = it,
                            filterableData = it.mediaFilterable,
                            copy = { copy(mediaFilterable = it) },
                        )
                    })
                }
            }
        }
}
