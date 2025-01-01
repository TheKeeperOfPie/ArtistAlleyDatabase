package com.thekeeperofpie.artistalleydatabase.anime.users.follow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.users.UserListRow
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems

@OptIn(ExperimentalMaterial3Api::class)
object UserListScreen {

    @Composable
    operator fun <MediaEntry> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        upIconOption: UpIconOption?,
        sortFilterState: SortFilterState<*>,
        title: @Composable () -> String,
        users: LazyPagingItems<UserListRow.Entry<MediaEntry>>,
        mediaRow: LazyListScope.(
            List<MediaEntry?>,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
    ) {
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            SortFilterBottomScaffold2(
                state = sortFilterState,
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        TopAppBar(
                            title = { Text(text = title(), maxLines = 1) },
                            navigationIcon = {
                                if (upIconOption != null) {
                                    UpIconButton(option = upIconOption)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    lerp(0.dp, 16.dp, scrollBehavior.state.overlappedFraction)
                                )
                            ),
                        )
                    }
                },
                modifier = Modifier
                    .padding(padding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { sortFilterScaffoldPadding ->
                val gridState = rememberLazyGridState()
                sortFilterState.ImmediateScrollResetEffect(gridState)
                VerticalList(
                    itemHeaderText = null,
                    items = users,
                    itemKey = { it.user.id },
                    onRefresh = users::refresh,
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    item = {
                        SharedTransitionKeyScope("user_list_row", it?.user?.id?.toString()) {
                            UserListRow(
                                entry = it,
                                mediaRow = { media ->
                                    mediaRow(media, onClickListEdit)
                                }
                            )
                        }
                    },
                    modifier = Modifier.padding(sortFilterScaffoldPadding)
                )
            }
        }
    }
}
