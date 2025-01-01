package com.thekeeperofpie.artistalleydatabase.anime.studios

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anilist.data.StudioMediasQuery
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems

// TODO: Use the same year segmented view as staff screen
@OptIn(ExperimentalMaterial3Api::class)
object StudioMediasScreen {

    @Composable
    operator fun <MediaEntry : Any> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        sortFilterState: SortFilterState<*>,
        upIconOption: UpIconOption?,
        onRefresh: () -> Unit,
        media: LazyPagingItems<MediaEntry>,
        mediaItemKey: (MediaEntry) -> String,
        mediaRow: @Composable (
            MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
        name: () -> String,
        favorite: () -> Boolean?,
        onFavoriteChanged: (Boolean) -> Unit,
    ) {
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            SortFilterBottomScaffold(
                state = sortFilterState,
                topBar = {
                    TopAppBar(
                        title = { Text(text = name(), maxLines = 1) },
                        navigationIcon = {
                            if (upIconOption != null) {
                                UpIconButton(option = upIconOption)
                            }
                        },
                        actions = {
                            FavoriteIconButton(
                                favorite = favorite(),
                                onFavoriteChanged = onFavoriteChanged,
                            )
                        },
                        scrollBehavior = scrollBehavior,
                    )
                },
                modifier = Modifier.padding(padding)
            ) {
                val gridState = rememberLazyGridState()
                sortFilterState.ImmediateScrollResetEffect(gridState)
                VerticalList(
                    gridState = gridState,
                    onRefresh = onRefresh,
                    itemHeaderText = null,
                    itemKey = mediaItemKey,
                    items = media,
                    item = {
                        mediaRow(
                            it,
                            onClickListEdit,
                            Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        )
                    },
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    modifier = Modifier.padding(it)
                )
            }
        }
    }

    data class Entry(
        val studio: StudioMediasQuery.Data.Studio,
    )
}
