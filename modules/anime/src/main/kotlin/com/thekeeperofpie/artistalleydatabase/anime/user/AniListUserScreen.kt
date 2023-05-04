package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.anilist.UserByIdQuery
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.descriptionSection
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("NAME_SHADOWING")
object AniListUserScreen {

    @Composable
    operator fun invoke(
        user: @Composable () -> UserByIdQuery.Data.User?,
        scrollStateSaver: ScrollStateSaver,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val user = user()
        Scaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 280.dp,
                    pinnedHeight = 180.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    CoverAndBannerHeader(
                        coverImage = { user?.avatar?.large },
                        bannerImage = { user?.bannerImage },
                        coverSize = 180.dp,
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            AutoResizeHeightText(
                                text = user?.name.orEmpty(),
                                style = MaterialTheme.typography.headlineLarge,
                                maxLines = 1,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 10.dp,
                                        bottom = 10.dp
                                    ),
                            )
                        }
                    }
                }
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            LazyColumn(
                state = scrollStateSaver.lazyListState(),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                if (user == null) {
                    item {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp)
                            )
                        }
                    }
                    return@LazyColumn
                }

                descriptionSection(
                    titleTextRes = R.string.anime_user_about_label,
                    htmlText = user.about?.trim()
                )
            }
        }
    }
}
