package com.thekeeperofpie.artistalleydatabase.anime.forum

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_forum_header
import artistalleydatabase.modules.anime.generated.resources.anime_forum_root_active_header
import artistalleydatabase.modules.anime.generated.resources.anime_forum_root_new_header
import artistalleydatabase.modules.anime.generated.resources.anime_forum_root_releases_header
import artistalleydatabase.modules.anime.generated.resources.anime_forum_search_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_forum_stickied_header
import artistalleydatabase.modules.anime.generated.resources.anime_forum_stickied_header_expand_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_forum_view_all_content_description
import com.anilist.fragment.ForumThread
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.ErrorSnackbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
)
object ForumRootScreen {

    @Composable
    operator fun invoke(
        viewModel: ForumRootScreenViewModel,
        upIconOption: UpIconOption?,
    ) {
        val pullRefreshState = rememberPullRefreshState(
            refreshing = viewModel.content.loading,
            onRefresh = viewModel::refresh,
        )
        val snackbarHostState = remember { SnackbarHostState() }
        viewModel.content.ErrorSnackbar(snackbarHostState)

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        Scaffold(
            topBar = {
                val navigationCallback = LocalNavigationCallback.current
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    TopAppBar(
                        title = { Text(text = stringResource(Res.string.anime_forum_header)) },
                        navigationIcon = { upIconOption?.let { UpIconButton(upIconOption) } },
                        actions = {
                            IconButton(onClick = {
                                navigationCallback.navigate(AnimeDestination.ForumSearch())
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = stringResource(
                                        Res.string.anime_forum_search_icon_content_description
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .pullRefresh(pullRefreshState)
        ) {
            var stickiedExpanded by rememberSaveable { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                val data = viewModel.content.result
                if (data == null) {
                    if (!viewModel.content.loading) {
                        AnimeMediaListScreen.NoResults()
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        categoryChips()
                        content(
                            data = data,
                            stickiedExpanded = stickiedExpanded,
                            onStickiedExpandedChange = { stickiedExpanded = it },
                        )
                    }
                }

                PullRefreshIndicator(
                    refreshing = viewModel.content.loading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    private fun LazyListScope.content(
        data: ForumRootScreenViewModel.Entry,
        stickiedExpanded: Boolean,
        onStickiedExpandedChange: (Boolean) -> Unit,
    ) {
        stickiedSection(data.stickied, stickiedExpanded, onStickiedExpandedChange)

        threadSection(
            titleRes = Res.string.anime_forum_root_active_header,
            data.active,
            AnimeDestination.ForumSearch(
                title = AnimeDestination.ForumSearch.Title.Active,
                sort = ForumThreadSortOption.REPLIED_AT
            )
        )
        threadSection(
            titleRes = Res.string.anime_forum_root_new_header,
            data.new,
            AnimeDestination.ForumSearch(
                title = AnimeDestination.ForumSearch.Title.New,
                sort = ForumThreadSortOption.CREATED_AT,
            )
        )
        threadSection(
            titleRes = Res.string.anime_forum_root_releases_header,
            data.releases,
            AnimeDestination.ForumSearch(
                title = AnimeDestination.ForumSearch.Title.Releases,
                categoryId = ForumCategoryOption.RELEASE_DISCUSSIONS.categoryId.toString(),
            )
        )
    }

    private fun LazyListScope.categoryChips() {
        item("categoryChips") {
            val navigationCallback = LocalNavigationCallback.current
            LazyRow(
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 4.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    // SubcomposeLayout doesn't support fill max width, so use a really large number.
                    // The parent will clamp the actual width so all content still fits on screen.
                    .size(width = LocalWindowConfiguration.current.screenWidthDp, height = 40.dp)
                    .animateItem()
            ) {
                val categories = ForumCategoryOption.values()
                items(items = categories, key = { it.categoryId }, contentType = { "category" }) {
                    val name = stringResource(it.textRes)
                    SuggestionChip(
                        onClick = {
                            navigationCallback.navigate(
                                AnimeDestination.ForumSearch(
                                    title = AnimeDestination.ForumSearch.Title.Custom(name),
                                    categoryId = it.categoryId.toString(),
                                )
                            )
                        },
                        label = { Text(text = name) },
                    )
                }
            }
        }
    }

    private fun LazyListScope.header(titleRes: StringResource, viewAllRoute: AnimeDestination) {
        item("header-$titleRes") {
            NavigationHeader(
                titleRes = titleRes,
                viewAllRoute = viewAllRoute,
                viewAllContentDescriptionTextRes = Res.string.anime_forum_view_all_content_description,
                modifier = Modifier.animateItem()
            )
        }
    }

    private fun LazyListScope.stickiedSection(
        stickied: List<ForumThread>,
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
    ) {
        item("stickiedHeader") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) }
                    .animateItem()
            ) {
                Text(
                    text = stringResource(Res.string.anime_forum_stickied_header),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                )

                TrailingDropdownIconButton(
                    expanded = expanded,
                    onClick = { onExpandedChange(!expanded) },
                    contentDescription = stringResource(
                        Res.string.anime_forum_stickied_header_expand_icon_content_description
                    ),
                )
            }
        }
        if (expanded) {
            itemsIndexed(
                items = stickied,
                key = { _, item -> "stickied-${item.id}" },
                contentType = { _, _ -> "thread" },
            ) { index, item ->
                ThreadCompactCard(
                    thread = item,
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = if (index == stickied.lastIndex) 0.dp else 4.dp
                        )
                        .animateItem()
                )
            }
        }
    }

    private fun LazyListScope.threadSection(
        titleRes: StringResource,
        threads: List<ForumThread>,
        viewAllRoute: AnimeDestination,
    ) {
        header(titleRes = titleRes, viewAllRoute = viewAllRoute)
        itemsIndexed(
            items = threads,
            key = { _, item -> "$titleRes-${item.id}" },
            contentType = { _, _ -> "thread" },
        ) { index, item ->
            ThreadCard(
                thread = item,
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (index == threads.lastIndex) 0.dp else 16.dp
                    )
                    .animateItem()
            )
        }
    }
}
