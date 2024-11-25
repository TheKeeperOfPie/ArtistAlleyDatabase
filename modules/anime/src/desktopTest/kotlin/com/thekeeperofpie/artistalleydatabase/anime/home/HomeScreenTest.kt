package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.paging.PagingData
import app.cash.burst.Burst
import app.cash.burst.burstValues
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_home_activity_label
import artistalleydatabase.modules.anime.generated.resources.anime_home_anime_current_header
import artistalleydatabase.modules.anime.generated.resources.anime_home_manga_current_header
import artistalleydatabase.modules.anime.generated.resources.anime_news_home_title
import artistalleydatabase.modules.anime.generated.resources.anime_recommendations_home_title
import artistalleydatabase.modules.anime.generated.resources.anime_reviews_home_title
import com.anilist.data.type.MediaType
import com.google.common.truth.Truth.assertThat
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityEntry
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeMediaViewModel.CurrentMediaState
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.fakeIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditState
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsEntry
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationEntry
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewEntry
import com.thekeeperofpie.artistalleydatabase.test_utils.ComposeTestRoot
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.loading
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.jetbrains.compose.resources.getString
import kotlin.test.Test

@OptIn(
    ExperimentalTestApi::class, ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class, ExperimentalCoroutinesApi::class
)
@Burst
class HomeScreenTest {

    // TODO: Replace with Burst: https://github.com/cashapp/burst/issues/72
    @Test
    fun anySectionLoading_showsRootLoading() = listOf(0, 1, 2, 3, 4, 5)
        .forEach(::anySectionLoading_showsRootLoading)

