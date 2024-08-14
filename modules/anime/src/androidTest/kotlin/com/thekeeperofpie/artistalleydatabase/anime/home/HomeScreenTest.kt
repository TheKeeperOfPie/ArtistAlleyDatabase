package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.onNodeWithTag
import androidx.paging.PagingData
import com.anilist.type.MediaType
import com.google.common.truth.Truth.assertThat
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.news.AnimeNewsController
import com.thekeeperofpie.artistalleydatabase.test_utils.HiltInjectExtension
import com.thekeeperofpie.artistalleydatabase.test_utils.TestActivity
import com.thekeeperofpie.artistalleydatabase.test_utils.spyStrict
import com.thekeeperofpie.artistalleydatabase.test_utils.whenever
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.LoadingResult
import dagger.hilt.android.testing.HiltAndroidTest
import de.mannodermaus.junit5.compose.createAndroidComposeExtension
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import javax.inject.Inject

@OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
@ExtendWith(HiltInjectExtension::class)
@HiltAndroidTest
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock("UI", mode = ResourceAccessMode.READ_WRITE)
class HomeScreenTest {

    @JvmField
    @RegisterExtension
    @ExperimentalTestApi
    val composeExtension = createAndroidComposeExtension<TestActivity>()

    @Inject
    lateinit var newsController: AnimeNewsController

    @Inject
    lateinit var aniListApi: AuthedAniListApi

    @Inject
    lateinit var mediaListStatusController: MediaListStatusController

    @Inject
    lateinit var ignoreController: IgnoreController

    @Inject
    lateinit var recommendationStatusController: RecommendationStatusController

    @Inject
    lateinit var activityStatusController: ActivityStatusController

    @Inject
    lateinit var settings: AnimeSettings

    @Inject
    lateinit var monetizationController: MonetizationController

    @Inject
    lateinit var notificationsController: NotificationsController

