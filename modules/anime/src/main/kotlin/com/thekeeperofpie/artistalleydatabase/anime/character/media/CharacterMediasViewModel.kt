package com.thekeeperofpie.artistalleydatabase.anime.character.media

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.anilist.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class CharacterMediasViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    favoritesController: FavoritesController,
    featureOverrideProvider: FeatureOverrideProvider,
    savedStateHandle: SavedStateHandle,
) : HeaderAndListViewModel<CharacterMediasScreen.Entry, MediaPreview, MediaPreviewEntry, MediaSortOption, CharacterMediaSortFilterController.FilterParams>(
    aniListApi = aniListApi,
    loadingErrorTextRes = R.string.anime_character_medias_error_loading,
) {
    val characterId = savedStateHandle.get<String>("characterId")!!
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    override val sortFilterController =
        CharacterMediaSortFilterController(settings, featureOverrideProvider)

    init {
        favoritesToggleHelper.initializeTracking(
            viewModel = this,
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
            settings = settings,
        )
}
