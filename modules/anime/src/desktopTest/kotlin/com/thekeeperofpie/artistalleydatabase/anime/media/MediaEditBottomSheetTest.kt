package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeDown
import com.anilist.data.type.MediaType
import com.google.common.truth.Truth.assertThat
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.AnimeMediaEditBottomSheet
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditState
import com.thekeeperofpie.artistalleydatabase.test_utils.ComposeTestRoot
import kotlin.test.Test

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
class MediaEditBottomSheetTest {

    @Test
    fun swipeDownDismiss() = runComposeUiTest {
        val state = makeState()
        setContent { BottomSheet(state) }
        state.showing = true

        onSheet(hasText("Test media title")).assertIsDisplayed()
        onSheet(hasContentDescription("Drag handle")).performTouchInput { swipeDown() }
        onSheet(hasText("Test media title")).assertDoesNotExist()
    }

    @Test
    fun toggleStatus() = runComposeUiTest {
        val state = makeState()
        setContent { BottomSheet(state) }
        state.showing = true

        onSheet(hasText("Not on list")).performClick()
        onSheet(hasText("Completed")).performClick()

        onSheet(hasText("Completed")).assertIsDisplayed()
        onSheet(hasText("Exit")).assertDoesNotExist()
    }

    @Test
    fun exitDialog_unsavedChange() = runComposeUiTest {
        val state = makeState()
        setContent { BottomSheet(state = state) }
        state.showing = true

        onSheet(hasText("Not on list")).performClick()
        onSheet(hasText("Completed")).performClick()

        onSheet(hasText("Completed")).assertIsDisplayed()
        onSheet(hasContentDescription("Drag handle")).performTouchInput { swipeDown() }
        onExitDialog(hasText("Exit")).performClick()

        onSheet(hasText("Completed")).assertDoesNotExist()
    }

    @Test
    fun exitDialog_saveChange() = runComposeUiTest {
        val state = makeState()
        var saved = false
        setContent {
            BottomSheet(
                state = state,
                eventSink = {
                    println("event = $it")
                    if (it is AnimeMediaEditBottomSheet.Event.Save) {
                        state.saving = true
                        state.showConfirmClose = false
                        state.showing = false
                        saved = true
                    }
                },
            )
        }
        state.showing = true
        mainClock.advanceTimeByFrame()

        onSheet(hasText("Not on list")).performClick()
        onSheet(hasText("Completed")).performClick()

        onSheet(hasText("Completed")).assertIsDisplayed()
        onSheet(hasContentDescription("Drag handle")).performTouchInput { swipeDown() }
        onExitDialog(hasText("Save")).performClick()

        assertThat(saved).isTrue()
    }

    private fun makeState() = MediaEditState().apply {
        this.initialParams = MediaEditState.InitialParams(
            id = "123",
            mediaId = "123",
            coverImage = null,
            title = "Test media title",
            status = null,
            score = null,
            progress = null,
            progressVolumes = null,
            repeat = null,
            priority = null,
            private = null,
            hiddenFromStatusLists = null,
            notes = null,
            startedAtYear = null,
            startedAtMonth = null,
            startedAtDay = null,
            completedAtYear = null,
            completedAtMonth = null,
            completedAtDay = null,
            updatedAt = null,
            createdAt = null,
            mediaType = MediaType.ANIME,
            maxProgress = 10,
            maxProgressVolumes = null,
            loading = false,
        )
    }

    private fun ComposeUiTest.onSheet(matcher: SemanticsMatcher) =
        onAllNodes(matcher = matcher).onFirst()

    private fun ComposeUiTest.onExitDialog(matcher: SemanticsMatcher) =
        onAllNodes(matcher = matcher).onLast()

    @Composable
    private fun BottomSheet(
        state: MediaEditState,
        eventSink: (AnimeMediaEditBottomSheet.Event) -> Unit = {},
    ) {
        ComposeTestRoot {
            MediaEditBottomSheetScaffold(
                state = { state },
                eventSink = eventSink,
            ) {
                Box(Modifier.fillMaxSize().padding(it))
            }
        }
    }
}
