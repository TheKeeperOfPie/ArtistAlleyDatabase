package com.thekeeperofpie.artistalleydatabase.anime.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.anilist.UserSocialActivityQuery.Data.Page.Activity
import com.anilist.UserSocialActivityQuery.Data.Page.ListActivityActivity
import com.anilist.UserSocialActivityQuery.Data.Page.MessageActivityActivity
import com.anilist.UserSocialActivityQuery.Data.Page.OtherActivity
import com.anilist.UserSocialActivityQuery.Data.Page.TextActivityActivity
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(ExperimentalMaterial3Api::class)
object AnimeActivityScreen {

    @Composable
    operator fun invoke(
        viewModel: AnimeActivityViewModel = hiltViewModel<AnimeActivityViewModel>(),
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val globalActivity = viewModel.globalActivity().collectAsLazyPagingItems()
        val followingActivity = viewModel.followingActivity().collectAsLazyPagingItems()

        val viewer by viewModel.viewer.collectAsState()
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        Scaffold(
            topBar = {
                if (viewer == null) {
                    AppBar(
                        text = stringResource(R.string.anime_activity_global_title),
                        upIconOption = UpIconOption.Back { navigationCallback.popUp() },
                    )
                } else {
                    Column {
                        AppBar(
                            text = stringResource(R.string.anime_activity_title),
                            upIconOption = UpIconOption.Back { navigationCallback.popUp() },
                        )

                        TabRow(selectedTabIndex = selectedTabIndex) {
                            Tab(selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                text = {
                                    Text(text = stringResource(R.string.anime_activity_tab_following))
                                }
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                text = { Text(text = stringResource(R.string.anime_activity_tab_global)) }
                            )
                        }
                    }
                }
            },
        ) {
            val uriHandler = LocalUriHandler.current
            if (viewer == null || selectedTabIndex == 1) {
                ActivityList(
                    activities = globalActivity,
                    scaffoldPadding = it,
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                )
            } else {
                ActivityList(
                    activities = followingActivity,
                    scaffoldPadding = it,
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                )
            }
        }
    }

    @Composable
    private fun ActivityList(
        activities: LazyPagingItems<Activity>,
        scaffoldPadding: PaddingValues,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 72.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(scaffoldPadding),
        ) {
            if (activities.itemCount == 0) {
                item("no_results") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            stringResource(id = R.string.anime_media_list_no_results),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
            } else {
                activities.itemKey()
                items(
                    count = activities.itemCount,
                    key = { index ->
                        val item = activities.peek(index)
                        if (item == null) {
                            "placeholder_$index"
                        } else {
                            when (item) {
                                is ListActivityActivity -> item.id
                                is MessageActivityActivity -> item.id
                                is TextActivityActivity -> item.id
                                is OtherActivity -> "other_$index"
                            }
                        }
                    },
                    contentType = {
                        when (activities[it]) {
                            is ListActivityActivity -> "list"
                            is MessageActivityActivity -> "message"
                            is TextActivityActivity -> "text"
                            is OtherActivity,
                            null -> null
                        }
                    }
                ) {
                    when (val activity = activities[it]) {
                        is TextActivityActivity -> TextActivitySmallCard(
                            activity = activity,
                            modifier = Modifier.fillMaxWidth()
                        )
                        is ListActivityActivity -> ListActivitySmallCard(
                            screenKey = AnimeNavDestinations.ACTIVITY.id,
                            activity = activity,
                            colorCalculationState = colorCalculationState,
                            navigationCallback = navigationCallback,
                            modifier = Modifier.fillMaxWidth()
                        )
                        is MessageActivityActivity,
                        is OtherActivity,
                        null -> TextActivitySmallCard(
                            activity = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
