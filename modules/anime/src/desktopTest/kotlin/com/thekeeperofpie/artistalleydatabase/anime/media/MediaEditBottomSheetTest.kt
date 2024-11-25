package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeDown
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.AnimeMediaEditBottomSheet
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditState
import com.thekeeperofpie.artistalleydatabase.test_utils.ComposeTestRoot
import kotlin.test.Ignore
import kotlin.test.Test

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
class MediaEditBottomSheetTest {

    @Test
    fun swipeDownDismiss() = runComposeUiTest {
        val state = makeState()
        setContent { BottomSheet(state) }
        state.showing = true
        mainClock.advanceTimeByFrame()

        onNodeWithText("Test media title").assertIsDisplayed()
        onNodeWithContentDescription("Drag handle").performTouchInput { swipeDown() }
        onNodeWithText("Test media title").assertIsNotDisplayed()
    }

    @Test
    fun toggleStatus() = runComposeUiTest {
        val state = makeState()
        setContent { BottomSheet(state) }
        state.showing = true
        mainClock.advanceTimeByFrame()

        onNodeWithText("Not on list").performClick()
        onAllNodes(isRoot())[1].onChildAt(0).onChildAt(0).onChildAt(0)
            .onChildren()
            .filterToOne(hasText("Completed"))
            .performClick()

        onNodeWithText("Completed").assertIsDisplayed()
        onNodeWithText("Exit").assertDoesNotExist()
    }

    @Ignore("Broken pending bottom sheet fixes")
    @Test
    fun unsavedChange() = runComposeUiTest {
        val state = makeState()
        var changed by mutableStateOf(false)
        setContent {
            BottomSheet(
                state = state,
                onAttemptDismiss = {
                    if (changed) {
                        state.showConfirmClose = true
                    }
                    !changed
                },
                eventSink = {
                    if (it is AnimeMediaEditBottomSheet.Event.StatusChange
                        && it.status == MediaListStatus.COMPLETED
                    ) {
                        changed = true
                    }
                },
            )
        }
        state.showing = true
        mainClock.advanceTimeByFrame()

        onNodeWithText("Not on list").performClick()
        onNodeWithText("Completed").performClick()

        onNodeWithText("Completed").assertIsDisplayed()
        onNodeWithContentDescription("Drag handle").performTouchInput { swipeDown() }
        onNodeWithText("Exit").assertIsDisplayed()
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

    @Composable
    private fun BottomSheet(
        state: MediaEditState,
        onAttemptDismiss: () -> Boolean = { true },
        eventSink: (AnimeMediaEditBottomSheet.Event) -> Unit = {},
    ) {
        ComposeTestRoot {
            MediaEditBottomSheetScaffold(
                onEditSheetValueChange = { true },
                onAttemptDismiss = onAttemptDismiss,
                state = { state },
                eventSink = eventSink,
            ) {
                Box(Modifier.fillMaxSize().padding(it))
            }
        }
    }
}
