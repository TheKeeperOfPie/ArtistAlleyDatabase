package com.thekeeperofpie.artistalleydatabase.anime.media.activity

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_activities_header
import artistalleydatabase.modules.anime.generated.resources.anime_media_activities_tab_following
import artistalleydatabase.modules.anime.generated.resources.anime_media_activities_tab_global
import com.anilist.data.MediaActivityQuery
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.activities.ListActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.users.UserDestinations
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object MediaActivitiesScreen {

    @Composable
    operator fun invoke(
        viewModel: MediaActivitiesViewModel,
        sortFilterState: SortFilterState<*>,
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
    ) {
        val entry = viewModel.entry
        val media = entry.result?.data?.media

        val animeComponent = LocalAnimeComponent.current
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        val viewer by viewModel.viewer.collectAsState()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
        val snackbarHostState = remember { SnackbarHostState() }

        val error = entry.error
        val errorString = error?.messageText()
        LaunchedEffect(errorString) {
            if (errorString != null) {
                snackbarHostState.showSnackbar(
                    message = errorString,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )
            }
        }

        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 120.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    MediaHeader(
                        viewer = viewer,
                        upIconOption = upIconOption,
                        mediaId = viewModel.mediaId,
                        mediaType = viewModel.entry.result?.data?.media?.type,
                        titles = entry.result?.titlesUnique,
                        episodes = media?.episodes,
                        format = media?.format,
                        averageScore = media?.averageScore,
                        popularity = media?.popularity,
                        progress = it,
                        headerValues = headerValues,
                        onFavoriteChanged = {
                            viewModel.favoritesToggleHelper.set(
                                headerValues.type.toFavoriteType(),
                                viewModel.mediaId,
                                it,
                            )
                        },
                        enableCoverImageSharedElement = false
                    )
                }
            },
            snackbarHostState = snackbarHostState,
        ) { scaffoldPadding ->
            SortFilterBottomScaffold(
                state = sortFilterState,
                modifier = Modifier.padding(scaffoldPadding)
            ) {
                val following = viewModel.following.collectAsLazyPagingItems()
                val global = viewModel.global.collectAsLazyPagingItems()
                val selectedIsFollowing = viewModel.selectedIsFollowing
                val items = if (selectedIsFollowing && viewer != null) following else global

                Column(modifier = Modifier.padding(it)) {
                    if (viewer != null) {
                        TabRow(selectedTabIndex = if (selectedIsFollowing) 0 else 1) {
                            Tab(
                                selected = selectedIsFollowing,
                                onClick = { viewModel.selectedIsFollowing = true },
                                text = {
                                    Text(stringResource(Res.string.anime_media_activities_tab_following))
                                },
                            )
                            Tab(
                                selected = !selectedIsFollowing,
                                onClick = { viewModel.selectedIsFollowing = false },
                                text = {
                                    Text(stringResource(Res.string.anime_media_activities_tab_global))
                                },
                            )
                        }
                    }

                    VerticalList(
                        itemHeaderText = Res.string.anime_media_activities_header,
                        items = items,
                        itemKey = { it.activityId },
                        onRefresh = items::refresh,
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 32.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    ) {
                        SharedTransitionKeyScope("activity_card", it?.activityId) {
                            ListActivitySmallCard(
                                viewer = viewer,
                                activity = it?.activity,
                                entry = it,
                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                userRoute = UserDestinations.User.route,
                                clickable = true,
                            )
                        }
                    }
                }
            }
        }
    }

    data class Entry(
        val data: MediaActivityQuery.Data,
    ) {
        val titlesUnique = data.media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()
    }
}
