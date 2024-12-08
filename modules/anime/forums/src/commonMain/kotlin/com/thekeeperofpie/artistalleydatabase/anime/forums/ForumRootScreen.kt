package com.thekeeperofpie.artistalleydatabase.anime.forums

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
import androidx.compose.runtime.collectAsState
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
import artistalleydatabase.modules.anime.forums.generated.resources.Res
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_header
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_root_active_header
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_root_new_header
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_root_releases_header
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_search_icon_content_description
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_stickied_header
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_stickied_header_expand_icon_content_description
import artistalleydatabase.modules.anime.ui.generated.resources.anime_generic_view_all_content_description
import com.anilist.data.fragment.ForumThread
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.ErrorSnackbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.ui.generated.resources.Res as UiRes

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
)
object ForumRootScreen {

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        onRefresh: () -> Unit,
        entry: StateFlow<LoadingResult<Entry>>,
        userRoute: UserRoute,
    ) {
        val entry by entry.collectAsState()
        val pullRefreshState = rememberPullRefreshState(
            refreshing = entry.loading,
            onRefresh = onRefresh,
        )
        val snackbarHostState = remember { SnackbarHostState() }
        entry.ErrorSnackbar(snackbarHostState)

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        Scaffold(
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    TopAppBar(
                        title = { Text(text = stringResource(Res.string.anime_forum_header)) },
                        navigationIcon = { upIconOption?.let { UpIconButton(upIconOption) } },
                        actions = {
                            val navHostController = LocalNavHostController.current
                            IconButton(onClick = {
                                navHostController.navigate(ForumDestinations.ForumSearch())
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
                val data = entry.result
                if (data == null) {
                    if (!entry.loading) {
                        VerticalList.NoResults()
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        categoryChips()
                        content(
                            entry = data,
                            stickiedExpanded = stickiedExpanded,
                            onStickiedExpandedChange = { stickiedExpanded = it },
                            userRoute = userRoute,
                        )
                    }
                }

                PullRefreshIndicator(
                    refreshing = entry.loading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    private fun LazyListScope.content(
        entry: Entry,
        stickiedExpanded: Boolean,
        onStickiedExpandedChange: (Boolean) -> Unit,
        userRoute: UserRoute,
    ) {
        stickiedSection(entry.stickied, stickiedExpanded, onStickiedExpandedChange)

        threadSection(
            titleRes = Res.string.anime_forum_root_active_header,
            entry.active,
            ForumDestinations.ForumSearch(
                title = ForumDestinations.ForumSearch.Title.Active,
                sort = ForumThreadSortOption.REPLIED_AT,
            ),
            userRoute = userRoute,
        )
        threadSection(
            titleRes = Res.string.anime_forum_root_new_header,
            entry.new,
            ForumDestinations.ForumSearch(
                title = ForumDestinations.ForumSearch.Title.New,
                sort = ForumThreadSortOption.CREATED_AT,
            ),
            userRoute = userRoute,
        )
        threadSection(
            titleRes = Res.string.anime_forum_root_releases_header,
            entry.releases,
            ForumDestinations.ForumSearch(
                title = ForumDestinations.ForumSearch.Title.Releases,
                categoryId = ForumCategoryOption.RELEASE_DISCUSSIONS.categoryId.toString(),
            ),
            userRoute = userRoute,
        )
    }

    private fun LazyListScope.categoryChips() {
        item("categoryChips") {
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
                val categories = ForumCategoryOption.entries
                items(items = categories, key = { it.categoryId }, contentType = { "category" }) {
                    val name = stringResource(it.textRes)
                    val navHostController = LocalNavHostController.current
                    SuggestionChip(
                        onClick = {
                            navHostController.navigate(
                                ForumDestinations.ForumSearch(
                                    title = ForumDestinations.ForumSearch.Title.Custom(name),
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

    private fun LazyListScope.header(titleRes: StringResource, viewAllRoute: NavDestination) {
        item("header-$titleRes") {
            NavigationHeader(
                titleRes = titleRes,
                viewAllRoute = viewAllRoute,
                viewAllContentDescriptionTextRes =
                    UiRes.string.anime_generic_view_all_content_description,
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
        viewAllRoute: NavDestination,
        userRoute: UserRoute,
    ) {
        header(titleRes = titleRes, viewAllRoute = viewAllRoute)
        itemsIndexed(
            items = threads,
            key = { _, item -> "$titleRes-${item.id}" },
            contentType = { _, _ -> "thread" },
        ) { index, item ->
            ThreadCard(
                thread = item,
                userRoute = userRoute,
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

    data class Entry(
        val stickied: List<ForumThread>,
        val active: List<ForumThread>,
        val new: List<ForumThread>,
        val releases: List<ForumThread>,
    )
}
