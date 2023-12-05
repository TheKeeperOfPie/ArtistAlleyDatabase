package com.thekeeperofpie.artistalleydatabase.anime.media

import android.app.Application
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToKey
import androidx.lifecycle.SavedStateHandle
import androidx.paging.compose.collectAsLazyPagingItems
import com.anilist.fragment.ListActivityMediaListActivityItem
import com.google.common.truth.Truth.assertThat
import com.mxalbert.sharedelements.SharedElementsRoot
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.emptyImmutableList
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.persistentListOfNotNull
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.AppMediaPlayer
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityComposables
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeMediaDetailsActivityViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activity.activitiesSection
import com.thekeeperofpie.artistalleydatabase.anime.character.AnimeCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.forum.AnimeForumThreadsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumComposables
import com.thekeeperofpie.artistalleydatabase.anime.forum.forumThreadsSection
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadStatusController
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.AnimeMediaDetailsRecommendationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationComposables
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.recommendationsSection
import com.thekeeperofpie.artistalleydatabase.anime.review.AnimeMediaDetailsReviewsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewComposables
import com.thekeeperofpie.artistalleydatabase.anime.review.reviewsSection
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongComposables
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsProvider
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.AnimeStaffViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import java.util.Optional
import javax.inject.Inject

@OptIn(ExperimentalTestApi::class)
@ExtendWith(HiltInjectExtension::class)
@HiltAndroidTest
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock("UI", mode = ResourceAccessMode.READ_WRITE)
class MediaDetailsScreenTest {

    @JvmField
    @RegisterExtension
    @ExperimentalTestApi
    val composeExtension = createAndroidComposeExtension<TestActivity>()

    @Inject
    lateinit var ignoreController: IgnoreController

    @Inject
    lateinit var application: Application

    @Inject
    lateinit var aniListApi: AuthedAniListApi

    @Inject
    lateinit var cdEntryDao: CdEntryDao

    @Inject
    lateinit var appJson: AppJson

    @Inject
    lateinit var animeSongsProviderOptional: Optional<AnimeSongsProvider>

    @Inject
    lateinit var mediaPlayer: AppMediaPlayer

    @Inject
    lateinit var oAuthStore: AniListOAuthStore

    @Inject
    lateinit var mediaListStatusController: MediaListStatusController

    @Inject
    lateinit var recommendationStatusController: RecommendationStatusController

    @Inject
    lateinit var settings: AnimeSettings

    @Inject
    lateinit var favoritesController: FavoritesController

    @Inject
    lateinit var activityStatusController: ActivityStatusController

    @Inject
    lateinit var threadStatusController: ForumThreadStatusController

    @Inject
    lateinit var historyController: HistoryController

    @Inject
    lateinit var markwon: Markwon

    @Test
    fun loggedOut_showGlobalActivity() {
        val viewModels = mockViewModels()
        mockActivities(viewModels, followingId = null, globalId = 2)

        composeExtension.use {
            setContent { ScreenContent(viewModels) }

            val columnNode = onNodeWithTag("rootColumn").performScrollToKey("activitiesHeader")
            val headerIndex = columnNode.fetchSemanticsNode()
                .config[SemanticsProperties.IndexForKey]
                .invoke("activitiesHeader")
            val activityIndex = columnNode.fetchSemanticsNode()
                .config[SemanticsProperties.IndexForKey]
                .invoke("${R.string.anime_media_details_activities_label}-2")
            assertThat(activityIndex).isEqualTo(headerIndex + 1)
        }
    }

    @Test
    fun loggedIn_hasFollowing_showFollowingActivity() {
        val viewModels = mockViewModels()
        mockActivities(viewModels, followingId = 1, globalId = 2)
        whenever(viewModels.mediaDetailsViewModel.viewer) {
            MutableStateFlow(mock())
        }

        composeExtension.use {
            setContent { ScreenContent(viewModels) }

            val columnNode = onNodeWithTag("rootColumn").performScrollToKey("activitiesHeader")
            val headerIndex = columnNode.fetchSemanticsNode()
                .config[SemanticsProperties.IndexForKey]
                .invoke("activitiesHeader")
            val activityIndex = columnNode.fetchSemanticsNode()
                .config[SemanticsProperties.IndexForKey]
                .invoke("${R.string.anime_media_details_activities_label}-1")
            // This is +2 to skip the tabs that are added when logged in
            assertThat(activityIndex).isEqualTo(headerIndex + 2)
        }
    }

