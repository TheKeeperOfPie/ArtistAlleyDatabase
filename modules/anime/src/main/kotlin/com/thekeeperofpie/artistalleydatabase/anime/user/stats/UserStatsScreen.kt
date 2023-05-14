package com.thekeeperofpie.artistalleydatabase.anime.user.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilist.UserByIdQuery
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen

object UserStatsScreen {

    @Composable
    operator fun invoke(
        user: () -> UserByIdQuery.Data.User?,
        statistics: @Composable () -> AniListUserScreen.Entry.Statistics?,
        genreState: UserStatsGenreState,
        isAnime: Boolean,
        callback: AniListUserScreen.Callback,
        bottomNavBarPadding: @Composable () -> Dp = { 0.dp },
    ) {
        Column {
            var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth(),
                divider = { /* No divider, manually draw so that it's full width */ }
            ) {
                UserStatsTab.values().forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = stringResource(tab.textRes), maxLines = 1) }
                    )
                }
            }

            Divider()

            when (UserStatsTab.values()[selectedTabIndex]) {
                UserStatsTab.STATS -> UserStatsBasicScreen(
                    user = user,
                    statistics = statistics,
                    isAnime = isAnime,
                    bottomNavBarPadding = bottomNavBarPadding,
                )
                UserStatsTab.GENRES -> UserStatsGenreScreen(
                    statistics = statistics,
                    state = genreState,
                    callback = callback,
                    bottomNavBarPadding = bottomNavBarPadding,
                )
            }
        }
    }
}
