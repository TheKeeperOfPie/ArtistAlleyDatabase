package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.StaffDetailsQuery.Data.Staff
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.character.DetailsCharacter
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import kotlinx.coroutines.launch

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object StaffDetailsScreen {

    @Composable
    operator fun invoke(
        viewModel: StaffDetailsViewModel = hiltViewModel(),
        coverImage: @Composable () -> String? = { null },
        coverImageWidthToHeightRatio: Float = 1f,
        title: @Composable () -> String = { "First Last" },
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val expandedState = rememberExpandedState()
        Scaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 180.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    Header(
                        staffId = viewModel.staffId,
                        progress = it,
                        color = { viewModel.colorMap[viewModel.staffId]?.first },
                        coverImage = coverImage,
                        coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
                        titleText = title,
                        subtitleText = {
                            viewModel.entry?.staff?.name?.run {
                                if (native != userPreferred) {
                                    native
                                } else if (full != userPreferred) {
                                    full
                                } else {
                                    null
                                }
                            }
                        },
                        colorCalculationState = colorCalculationState,
                    )
                }
            },
            snackbarHost = {
                val errorRes = viewModel.errorResource
                if (errorRes != null) {
                    SnackbarErrorText(
                        errorRes.first,
                        errorRes.second,
                        onErrorDismiss = { viewModel.errorResource = null },
                    )
                }
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { scaffoldPadding ->
            Column(modifier = Modifier.padding(scaffoldPadding)) {
                val entry = viewModel.entry
                if (entry == null) {
                    if (viewModel.loading) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp)
                            )
                        }
                    } else {
                        val errorRes = viewModel.errorResource
                        AnimeMediaListScreen.Error(
                            errorTextRes = errorRes?.first,
                            exception = errorRes?.second,
                        )
                    }
                } else {
                    val pagerState = rememberPagerState(pageCount = { StaffTab.values().size })
                    val scope = rememberCoroutineScope()
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        divider = { /* No divider, manually draw so that it's full width */ }
                    ) {
                        StaffTab.values().forEachIndexed { index, tab ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = { Text(text = stringResource(tab.textRes), maxLines = 1) }
                            )
                        }
                    }

                    Divider()

                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = false,
                        pageNestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    ) {
                        when (StaffTab.values()[it]) {
                            StaffTab.OVERVIEW -> StaffOverviewScreen(
                                entry = entry,
                                colorCalculationState = colorCalculationState,
                                expandedState = expandedState,
                                navigationCallback = navigationCallback,
                            )
                            StaffTab.MEDIA -> StaffMediaScreen(
                                mediaTimeline = viewModel.mediaTimeline.collectAsState().value,
                                onRequestYear = viewModel::onRequestMediaYear,
                                colorCalculationState = colorCalculationState,
                                navigationCallback = navigationCallback,
                            )
                            StaffTab.STAFF -> StaffStaffScreen(
                                staffTimeline = viewModel.staffTimeline.collectAsState().value,
                                onRequestYear = viewModel::onRequestStaffYear,
                                colorCalculationState = colorCalculationState,
                                navigationCallback = navigationCallback,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Header(
        staffId: String,
        progress: Float,
        color: () -> Color?,
        coverImage: @Composable () -> String?,
        coverImageWidthToHeightRatio: Float,
        titleText: @Composable () -> String,
        subtitleText: @Composable () -> String?,
        colorCalculationState: ColorCalculationState,
    ) {
        CoverAndBannerHeader(
            screenKey = AnimeNavDestinations.STAFF_DETAILS.id,
            entryId = EntryId("anime_staff", staffId),
            pinnedHeight = 180.dp,
            progress = progress,
            color = color,
            coverImage = coverImage,
            coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
            coverImageOnSuccess = {
                ComposeColorUtils.calculatePalette(staffId, it, colorCalculationState)
            }
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AutoResizeHeightText(
                    text = titleText(),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                )
            }

            val subtitleText = subtitleText()
            AnimatedVisibility(subtitleText != null, label = "Staff details subtitle text") {
                if (subtitleText != null) {
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .wrapContentHeight(Alignment.Bottom)
                    )
                }
            }
        }
    }

    data class Entry(val staff: Staff, val showAdult: Boolean) {
        val characters = staff.characters?.nodes?.filterNotNull().orEmpty().map {
            DetailsCharacter(
                id = it.id.toString(),
                name = it.name?.userPreferred,
                image = it.image?.large,
                character = it,
            )
        }
    }

    @Composable
    private fun rememberExpandedState() = rememberSaveable(saver = listSaver(
        save = {
            listOf(
                it.description,
            )
        },
        restore = {
            ExpandedState(
                description = it[0],
            )
        }
    )) {
        ExpandedState()
    }

    class ExpandedState(
        description: Boolean = false,
    ) {
        var description by mutableStateOf(description)
    }
}

@Preview
@Composable
private fun Preview() {
    StaffDetailsScreen()
}