    @Test
    fun loggedIn_noFollowing_showGlobalActivity() {
        val viewModels = mockViewModels()
        mockActivities(viewModels, followingId = null, globalId = 2)
        whenever(viewModels.mediaDetailsViewModel.viewer) {
            MutableStateFlow(mock())
        }

        composeExtension.use {
            setContent { ScreenContent(viewModels) }

            val columnNode = onNodeWithTag("rootColumn").performScrollToKey("activitiesHeader")
            val headerIndex = columnNode.fetchSemanticsNode()
                .config[SemanticsProperties.IndexForKey]
                .invoke("activitiesHeader")
            val activityIndex = columnNode.fetchSemanticsNode()
                .config[SemanticsProperties.IndexForKey]
                .invoke("${R.string.anime_media_details_activities_label}-2")
            // This is +2 to skip the tabs that are added when logged in
            assertThat(activityIndex).isEqualTo(headerIndex + 2)
        }
    }

    private fun mockViewModels(): ViewModels {
        val savedStateHandle = SavedStateHandle().apply { set("mediaId", "1234") }
        val mediaDetailsViewModel = spy(
            AnimeMediaDetailsViewModel(
                application = application,
                aniListApi = aniListApi,
                cdEntryDao = cdEntryDao,
                appJson = appJson,
                oAuthStore = oAuthStore,
                mediaListStatusController = mediaListStatusController,
                ignoreController = ignoreController,
                settings = settings,
                favoritesController = favoritesController,
                historyController = historyController,
                markwon = markwon,
                savedStateHandle = savedStateHandle,
            )
        )
        val charactersViewModel = spy(
            AnimeCharactersViewModel(
                aniListApi = aniListApi,
                savedStateHandle = savedStateHandle,
            ).apply { initialize(mediaDetailsViewModel) }
        )
        val staffViewModel = spy(
            AnimeStaffViewModel(
                aniListApi = aniListApi,
                savedStateHandle = savedStateHandle,
            ).apply { initialize(mediaDetailsViewModel) }
        )
        val songsViewModel = spy(
            AnimeSongsViewModel(
                animeSongsProviderOptional = animeSongsProviderOptional,
                mediaPlayer = mediaPlayer,
            ).apply { initialize(mediaDetailsViewModel) }
        )
        val recommendationsViewModel = spy(
            AnimeMediaDetailsRecommendationsViewModel(
                aniListApi = aniListApi,
                mediaListStatusController = mediaListStatusController,
                recommendationStatusController = recommendationStatusController,
                ignoreController = ignoreController,
                settings = settings,
                savedStateHandle = savedStateHandle,
            ).apply { initialize(mediaDetailsViewModel) }
        )
        val activitiesViewModel = spy(
            AnimeMediaDetailsActivityViewModel(
                aniListApi = aniListApi,
                activityStatusController = activityStatusController,
            ).apply { initialize(mediaDetailsViewModel) }
        )
        val forumThreadsViewModel = spy(
            AnimeForumThreadsViewModel(
                aniListApi = aniListApi,
                threadStatusController = threadStatusController,
                savedStateHandle = savedStateHandle,
            ).apply { initialize(mediaDetailsViewModel) }
        )
        val reviewsViewModel = spy(
            AnimeMediaDetailsReviewsViewModel(
                aniListApi = aniListApi,
            ).apply { initialize(mediaDetailsViewModel) }
        )

        whenever(mediaDetailsViewModel.entry) {
            LoadingResult.success(
                AnimeMediaDetailsScreen.Entry(
                    mediaId = "1234",
                    media = mock(),
                    relations = emptyImmutableList(),
                    description = null,
                )
            )
        }

        return ViewModels(
            mediaDetailsViewModel = mediaDetailsViewModel,
            charactersViewModel = charactersViewModel,
            staffViewModel = staffViewModel,
            songsViewModel = songsViewModel,
            recommendationsViewModel = recommendationsViewModel,
            activitiesViewModel = activitiesViewModel,
            forumThreadsViewModel = forumThreadsViewModel,
            reviewsViewModel = reviewsViewModel,
        )
    }

