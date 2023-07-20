package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.StaffDetailsQuery.Data.Staff
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.character.DetailsCharacter
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object StaffDetailsScreen {

    @Composable
    operator fun invoke(
        viewModel: StaffDetailsViewModel = hiltViewModel(),
        upIconOption: UpIconOption?,
        headerValues: StaffHeaderValues,
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val expandedState = rememberExpandedState()

        var staffImageWidthToHeightRatio by remember {
            mutableFloatStateOf(headerValues.imageWidthToHeightRatio)
        }

        Scaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 120.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    StaffHeader(
                        staffId = viewModel.staffId,
                        upIconOption = upIconOption,
                        progress = it,
                        headerValues = headerValues,
                        onFavoriteChanged = {
                            viewModel.favoritesToggleHelper
                                .set(FavoriteType.STAFF, viewModel.staffId, it)
                        },
                        colorCalculationState = colorCalculationState,
                        onImageWidthToHeightRatioAvailable = { staffImageWidthToHeightRatio = it },
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
                                viewModel = viewModel,
                                entry = entry,
                                staffImageWidthToHeightRatio = { staffImageWidthToHeightRatio },
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
