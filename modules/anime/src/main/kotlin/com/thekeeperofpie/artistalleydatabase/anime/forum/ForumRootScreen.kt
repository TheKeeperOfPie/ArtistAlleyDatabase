package com.thekeeperofpie.artistalleydatabase.anime.forum

import androidx.annotation.StringRes
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.fragment.ForumThread
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.ui.NavigationHeader
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.ErrorSnackbar
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
object ForumRootScreen {

    private val SCREEN_KEY = AnimeNavDestinations.FORUM.id

    @Composable
    operator fun invoke(
        viewModel: ForumRootScreenViewModel = hiltViewModel(),
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
                        title = { Text(text = stringResource(R.string.anime_forum_header)) },
                        navigationIcon = { upIconOption?.let { UpIconButton(upIconOption) } },
                        actions = {
                            IconButton(onClick = { navigationCallback.onForumSearchClick() }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = stringResource(
                                        R.string.anime_forum_search_icon_content_description
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
            titleRes = R.string.anime_forum_root_active_header,
            data.active,
            AnimeNavDestinations.FORUM_SEARCH.id
                    + "?titleRes=${R.string.anime_forum_root_active_title}"
                    + "&sort=${ForumThreadSortOption.REPLIED_AT.name}"
        )
        threadSection(
            titleRes = R.string.anime_forum_root_new_header,
            data.new,
            AnimeNavDestinations.FORUM_SEARCH.id
                    + "?titleRes=${R.string.anime_forum_root_new_title}"
                    + "&sort=${ForumThreadSortOption.CREATED_AT.name}"
        )
        threadSection(
            titleRes = R.string.anime_forum_root_releases_header,
            data.releases,
            AnimeNavDestinations.FORUM_SEARCH.id
                    + "?titleRes=${R.string.anime_forum_root_releases_title}"
                    + "&categoryId=${ForumCategoryOption.RELEASE_DISCUSSIONS.categoryId}"
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
                    .size(width = LocalConfiguration.current.screenWidthDp.dp, height = 40.dp)
                    .animateItemPlacement()
            ) {
                val categories = ForumCategoryOption.values()
                items(items = categories, key = { it.categoryId }, contentType = { "category" }) {
                    val name = stringResource(it.textRes)
                    SuggestionChip(
                        onClick = {
                            navigationCallback.onForumCategoryClick(name, it.categoryId.toString())
                        },
                        label = { Text(text = name) },
                    )
                }
            }
        }
    }

    private fun LazyListScope.header(@StringRes titleRes: Int, viewAllRoute: String) {
        item("header-$titleRes") {
            NavigationHeader(
                titleRes = titleRes,
                viewAllRoute = viewAllRoute,
                viewAllContentDescriptionTextRes = R.string.anime_forum_view_all_content_description,
                modifier = Modifier.animateItemPlacement()
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
                    .animateItemPlacement()
            ) {
                Text(
                    text = stringResource(R.string.anime_forum_stickied_header),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                )

                TrailingDropdownIconButton(
                    expanded = expanded,
                    onClick = { onExpandedChange(!expanded) },
                    contentDescription = stringResource(
                        R.string.anime_forum_stickied_header_expand_icon_content_description
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
                        .animateItemPlacement()
                )
            }
        }
    }

    private fun LazyListScope.threadSection(
        @StringRes titleRes: Int,
        threads: List<ForumThread>,
        viewAllRoute: String,
    ) {
        header(titleRes = titleRes, viewAllRoute = viewAllRoute)
        itemsIndexed(
            items = threads,
            key = { _, item -> "$titleRes-${item.id}" },
            contentType = { _, _ -> "thread" },
        ) { index, item ->
            ThreadCard(
                screenKey = SCREEN_KEY,
                thread = item,
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (index == threads.lastIndex) 0.dp else 16.dp
                    )
                    .animateItemPlacement()
            )
        }
    }
}
