package com.thekeeperofpie.artistalleydatabase.anime.media

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
import com.anilist.fragment.ListActivityMediaListActivityItem
import com.google.common.truth.Truth.assertThat
import com.mxalbert.sharedelements.SharedElementsRoot
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.emptyImmutableList
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.persistentListOfNotNull
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityComposables
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeMediaDetailsActivityViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activity.activitiesSection
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
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
    lateinit var aniListApi: AuthedAniListApi

    @Inject
    lateinit var oAuthStore: AniListOAuthStore

    @Inject
    lateinit var mediaListStatusController: MediaListStatusController

    @Inject
    lateinit var settings: AnimeSettings

    @Inject
    lateinit var favoritesController: FavoritesController

    @Inject
    lateinit var activityStatusController: ActivityStatusController

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
                aniListApi = aniListApi,
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
        val activitiesViewModel = spy(
            AnimeMediaDetailsActivityViewModel(
                aniListApi = aniListApi,
                activityStatusController = activityStatusController,
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
            activitiesViewModel = activitiesViewModel,
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
                    charactersCount = { 0 },
                    charactersSection = { _, _, _ -> },
                    staffCount = { 0 },
                    staffSection = {},
                    recommendationsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.Empty,
                    recommendationsSection = { _, _, _, _, _ -> },
                    songsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.Empty,
                    songsSection = { _, _, _ -> },
                    cdsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.Empty,
                    cdsSection = {},
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
                    forumThreadsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.Empty,
                    forumThreadsSection = { _, _ -> },
                    reviewsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.Empty,
                    reviewsSection = { screenKey, expanded, onExpandedChange, coverImageWidthToHeightRatio -> },
                )
            }
        }
    }

    data class ViewModels(
        val mediaDetailsViewModel: AnimeMediaDetailsViewModel,
        val activitiesViewModel: AnimeMediaDetailsActivityViewModel,
    )
}
