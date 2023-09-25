package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.onNodeWithTag
import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.mxalbert.sharedelements.SharedElementsRoot
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsController
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.test_utils.HiltInjectExtension
import com.thekeeperofpie.artistalleydatabase.test_utils.TestActivity
import com.thekeeperofpie.artistalleydatabase.test_utils.whenever
import dagger.hilt.android.testing.HiltAndroidTest
import de.mannodermaus.junit5.compose.createAndroidComposeExtension
import io.noties.markwon.Markwon
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
import org.mockito.Mockito.spy
import javax.inject.Inject

@OptIn(ExperimentalTestApi::class)
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

    @Inject
    lateinit var markwon: Markwon

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4])
    fun anySectionLoading_showsRootLoading(index: Int) {
        val (viewModel, mediaViewModel) = mockViewModels(
            activityLoading = index == 0,
            recommendationsLoading = index == 1,
            mediaEntryLoading = index == 2,
            mediaCurrentLoading = index == 3,
            mediaReviewsLoading = index == 4,
        )

        composeExtension.use {
            setContent {
                SharedElementsRoot {
                    CompositionLocalProvider(
                        LocalIgnoreController.provides(ignoreController),
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
            activityLoading = false,
            recommendationsLoading = false,
            mediaEntryLoading = false,
            mediaCurrentLoading = false,
            mediaReviewsLoading = false,
        )

        synchronized(composeExtension) {
            composeExtension.use {
                setContent {
                    SharedElementsRoot {
                        CompositionLocalProvider(
                            LocalIgnoreController.provides(ignoreController),
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
                val node = onNodeWithTag("rootRefreshIndicator")
                    .fetchSemanticsNode()
                val matcher = SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo.Indeterminate,
                )
                assertThat(node.children.any { matcher.matches(it) }).isFalse()
            }
        }
    }

    private fun mockViewModels(
        activityLoading: Boolean,
        recommendationsLoading: Boolean,
        mediaEntryLoading: Boolean,
        mediaCurrentLoading: Boolean,
        mediaReviewsLoading: Boolean,
    ): Pair<AnimeHomeViewModel, AnimeHomeMediaViewModel> {
        val viewModel = spy(
            AnimeHomeViewModel(
                newsController = newsController,
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
            AnimeHomeMediaViewModel.Anime(
                aniListApi = aniListApi,
                settings = settings,
                ignoreController = ignoreController,
                userMediaListController = userMediaListController,
                mediaListStatusController = mediaListStatusController,
            )
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
}
