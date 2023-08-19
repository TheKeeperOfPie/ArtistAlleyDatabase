package com.thekeeperofpie.artistalleydatabase.anime.character.media

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.anilist.fragment.MediaPreview
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
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
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
) : HeaderAndListViewModel<CharacterMediasScreen.Entry, MediaPreview, MediaPreviewEntry, MediaSortOption>(
    aniListApi = aniListApi,
    sortOptionEnum = MediaSortOption::class,
    sortOptionEnumDefault = MediaSortOption.POPULARITY,
    loadingErrorTextRes = R.string.anime_character_medias_error_loading,
) {
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    override fun initialize(headerId: String) {
        super.initialize(headerId)
        favoritesToggleHelper.initializeTracking(
            viewModel = this,
            entry = { snapshotFlow { entry } },
            entryToId = { it.character.id.toString() },
            entryToType = { FavoriteType.CHARACTER },
            entryToFavorite = { it.character.isFavourite },
        )
    }

    override fun makeEntry(item: MediaPreview) = MediaPreviewEntry(item)

    override fun entryId(entry: MediaPreviewEntry) = entry.media.id.toString()

    override suspend fun initialRequest(
        headerId: String,
        sortOption: MediaSortOption,
        sortAscending: Boolean,
    ) = CharacterMediasScreen.Entry(
        aniListApi.characterAndMedias(
            characterId = headerId,
            sort = sortOption.toApiValue(sortAscending),
        )
    )

    override suspend fun pagedRequest(
        entry: CharacterMediasScreen.Entry,
        page: Int,
        sortOption: MediaSortOption,
        sortAscending: Boolean,
    ) = if (page == 1) {
        val result = entry.character.media
        result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
    } else {
        val result = aniListApi.characterAndMediasPage(
            characterId = entry.character.id.toString(),
            sort = sortOption.toApiValue(sortAscending),
            page = page,
        ).character.media
        result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
    }

    override fun Flow<PagingData<MediaPreviewEntry>>.transformFlow() =
        applyMediaStatusChanges(
            statusController = statusController,
            ignoreController = ignoreController,
            settings = settings,
        )

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry) = ignoreController.toggle(entry.media)
}
