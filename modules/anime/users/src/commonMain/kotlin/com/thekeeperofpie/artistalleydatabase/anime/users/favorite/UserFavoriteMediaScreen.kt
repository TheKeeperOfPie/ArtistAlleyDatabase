package com.thekeeperofpie.artistalleydatabase.anime.users.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import artistalleydatabase.modules.anime.media.data.generated.resources.Res
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_view_option_icon_content_description
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.widthAdaptiveCells
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object UserFavoriteMediaScreen {

    @Composable
    operator fun <MediaEntry : Any> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        upIconOption: UpIconOption? = null,
        onRefresh: () -> Unit,
        title: @Composable () -> String,
        mediaViewOption: () -> MediaViewOption,
        onMediaViewOptionChanged: (MediaViewOption) -> Unit,
        media: LazyPagingItems<MediaEntry>,
        mediaId: (MediaEntry) -> String,
        mediaCell: @Composable (MediaEntry?, (MediaNavigationData) -> Unit) -> Unit,
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
                            actions = {
                                val mediaViewOption = mediaViewOption()
                                val nextMediaViewOption = MediaViewOption.entries
                                    .let { it[(it.indexOf(mediaViewOption) + 1) % it.size] }
                                IconButton(onClick = {
                                    onMediaViewOptionChanged(nextMediaViewOption)
                                }) {
                                    Icon(
                                        imageVector = nextMediaViewOption.icon,
                                        contentDescription = stringResource(
                                            Res.string.anime_media_view_option_icon_content_description
                                        ),
                                    )
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
                    items = media,
                    itemKey = mediaId,
                    item = { mediaCell(it, onClickListEdit) },
                    onRefresh = onRefresh,
                    columns = mediaViewOption().widthAdaptiveCells,
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    modifier = Modifier.padding(scaffoldPadding),
                )
            }
        }
    }
}