    private fun mockActivities(
        viewModels: ViewModels,
        followingId: Int?,
        globalId: Int?,
    ) {
        whenever(viewModels.activitiesViewModel.activities) {
            AnimeMediaDetailsActivityViewModel.ActivitiesEntry(
                following = persistentListOfNotNull(
                    followingId?.let {
                        AnimeMediaDetailsActivityViewModel.ActivityEntry(
                            object : ListActivityMediaListActivityItem {
                                override val __typename = "Default"
                                override val createdAt = 0
                                override val id = it
                                override val isLiked = null
                                override val isSubscribed = null
                                override val likeCount = 0
                                override val progress = null
                                override val replyCount = 0
                                override val replies = null
                                override val status = null
                                override val type = null
                                override val user =
                                    object : ListActivityMediaListActivityItem.User {
                                        override val __typename = "Default"
                                        override val id = 10
                                        override val avatar = null
                                        override val name = "FollowerUser"
                                    }
                            }
                        )
                    }
                ),
                global = persistentListOfNotNull(
                    globalId?.let {
                        AnimeMediaDetailsActivityViewModel.ActivityEntry(
                            object : ListActivityMediaListActivityItem {
                                override val __typename = "Default"
                                override val createdAt = 0
                                override val id = it
                                override val isLiked = null
                                override val isSubscribed = null
                                override val likeCount = 0
                                override val progress = null
                                override val replyCount = 0
                                override val replies = null
                                override val status = null
                                override val type = null
                                override val user =
                                    object : ListActivityMediaListActivityItem.User {
                                        override val __typename = "Default"
                                        override val id = 10
                                        override val avatar = null
                                        override val name = "GlobalUser"
                                    }
                            }
                        )
                    }
                ),
            )
        }
    }

