package com.thekeeperofpie.artistalleydatabase.anime.staff.character

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.staff.generated.resources.Res
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_characters_error_loading
import com.anilist.data.StaffAndCharactersQuery
import com.anilist.data.fragment.CharacterWithRoleAndFavorites
import com.anilist.data.fragment.MediaWithListStatus
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDestinations
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSettings
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilteredViewModel
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
class StaffCharactersViewModel<CharacterEntry : Any, MediaEntry>(
    private val aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    private val settings: StaffSettings,
    favoritesController: FavoritesController,
    featureOverrideProvider: FeatureOverrideProvider,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val characterEntryProvider: CharacterEntryProvider<CharacterWithRoleAndFavorites, CharacterEntry, MediaEntry>,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
) : SortFilteredViewModel<StaffAndCharactersQuery.Data.Staff, CharacterWithRoleAndFavorites, CharacterEntry, StaffCharactersSortFilterController.FilterParams>(
    loadingErrorTextRes = Res.string.anime_staff_characters_error_loading,
) {
    private val destination =
        savedStateHandle.toDestination<StaffDestinations.StaffCharacters>(navigationTypeMap)
    val staffId = destination.staffId
    val viewer = aniListApi.authedUser
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    override val sortFilterController =
        StaffCharactersSortFilterController(viewModelScope, settings, featureOverrideProvider)

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result } },
            entryToId = { it.id.toString() },
            entryToType = { FavoriteType.STAFF },
            entryToFavorite = { it.isFavourite },
        )
    }

    override fun makeEntry(item: CharacterWithRoleAndFavorites) =
        characterEntryProvider.characterEntry(
            item,
            media = item.node.media?.nodes?.filterNotNull().orEmpty().distinctBy { it.id }
                .map(mediaEntryProvider::mediaEntry),
        )

    override fun entryId(entry: CharacterEntry) = characterEntryProvider.id(entry)

    override suspend fun initialRequest(
        filterParams: StaffCharactersSortFilterController.FilterParams?,
    ) = aniListApi.staffAndCharacters(staffId = staffId)

    override suspend fun request(
        filterParams: StaffCharactersSortFilterController.FilterParams?,
    ): Flow<PagingData<CharacterWithRoleAndFavorites>> =
        AniListPager { page ->
            aniListApi.staffAndCharactersPage(
                staffId = staffId,
                sort = filterParams!!.sort.selectedOption(CharacterSortOption.FAVORITES)
                    .toApiValue(filterParams.sortAscending),
                page = page,
            ).staff.characters.run { pageInfo to edges }
        }

    override fun Flow<PagingData<CharacterEntry>>.transformFlow() =
        flatMapLatest {
            combine(
                mediaListStatusController.allChanges(),
                ignoreController.updates(),
                settings.mediaFilteringData(),
            ) { statuses, _, filteringData ->
                it.mapNotNull {
                    characterEntryProvider.copyCharacterEntry(
                        it,
                        media = characterEntryProvider.media(it).mapNotNull {
                            applyMediaFiltering(
                                statuses = statuses,
                                ignoreController = ignoreController,
                                filteringData = filteringData,
                                entry = it,
                                filterableData = mediaEntryProvider.mediaFilterable(it),
                                copy = { mediaEntryProvider.copyMediaEntry(this, it) },
                            )
                        }
                    )
                }
            }
        }

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val mediaListStatusController: MediaListStatusController,
        private val ignoreController: IgnoreController,
        private val settings: StaffSettings,
        private val favoritesController: FavoritesController,
        private val featureOverrideProvider: FeatureOverrideProvider,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {
        fun <CharacterEntry : Any, MediaEntry> create(
            characterEntryProvider: CharacterEntryProvider<CharacterWithRoleAndFavorites, CharacterEntry, MediaEntry>,
            mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
        ) = StaffCharactersViewModel(
            aniListApi = aniListApi,
            mediaListStatusController = mediaListStatusController,
            ignoreController = ignoreController,
            settings = settings,
            favoritesController = favoritesController,
            featureOverrideProvider = featureOverrideProvider,
            navigationTypeMap = navigationTypeMap,
            savedStateHandle = savedStateHandle,
            characterEntryProvider = characterEntryProvider,
            mediaEntryProvider = mediaEntryProvider,
        )
    }
}
