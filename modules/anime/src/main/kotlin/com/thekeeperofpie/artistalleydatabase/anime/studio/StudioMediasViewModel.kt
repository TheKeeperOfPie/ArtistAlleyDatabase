package com.thekeeperofpie.artistalleydatabase.anime.studio

import androidx.paging.PagingData
import com.anilist.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class StudioMediasViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    private val ignoreList: AnimeMediaIgnoreList,
    private val settings: AnimeSettings,
) : HeaderAndListViewModel<StudioMediasScreen.Entry, MediaPreview, AnimeMediaListRow.Entry<MediaPreview>, MediaSortOption>(
    aniListApi = aniListApi,
    sortOptionEnum = MediaSortOption::class,
    sortOptionEnumDefault = MediaSortOption.POPULARITY,
    loadingErrorTextRes = R.string.anime_studio_medias_error_loading,
) {

    override fun makeEntry(item: MediaPreview) = AnimeMediaListRow.Entry(item)

    override fun entryId(entry: AnimeMediaListRow.Entry<MediaPreview>) = entry.media.id.toString()

    override suspend fun initialRequest(
        headerId: String,
        sortOption: MediaSortOption,
        sortAscending: Boolean
    ) = StudioMediasScreen.Entry(
        aniListApi.studioMedias(
            studioId = headerId,
            sort = sortOption.toApiValue(sortAscending),
        )
    )

    override suspend fun pagedRequest(
        entry: StudioMediasScreen.Entry,
        page: Int,
        sortOption: MediaSortOption,
        sortAscending: Boolean
    ) = if (page == 1) {
        val result = entry.studio.media
        result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
    } else {
        val result = aniListApi.studioMediasPage(
            studioId = entry.studio.id.toString(),
            sort = sortOption.toApiValue(sortAscending),
            page = page,
        ).studio.media
        result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
    }

    override fun Flow<PagingData<AnimeMediaListRow.Entry<MediaPreview>>>.transformFlow() =
        applyMediaStatusChanges(
            statusController = statusController,
            ignoreList = ignoreList,
            settings = settings,
            media = { it.media },
            copy = { mediaListStatus, progress, progressVolumes, ignored ->
                AnimeMediaListRow.Entry(
                    media = this.media,
                    mediaListStatus = mediaListStatus,
                    progress = progress,
                    progressVolumes = progressVolumes,
                    ignored = ignored,
                )
            },
        )

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry<*>) =
        ignoreList.toggle(entry.media.id.toString())
}
