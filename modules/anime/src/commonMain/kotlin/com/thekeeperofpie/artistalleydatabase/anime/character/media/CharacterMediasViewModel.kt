package com.thekeeperofpie.artistalleydatabase.anime.character.media

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_character_medias_error_loading
import com.anilist.data.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class CharacterMediasViewModel(
    aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    favoritesController: FavoritesController,
    featureOverrideProvider: FeatureOverrideProvider,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : HeaderAndListViewModel<CharacterMediasScreen.Entry, MediaPreview, MediaPreviewEntry, MediaSortOption, CharacterMediaSortFilterController.FilterParams>(
    aniListApi = aniListApi,
    loadingErrorTextRes = Res.string.anime_character_medias_error_loading,
) {
    private val destination =
        savedStateHandle.toDestination<AnimeDestination.CharacterMedias>(navigationTypeMap)
    val characterId = destination.characterId
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    override val sortFilterController =
        CharacterMediaSortFilterController(viewModelScope, settings, featureOverrideProvider)

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result } },
            entryToId = { it.character.id.toString() },
            entryToType = { FavoriteType.CHARACTER },
            entryToFavorite = { it.character.isFavourite },
        )
    }

    override fun makeEntry(item: MediaPreview) = MediaPreviewEntry(item)

    override fun entryId(entry: MediaPreviewEntry) = entry.media.id.toString()

    override suspend fun initialRequest(
        filterParams: CharacterMediaSortFilterController.FilterParams?,
    ) = CharacterMediasScreen.Entry(
        aniListApi.characterAndMedias(characterId = characterId)
    )

    override suspend fun pagedRequest(
        page: Int,
        filterParams: CharacterMediaSortFilterController.FilterParams?,
    ) = aniListApi.characterAndMediasPage(
        characterId = characterId,
        sort = filterParams!!.sort.selectedOption(MediaSortOption.TRENDING)
            .toApiValue(filterParams.sortAscending),
        onList = filterParams.onList,
        page = page,
    ).character.media.run { pageInfo to nodes }

    override fun Flow<PagingData<MediaPreviewEntry>>.transformFlow() =
        applyMediaStatusChanges(
            statusController = statusController,
            ignoreController = ignoreController,
            mediaFilteringData = settings.mediaFilteringData(false),
            mediaFilterable = { it.mediaFilterable },
            copy = { copy(mediaFilterable = it) },
        )
}
