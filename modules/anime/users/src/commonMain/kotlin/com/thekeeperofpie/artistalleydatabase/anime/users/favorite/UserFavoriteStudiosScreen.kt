package com.thekeeperofpie.artistalleydatabase.anime.users.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.paging.compose.LazyPagingItems
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList

@OptIn(ExperimentalMaterial3Api::class)
object UserFavoriteStudiosScreen {

    @Composable
    operator fun <StudioEntry : Any> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        upIconOption: UpIconOption? = null,
        onRefresh: () -> Unit,
        title: @Composable () -> String,
        studios: LazyPagingItems<StudioEntry>,
        studioId: (StudioEntry) -> String,
        studioRow: @Composable (
            StudioEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            Scaffold(
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        TopAppBar(
                            title = { AutoResizeHeightText(text = title(), maxLines = 1) },
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
                modifier = Modifier.padding(padding)
            ) { scaffoldPadding ->
                VerticalList(
                    itemHeaderText = null,
                    items = studios,
                    itemKey = studioId,
                    item = { studioRow(it, onClickListEdit) },
                    onRefresh = onRefresh,
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    modifier = Modifier.padding(scaffoldPadding)
                )
            }
        }
    }
}