    @Inject
    lateinit var userMediaListController: UserMediaListController

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5])
    fun anySectionLoading_showsRootLoading(index: Int) {
        val (viewModel, mediaViewModel) = mockViewModels(
            newsLoading = index == 0,
            activityLoading = index == 1,
            recommendationsLoading = index == 2,
            mediaEntryLoading = index == 3,
            mediaCurrentLoading = index == 4,
            mediaReviewsLoading = index == 5,
        )

        composeExtension.use {
            setContent { HomeScreenContent(viewModel, mediaViewModel) }
            val node = onNodeWithTag("rootRefreshIndicator")
                .fetchSemanticsNode()
            val matcher = SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo.Indeterminate,
            )
            assertThat(node.children.any { matcher.matches(it) }).isTrue()
        }
    }

    @Test
    fun noSectionLoading_noRootLoading() {
        val (viewModel, mediaViewModel) = mockViewModels(
            newsLoading = false,
            activityLoading = false,
            recommendationsLoading = false,
            mediaEntryLoading = false,
            mediaCurrentLoading = false,
            mediaReviewsLoading = false,
        )

        composeExtension.use {
            setContent { HomeScreenContent(viewModel, mediaViewModel) }

            val node = onNodeWithTag("rootRefreshIndicator")
                .fetchSemanticsNode()
            val matcher = SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo.Indeterminate,
            )
            assertThat(node.children.any { matcher.matches(it) }).isFalse()
        }
    }

    @Test
    fun emptySection_stillShowsHeader() {
        val args = arrayOf(
            R.string.anime_news_home_title,
            R.string.anime_home_activity_label,
            R.string.anime_recommendations_home_title,
            R.string.anime_reviews_home_title,
        )

        val (viewModel, mediaViewModel) = mockViewModels()

        composeExtension.use {
            setContent { HomeScreenContent(viewModel, mediaViewModel) }
            val columnNode = onNodeWithTag("homeColumn")
            args.forEach {
                val headerIndex = columnNode.fetchSemanticsNode()
                    .config[SemanticsProperties.IndexForKey]
                    .invoke("header_$it")
                assertThat(headerIndex).isAtLeast(0)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun nonEmptyCurrent_showsHeader_isAnime(isAnime: Boolean) {
        val currentHeaderRes = if (isAnime) {
            R.string.anime_home_anime_current_header
        } else {
            R.string.anime_home_manga_current_header
        }
        val (viewModel, mediaViewModel) = mockViewModels(isAnime = isAnime)
        whenever(mediaViewModel.currentMedia) {
            LoadingResult.success(listOf(mock<UserMediaListController.MediaEntry>()))
        }
        composeExtension.use {
            setContent { HomeScreenContent(viewModel, mediaViewModel) }

            val columnNode = onNodeWithTag("homeColumn")
            val headerIndex = columnNode.fetchSemanticsNode()
                .config[SemanticsProperties.IndexForKey]
                .invoke("header_$currentHeaderRes")
            assertThat(headerIndex).isAtLeast(0)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun emptyCurrent_hidesHeader_isAnime(isAnime: Boolean) {
        val currentHeaderRes = if (isAnime) {
            R.string.anime_home_anime_current_header
        } else {
            R.string.anime_home_manga_current_header
        }
        val (viewModel, mediaViewModel) = mockViewModels(isAnime = isAnime)
        whenever(mediaViewModel.currentMedia) {
            LoadingResult.success(emptyList())
        }
        composeExtension.use {
            setContent { HomeScreenContent(viewModel, mediaViewModel) }

            val columnNode = onNodeWithTag("homeColumn")
            val headerIndex = columnNode.fetchSemanticsNode()
                .config[SemanticsProperties.IndexForKey]
                .invoke("header_$currentHeaderRes")
            assertThat(headerIndex).isEqualTo(-1)
        }
    }

    private fun mockViewModels(
        isAnime: Boolean = true,
        newsLoading: Boolean = false,
        activityLoading: Boolean = false,
        recommendationsLoading: Boolean = false,
        mediaEntryLoading: Boolean = false,
        mediaCurrentLoading: Boolean = false,
        mediaReviewsLoading: Boolean = false,
    ): Pair<AnimeHomeViewModel, AnimeHomeMediaViewModel> {
        val spiedNewsController = spyStrict(newsController) {
            whenever(newsDateDescending()) {
                if (newsLoading) {
                    null
                } else {
                    emptyList()
                }
            }
        }

        // TODO: spyStrict
        val viewModel = spy(
            AnimeHomeViewModel(
                newsController = spiedNewsController,
                aniListApi = aniListApi,
                mediaListStatusController = mediaListStatusController,
                ignoreController = ignoreController,
                recommendationStatusController = recommendationStatusController,
                activityStatusController = activityStatusController,
                settings = settings,
                monetizationController = monetizationController,
                notificationsController = notificationsController,
            )
        )

        whenever(viewModel.preferredMediaType) {
            if (isAnime) MediaType.ANIME else MediaType.MANGA
        }

        whenever(viewModel.activity) {
            val pagingData = if (activityLoading) {
                PagingData.empty()
            } else {
                PagingData.from(emptyList())
            }
            MutableStateFlow(pagingData)
        }
        whenever(viewModel.recommendations) {
            val pagingData = if (recommendationsLoading) {
                PagingData.empty()
            } else {
                PagingData.from(emptyList())
            }
            MutableStateFlow(pagingData)
        }

        val mediaViewModel = spy(
            if (isAnime) {
                AnimeHomeMediaViewModel.Anime(
                    aniListApi = aniListApi,
                    settings = settings,
                    ignoreController = ignoreController,
                    userMediaListController = userMediaListController,
                    mediaListStatusController = mediaListStatusController,
                )
            } else {
                AnimeHomeMediaViewModel.Manga(
                    aniListApi = aniListApi,
                    settings = settings,
                    ignoreController = ignoreController,
                    userMediaListController = userMediaListController,
                    mediaListStatusController = mediaListStatusController,
                )
            }
        )

        whenever(mediaViewModel.entry) {
            if (mediaEntryLoading) {
                LoadingResult.loading()
            } else {
                LoadingResult.empty()
            }
        }

        whenever(mediaViewModel.currentMedia) {
            if (mediaCurrentLoading) {
                LoadingResult.loading()
            } else {
                LoadingResult.empty()
            }
        }

        whenever(mediaViewModel.reviews) {
            val pagingData = if (mediaReviewsLoading) {
                PagingData.empty()
            } else {
                PagingData.from(emptyList())
            }
            MutableStateFlow(pagingData)
        }

        return viewModel to mediaViewModel
    }

    @Composable
    private fun HomeScreenContent(
        viewModel: AnimeHomeViewModel,
        mediaViewModel: AnimeHomeMediaViewModel,
    ) {
        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this,
                LocalIgnoreController provides ignoreController,
            ) {
                AnimeHomeScreen(
                    viewModel = viewModel,
                    upIconOption = null,
                    scrollStateSaver = ScrollStateSaver.STUB,
                    bottomNavigationState = null,
                    mediaViewModel = { mediaViewModel },
                )
            }
        }
    }
}
