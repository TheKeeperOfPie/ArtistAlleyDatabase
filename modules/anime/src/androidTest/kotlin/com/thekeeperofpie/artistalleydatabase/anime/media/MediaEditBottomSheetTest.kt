package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.test_utils.HiltInjectExtension
import com.thekeeperofpie.artistalleydatabase.test_utils.TestActivity
import dagger.hilt.android.testing.HiltAndroidTest
import de.mannodermaus.junit5.compose.createAndroidComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock

@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class
)
@ExtendWith(HiltInjectExtension::class)
@HiltAndroidTest
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock("UI", mode = ResourceAccessMode.READ_WRITE)
class MediaEditBottomSheetTest {

    @JvmField
    @RegisterExtension
    @ExperimentalTestApi
    val composeExtension = createAndroidComposeExtension<TestActivity>()

    @Test
    fun swipeDownDismiss() {
        composeExtension.use {
            var viewModel: MediaEditViewModel? = null
            setContent {
                Content()
                viewModel = hiltViewModel<MediaEditViewModel>()
            }

            // TODO: This needs to be run after initial composition, which isn't correct
            // TODO: Test request from sparse params
            viewModel?.initialize(
                mediaId = "123",
                coverImage = null,
                title = "Test media title",
                mediaListEntry = null,
                mediaType = MediaType.ANIME,
                status = null,
                maxProgress = 10,
                maxProgressVolumes = null,
                loading = false,
            )
            viewModel?.editData?.showing = true

            onNodeWithText("Test media title").assertIsDisplayed()
            onNodeWithTag("bottomSheetDragHandle").performTouchInput { swipeDown() }
            onNodeWithText("Test media title").assertDoesNotExist()
        }
    }

    @Test
    fun toggleStatus() {
        composeExtension.use {
            var viewModel: MediaEditViewModel? = null
            setContent {
                Content()
                viewModel = hiltViewModel<MediaEditViewModel>()
            }

            // TODO: This needs to be run after initial composition, which isn't correct
            // TODO: Test request from sparse params
            viewModel?.initialize(
                mediaId = "123",
                coverImage = null,
                title = "Test media title",
                mediaListEntry = null,
                mediaType = MediaType.ANIME,
                status = null,
                maxProgress = 10,
                maxProgressVolumes = null,
                loading = false,
            )
            viewModel?.editData?.showing = true

            onNodeWithText("Not on list").performClick()
            onNodeWithText("Completed").performClick()

            onNodeWithText("Completed").assertIsDisplayed()
            onNodeWithText("Exit").assertDoesNotExist()
        }
    }

    @Test
    fun unsavedChange() {
        composeExtension.use {
            var viewModel: MediaEditViewModel? = null
            setContent {
                Content()
                viewModel = hiltViewModel<MediaEditViewModel>()
            }

            // TODO: This needs to be run after initial composition, which isn't correct
            // TODO: Test request from sparse params
            viewModel?.initialize(
                mediaId = "123",
                coverImage = null,
                title = "Test media title",
                mediaListEntry = null,
                mediaType = MediaType.ANIME,
                status = null,
                maxProgress = 10,
                maxProgressVolumes = null,
                loading = false,
            )
            viewModel?.editData?.showing = true

            onNodeWithText("Not on list").performClick()
            onNodeWithText("Completed").performClick()

            onNodeWithText("Completed").assertIsDisplayed()
            onNodeWithTag("bottomSheetDragHandle").performTouchInput { swipeDown() }
            onNodeWithText("Exit").assertIsDisplayed()
        }
    }

    @Composable
    private fun Content() {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                MediaEditBottomSheetScaffold(
                    screenKey = "test",
                    topBar = { TopAppBar(title = { Text(text = "Top bar title") }) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .background(Color.Blue)
                    )
                }
            }
        }
    }
}
