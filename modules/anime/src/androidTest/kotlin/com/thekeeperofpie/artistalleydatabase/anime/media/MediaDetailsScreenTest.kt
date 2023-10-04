package com.thekeeperofpie.artistalleydatabase.anime.media

import android.app.Application
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToKey
import androidx.lifecycle.SavedStateHandle
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
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadStatusController
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsProvider
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
        val viewModel = mockViewModel()
        mockActivities(viewModel, followingId = null, globalId = 2)

        composeExtension.use {
            setContent { ScreenContent(viewModel) }

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
        val viewModel = mockViewModel()
        mockActivities(viewModel, followingId = 1, globalId = 2)
        whenever(viewModel.viewer) {
            MutableStateFlow(mock())
        }

        composeExtension.use {
            setContent { ScreenContent(viewModel) }

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
        val viewModel = mockViewModel()
        mockActivities(viewModel, followingId = null, globalId = 2)
        whenever(viewModel.viewer) {
            MutableStateFlow(mock())
        }

        composeExtension.use {
            setContent { ScreenContent(viewModel) }

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

    private fun mockViewModel(): AnimeMediaDetailsViewModel {
        val viewModel = spy(
            AnimeMediaDetailsViewModel(
                application = application,
                aniListApi = aniListApi,
                cdEntryDao = cdEntryDao,
                appJson = appJson,
                animeSongsProviderOptional = animeSongsProviderOptional,
                mediaPlayer = mediaPlayer,
                oAuthStore = oAuthStore,
                mediaListStatusController = mediaListStatusController,
                recommendationStatusController = recommendationStatusController,
                ignoreController = ignoreController,
                settings = settings,
                favoritesController = favoritesController,
                activityStatusController = activityStatusController,
                threadStatusController = threadStatusController,
                historyController = historyController,
                markwon = markwon,
                savedStateHandle = SavedStateHandle().apply {
                    set("mediaId", "1234")
                },
            )
        )

        whenever(viewModel.entry) {
            LoadingResult.success(
                AnimeMediaDetailsScreen.Entry(
                    mediaId = "1234",
                    media = mock(),
                    relations = emptyImmutableList(),
                    description = null,
                )
            )
        }

        return viewModel
    }

    private fun mockActivities(
        viewModel: AnimeMediaDetailsViewModel,
        followingId: Int?,
        globalId: Int?,
    ) {
        whenever(viewModel.activities) {
            AnimeMediaDetailsViewModel.ActivitiesEntry(
                following = persistentListOfNotNull(
                    followingId?.let {
                        AnimeMediaDetailsViewModel.ActivityEntry(
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
                        AnimeMediaDetailsViewModel.ActivityEntry(
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
        viewModel: AnimeMediaDetailsViewModel,
    ) {
        SharedElementsRoot {
            CompositionLocalProvider(
                LocalIgnoreController.provides(ignoreController),
            ) {
                AnimeMediaDetailsScreen(
                    viewModel = viewModel,
                    upIconOption = UpIconOption.Back {},
                    headerValues = MediaHeaderValues(
                        Bundle.EMPTY,
                        media = { mock() },
                        favoriteUpdate = { false },
                    )
                )
            }
        }
    }
}