    @Composable
    private fun ScreenContent(
        viewModels: ViewModels,
    ) {
        SharedElementsRoot {
            CompositionLocalProvider(
                LocalIgnoreController.provides(ignoreController),
            ) {
                val charactersDeferred =
                    viewModels.charactersViewModel.charactersDeferred.collectAsLazyPagingItems()
                val staff = viewModels.staffViewModel.staff.collectAsLazyPagingItems()
                val viewer by viewModels.mediaDetailsViewModel.viewer.collectAsState()
                val activities = viewModels.activitiesViewModel.activities
                val (activityTab, onActivityTabChange) = rememberSaveable(viewer, activities) {
                    mutableStateOf(
                        if (activities?.following.isNullOrEmpty()) {
                            AnimeMediaDetailsActivityViewModel.ActivityTab.GLOBAL
                        } else {
                            AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING
                        }
                    )
                }
                AnimeMediaDetailsScreen(
                    viewModel = viewModels.mediaDetailsViewModel,
                    upIconOption = UpIconOption.Back {},
                    headerValues = MediaHeaderValues(
                        Bundle.EMPTY,
                        media = { mock() },
                        favoriteUpdate = { false },
                    ),
                    charactersCount = {
                        charactersDeferred.itemCount
                            .coerceAtLeast(viewModels.charactersViewModel.charactersInitial.size)
                    },
                    charactersSection = { screenKey, entry, coverImageWidthToHeightRatio ->
                        charactersSection(
                            screenKey = screenKey,
                            titleRes = R.string.anime_media_details_characters_label,
                            charactersInitial = viewModels.charactersViewModel.charactersInitial,
                            charactersDeferred = { charactersDeferred },
                            mediaId = entry.mediaId,
                            media = entry.media,
                            mediaFavorite = viewModels.mediaDetailsViewModel.favoritesToggleHelper.favorite,
                            mediaCoverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
                            viewAllContentDescriptionTextRes = R.string.anime_media_details_view_all_content_description,
                        )
                    },
                    staffCount = { staff.itemCount },
                    staffSection = {
                        staffSection(
                            screenKey = it,
                            titleRes = R.string.anime_media_details_staff_label,
                            staffList = staff,
                        )
                    },
                    recommendationsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                        items = viewModels.recommendationsViewModel.recommendations?.recommendations,
                        aboveFold = RecommendationComposables.RECOMMENDATIONS_ABOVE_FOLD,
                        hasMore = viewModels.recommendationsViewModel.recommendations?.hasMore
                            ?: true,
                    ),
                    recommendationsSection = { screenKey, expanded, onExpandedChange, onClickListEdit, coverImageWidthToHeightRatio ->
                        val entry = viewModels.recommendationsViewModel.recommendations
                        recommendationsSection(
                            screenKey = screenKey,
                            viewer = viewer,
                            entry = entry,
                            expanded = expanded,
                            onExpandedChange = onExpandedChange,
                            onClickListEdit = onClickListEdit,
                            onClickViewAll = {
                                it.onMediaRecommendationsClick(
                                    viewModels.mediaDetailsViewModel.mediaId,
                                    viewModels.mediaDetailsViewModel.entry.result?.media,
                                    viewModels.mediaDetailsViewModel.favoritesToggleHelper.favorite,
                                    coverImageWidthToHeightRatio(),
                                )
                            },
                            onUserRecommendationRating = viewModels.recommendationsViewModel.recommendationToggleHelper::toggle,
                        )
                    },
                    songSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                        items = viewModels.songsViewModel.animeSongs?.entries.orEmpty(),
                        aboveFold = AnimeSongComposables.SONGS_ABOVE_FOLD,
                        hasMore = false,
                    ),
                    songsSection = { screenKey, expanded, onExpandedChange ->
                        AnimeSongComposables.songsSection(
                            screenKey = screenKey,
                            viewModel = viewModels.songsViewModel,
                            songsExpanded = expanded,
                            onSongsExpandedChange = onExpandedChange,
                        )
                    },
                    activitiesSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                        items = if (activityTab == AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING) {
                            activities?.following
                        } else {
                            activities?.global
                        },
                        aboveFold = AnimeActivityComposables.ACTIVITIES_ABOVE_FOLD,
                        hasMore = true,
                        addOneForViewer = true,
                    ),
                    activitiesSection = { screenKey, expanded, onExpandedChanged, onClickListEdit, coverImageWidthToHeightRatio ->
                        activitiesSection(
                            screenKey = screenKey,
                            viewer = viewer,
                            onActivityStatusUpdate = viewModels.activitiesViewModel.activityToggleHelper::toggle,
                            activityTab = activityTab,
                            activities = if (activityTab == AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING) {
                                activities?.following
                            } else {
                                activities?.global
                            },
                            onActivityTabChange = onActivityTabChange,
                            expanded = expanded,
                            onExpandedChange = onExpandedChanged,
                            onClickListEdit = onClickListEdit,
                            onClickViewAll = {
                                val entry = viewModels.mediaDetailsViewModel.entry.result
                                if (entry != null) {
                                    it.onMediaActivitiesClick(
                                        entry,
                                        activityTab == AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING,
                                        viewModels.mediaDetailsViewModel.favoritesToggleHelper.favorite,
                                        coverImageWidthToHeightRatio(),
                                    )
                                }
                            },
                        )
                    },
                    forumThreadsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                        items = viewModels.forumThreadsViewModel.forumThreads,
                        aboveFold = ForumComposables.FORUM_THREADS_ABOVE_FOLD,
                        hasMore = true,
                    ),
                    forumThreadsSection = { expanded, onExpandedChanged ->
                        forumThreadsSection(
                            viewer = viewer,
                            forumThreads = viewModels.forumThreadsViewModel.forumThreads,
                            expanded = expanded,
                            onExpandedChange = onExpandedChanged,
                            onClickViewAll = {
                                val entry = viewModels.mediaDetailsViewModel.entry.result
                                it.onForumMediaCategoryClick(
                                    entry?.media?.title?.userPreferred,
                                    viewModels.mediaDetailsViewModel.mediaId
                                )
                            },
                            onStatusUpdate = viewModels.forumThreadsViewModel.threadToggleHelper::toggle,
                        )
                    },
                    reviewsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                        items = viewModels.reviewsViewModel.reviews?.reviews,
                        aboveFold = ReviewComposables.REVIEWS_ABOVE_FOLD,
                        hasMore = viewModels.reviewsViewModel.reviews?.hasMore ?: false,
                    ),
                    reviewsSection = { screenKey, expanded, onExpandedChange, coverImageWidthToHeightRatio ->
                        reviewsSection(
                            screenKey = screenKey,
                            entry = viewModels.reviewsViewModel.reviews,
                            expanded = expanded,
                            onExpandedChange = onExpandedChange,
                            onClickViewAll = {
                                it.onMediaReviewsClick(
                                    viewModels.mediaDetailsViewModel.mediaId,
                                    viewModels.mediaDetailsViewModel.entry.result?.media,
                                    viewModels.mediaDetailsViewModel.favoritesToggleHelper.favorite,
                                    coverImageWidthToHeightRatio(),
                                )
                            },
                            onReviewClick = { navigationCallback, item ->
                                navigationCallback.onReviewClick(
                                    reviewId = item.id.toString(),
                                    media = viewModels.mediaDetailsViewModel.entry.result?.media,
                                    favorite = viewModels.mediaDetailsViewModel.favoritesToggleHelper.favorite,
                                    imageWidthToHeightRatio = coverImageWidthToHeightRatio()
                                )
                            },
                        )
                    },
                )
            }
        }
    }

    data class ViewModels(
        val mediaDetailsViewModel: AnimeMediaDetailsViewModel,
        val charactersViewModel: AnimeCharactersViewModel,
        val staffViewModel: AnimeStaffViewModel,
        val songsViewModel: AnimeSongsViewModel,
        val recommendationsViewModel: AnimeMediaDetailsRecommendationsViewModel,
        val activitiesViewModel: AnimeMediaDetailsActivityViewModel,
        val forumThreadsViewModel: AnimeForumThreadsViewModel,
        val reviewsViewModel: AnimeMediaDetailsReviewsViewModel,
    )
}