    fun anySectionLoading_showsRootLoading(index: Int = burstValues(0, 1, 2, 3, 4, 5)) =
        runComposeUiTest {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            setContent {
                HomeScreen(
                    activities = if (index == 0) PagingData.loading() else PagingData.empty(),
                    recommendations = if (index == 1) PagingData.loading() else PagingData.empty(),
                    reviews = if (index == 2) PagingData.loading() else PagingData.empty(),
                    news = if (index == 3) LoadingResult.loading() else LoadingResult.empty(),
                    homeEntry = if (index == 4) LoadingResult.loading() else LoadingResult.empty(),
                    currentMedia = if (index == 5) LoadingResult.loading() else LoadingResult.empty(),
                )
            }
            val node = onNodeWithTag("rootRefreshIndicator")
                .fetchSemanticsNode()
            val matcher = SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo.Indeterminate,
            )
            assertThat(node.children.any { matcher.matches(it) }).isTrue()
        }

    @Test
    fun noSectionLoading_noRootLoading() = runComposeUiTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        setContent {
            HomeScreen(
                activities = PagingData.empty(),
                recommendations = PagingData.empty(),
                reviews = PagingData.empty(),
                news = LoadingResult.empty(),
                homeEntry = LoadingResult.empty(),
                currentMedia = LoadingResult.empty(),
            )
        }

        val node = onNodeWithTag("rootRefreshIndicator")
            .fetchSemanticsNode()
        val matcher = SemanticsMatcher.expectValue(
            SemanticsProperties.ProgressBarRangeInfo,
            ProgressBarRangeInfo.Indeterminate,
        )
        assertThat(node.children.any { matcher.matches(it) }).isFalse()
    }

    @Test
    fun emptySection_stillShowsHeader() = runComposeUiTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        setContent {
            HomeScreen(
                activities = PagingData.empty(),
                recommendations = PagingData.empty(),
                reviews = PagingData.empty(),
                news = LoadingResult.empty(),
                homeEntry = LoadingResult.empty(),
                currentMedia = LoadingResult.empty(),
            )
        }

        val columnNode = onNodeWithTag("homeColumn")
        listOf(
            Res.string.anime_news_home_title,
            Res.string.anime_home_activity_label,
            Res.string.anime_recommendations_home_title,
            Res.string.anime_reviews_home_title,
        ).forEach {
            val text = runBlocking { getString(it) }
            columnNode.onChildren()
                .filterToOne(hasTextExactly(text))
                .assertExists()
        }
    }

    @Test
    fun nonEmptyCurrent_showsHeader_anime() = nonEmptyCurrent_showsHeader(true)

    @Test
    fun nonEmptyCurrent_showsHeader_manga() = nonEmptyCurrent_showsHeader(false)

    fun nonEmptyCurrent_showsHeader(isAnime: Boolean) = runComposeUiTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        setContent {
            HomeScreen(
                activities = PagingData.empty(),
                recommendations = PagingData.empty(),
                reviews = PagingData.empty(),
                news = LoadingResult.empty(),
                homeEntry = LoadingResult.empty(),
                currentMedia = LoadingResult.success(
                    listOf(
                        UserMediaListController.MediaEntry(
                            media = UserMediaListController.MediaEntry.Media(id = 1234),
                        )
                    )
                ),
                isAnime = isAnime,
            )
        }
        val currentHeaderRes = if (isAnime) {
            Res.string.anime_home_anime_current_header
        } else {
            Res.string.anime_home_manga_current_header
        }

        val text = runBlocking { getString(currentHeaderRes) }
        onNodeWithTag("homeColumn")
            .onChildren()
            .filterToOne(hasTextExactly(text))
            .assertExists()
    }

    @Test
    fun emptyCurrent_hidesHeader_anime() = emptyCurrent_hidesHeader(true)

    @Test
    fun emptyCurrent_hidesHeader_manga() = emptyCurrent_hidesHeader(false)

    fun emptyCurrent_hidesHeader(isAnime: Boolean) = runComposeUiTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        setContent {
            HomeScreen(
                activities = PagingData.empty(),
                recommendations = PagingData.empty(),
                reviews = PagingData.empty(),
                news = LoadingResult.empty(),
                homeEntry = LoadingResult.empty(),
                currentMedia = LoadingResult.success(emptyList<UserMediaListController.MediaEntry>()),
                isAnime = isAnime,
            )
        }
        val currentHeaderRes = if (isAnime) {
            Res.string.anime_home_anime_current_header
        } else {
            Res.string.anime_home_manga_current_header
        }

        val text = runBlocking { getString(currentHeaderRes) }
        onNodeWithTag("homeColumn")
            .onChildren()
            .filterToOne(hasTextExactly(text))
            .assertDoesNotExist()
    }

    @Composable
    private fun HomeScreen(
        activities: PagingData<ActivityEntry>,
        recommendations: PagingData<RecommendationEntry<MediaCompactWithTagsEntry>>,
        reviews: PagingData<ReviewEntry>,
        news: LoadingResult<List<AnimeNewsEntry<*>>>,
        homeEntry: LoadingResult<AnimeHomeDataEntry>,
        currentMedia: LoadingResult<List<UserMediaListController.MediaEntry>>,
        isAnime: Boolean = true,
    ) {
        val scope = rememberCoroutineScope()
        val ignoreController = remember { fakeIgnoreController(scope) }
        ComposeTestRoot(LocalIgnoreController provides ignoreController) {
            val currentMediaState = remember {
                CurrentMediaState(
                    result = currentMedia,
                    headerTextRes = if (isAnime) {
                        Res.string.anime_home_anime_current_header
                    } else {
                        Res.string.anime_home_manga_current_header
                    },
                    mediaType = if (isAnime) MediaType.ANIME else MediaType.MANGA,
                    previousSize = 0,
                )
            }
            val mediaEditState = remember { MediaEditState() }
            AnimeHomeScreen(
                upIconOption = null,
                scrollStateSaver = ScrollStateSaver.STUB,
                bottomNavigationState = null,
                selectedIsAnime = isAnime,
                onSelectedIsAnimeChanged = {},
                onRefresh = {},
                activity = remember { MutableStateFlow(activities) }.collectAsLazyPagingItems(),
                recommendations = remember { MutableStateFlow(recommendations) }.collectAsLazyPagingItems(),
                reviews = remember { MutableStateFlow(reviews) }.collectAsLazyPagingItems(),
                news = { news },
                homeEntry = { homeEntry },
                currentMedia = { currentMedia },
                currentMediaState = { currentMediaState },
                suggestions = emptyList(),
                notificationsUnreadCount = { 0 },
                unlocked = { false },
                viewer = { null },
                onActivityStatusUpdate = {},
                onUserRecommendationRating = { _, _ -> },
                onEditSheetValueChange = { true },
                editOnAttemptDismiss = { true },
                editState = { mediaEditState },
                editEventSink = {},
                onClickListEdit = {},
                onClickIncrementProgress = {},
            )
        }
    }
}
