package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_genre_info_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_tag_info_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_tag_search_show_when_spoiler
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_view_option_icon_content_description
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaGenre
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.widthAdaptiveCells
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.OnChangeEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.media.data.generated.resources.Res as MediaDataRes

@OptIn(ExperimentalMaterial3Api::class)
object MediaSearchScreen {

    @Composable
    operator fun invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        state: State,
        upIconOption: UpIconOption?,
        title: SearchDestinations.SearchMedia.Title?,
        onRefresh: () -> Unit,
        content: LazyPagingItems<AnimeSearchEntry>,
        tagId: String?,
        genre: String?,
        sortFilterState: () -> SortFilterState<*>,
        showWithSpoiler: () -> MutableStateFlow<Boolean>,
        onLongClickTag: (String) -> Unit,
        onLongClickGenre: (String) -> Unit,
        item: @Composable (
            AnimeSearchEntry.Media<*>?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
    ) {
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            SortFilterBottomScaffold(
                state = sortFilterState(),
                topBar = {
                    TopBar(
                        state = state,
                        upIconOption = upIconOption,
                        title = title,
                        tagId = tagId,
                        genre = genre,
                        scrollBehavior = scrollBehavior,
                        showWithSpoiler = showWithSpoiler,
                        onLongClickTag = onLongClickTag,
                        onLongClickGenre = onLongClickGenre,
                    )
                },
                modifier = Modifier.Companion.padding(padding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { scaffoldPadding ->
                val gridState = rememberLazyGridState()
                val mediaViewOption by state.mediaViewOption.collectAsStateWithLifecycle()
                val columns = mediaViewOption.widthAdaptiveCells
                sortFilterState().ImmediateScrollResetEffect(gridState)
                val showWithSpoiler by showWithSpoiler().collectAsStateWithLifecycle()
                OnChangeEffect(showWithSpoiler) {
                    gridState.scrollToItem(0)
                }
                VerticalList(
                    itemHeaderText = null,
                    items = content,
                    itemKey = { it.entryId.scopedId },
                    gridState = gridState,
                    onRefresh = onRefresh,
                    columns = columns,
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 32.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.Companion.padding(scaffoldPadding)
                ) {
                    item(it as? AnimeSearchEntry.Media<*>, onClickListEdit)
                }
            }
        }
    }

    @Composable
    private fun TopBar(
        state: State,
        upIconOption: UpIconOption?,
        title: SearchDestinations.SearchMedia.Title?,
        tagId: String?,
        genre: String?,
        scrollBehavior: TopAppBarScrollBehavior,
        showWithSpoiler: () -> MutableStateFlow<Boolean>,
        onLongClickTag: (String) -> Unit,
        onLongClickGenre: (String) -> Unit,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            Column {
                val text = title?.text().orEmpty()

                TopAppBar(
                    title = { Text(text = text, maxLines = 1) },
                    navigationIcon = {
                        if (upIconOption != null) {
                            UpIconButton(option = upIconOption)
                        }
                    },
                    actions = {
                        val infoContentDescriptionRes = remember {
                            if (tagId != null) {
                                MediaDataRes.string.anime_media_tag_info_content_description
                            } else if (genre != null) {
                                if (MediaGenre.entries.any { it.id == genre }) {
                                    MediaDataRes.string.anime_media_genre_info_content_description
                                } else null
                            } else null
                        }

                        if (infoContentDescriptionRes != null) {
                            IconButton(onClick = {
                                if (tagId != null) {
                                    onLongClickTag(tagId)
                                } else if (genre != null) {
                                    onLongClickGenre(genre)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = stringResource(infoContentDescriptionRes),
                                )
                            }
                        }

                        var mediaViewOption by state.mediaViewOption
                            .collectAsMutableStateWithLifecycle()
                        val nextMediaViewOption = MediaViewOption.values()
                            .let { it[(it.indexOf(mediaViewOption) + 1) % it.size] }
                        IconButton(onClick = {
                            mediaViewOption = nextMediaViewOption
                        }) {
                            Icon(
                                imageVector = nextMediaViewOption.icon,
                                contentDescription = stringResource(
                                    MediaDataRes.string.anime_media_view_option_icon_content_description
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

                var selectedType by state.selectedType.collectAsMutableStateWithLifecycle()
                if (tagId != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.Companion.padding(horizontal = 16.dp)
                    ) {
                        var showWithSpoiler by showWithSpoiler().collectAsMutableStateWithLifecycle()
                        Switch(
                            checked = showWithSpoiler,
                            onCheckedChange = { showWithSpoiler = it },
                        )

                        Text(
                            text = stringResource(MediaDataRes.string.anime_media_tag_search_show_when_spoiler),
                            modifier = Modifier.Companion.padding(vertical = 16.dp)
                        )
                    }
                }

                val selectedIsAnime = selectedType == SearchType.ANIME
                TabRow(
                    selectedTabIndex = if (selectedIsAnime) 0 else 1,
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .align(Alignment.Companion.CenterHorizontally)
                ) {
                    Tab(
                        selected = selectedIsAnime,
                        onClick = { selectedType = SearchType.ANIME },
                        text = {
                            Text(
                                text = stringResource(SearchType.ANIME.tabText),
                                maxLines = 1,
                            )
                        }
                    )
                    Tab(
                        selected = !selectedIsAnime,
                        onClick = { selectedType = SearchType.MANGA },
                        text = {
                            Text(
                                text = stringResource(SearchType.MANGA.tabText),
                                maxLines = 1,
                            )
                        }
                    )
                }
            }
        }
    }

    @Stable
    class State(
        val selectedType: MutableStateFlow<SearchType>,
        val mediaViewOption: MutableStateFlow<MediaViewOption>,
    )
}
