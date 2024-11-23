package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anilist.data.StaffDetailsQuery.Data.Staff
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object StaffDetailsScreen {

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        viewModel: StaffDetailsViewModel = viewModel {
            animeComponent.staffDetailsViewModel(createSavedStateHandle())
        },
        upIconOption: UpIconOption?,
        headerValues: StaffHeaderValues,
        sharedTransitionKey: SharedTransitionKey?,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
        val expandedState = rememberExpandedState()
        val coverImageState = rememberCoilImageState(headerValues.coverImage)

        val snackbarHostState = remember { SnackbarHostState() }
        val error = viewModel.error
        val errorText = error?.first?.let { stringResource(it) }
        LaunchedEffect(errorText) {
            if (errorText != null) {
                snackbarHostState.showSnackbar(
                    message = errorText,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )
            }
        }
        MediaEditBottomSheetScaffold(
            viewModel = viewModel { animeComponent.mediaEditViewModel() },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                        sharedTransitionKey = sharedTransitionKey,
                        coverImageState = coverImageState,
                        onFavoriteChanged = {
                            viewModel.favoritesToggleHelper
                                .set(FavoriteType.STAFF, viewModel.staffId, it)
                        },
                    )
                }
            }
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
                        val errorRes = viewModel.error
                        AnimeMediaListScreen.Error(
                            errorTextRes = errorRes?.first,
                            exception = errorRes?.second,
                        )
                    }
                } else {
                    val mediaTimeline by viewModel.mediaTimeline.collectAsState()
                    val staffTimeline by viewModel.staffTimeline.collectAsState()
                    val staffTabs = StaffTab.values()
                        .filter { it != StaffTab.MEDIA || mediaTimeline.yearsToCharacters.isNotEmpty() }
                        .filter { it != StaffTab.STAFF || staffTimeline.yearsToMedia.isNotEmpty() }
                    val pagerState = rememberPagerState(pageCount = { staffTabs.size })
                    val scope = rememberCoroutineScope()
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        divider = { /* No divider, manually draw so that it's full width */ }
                    ) {
                        staffTabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = { Text(text = stringResource(tab.textRes), maxLines = 1) }
                            )
                        }
                    }

                    HorizontalDivider()

                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = false,
                        pageNestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    ) {
                        when (staffTabs[it]) {
                            StaffTab.OVERVIEW -> StaffOverviewScreen(
                                viewModel = viewModel,
                                entry = entry,
                                coverImageState = coverImageState,
                                expandedState = expandedState,
                            )
                            StaffTab.MEDIA -> StaffMediaScreen(
                                mediaTimeline = mediaTimeline,
                                onRequestYear = viewModel::onRequestMediaYear,
                            )
                            StaffTab.STAFF -> StaffStaffScreen(staffTimeline = staffTimeline)
                        }
                    }
                }
            }
        }
    }

    data class Entry(
        val staff: Staff,
        val description: MarkdownText?,
    )

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
