package com.thekeeperofpie.artistalleydatabase.entry

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EntryFormLongTextTest {

    @Test
    fun edit() = runComposeUiTest {
        setUpExistingState()
        onNode(hasSetTextAction()).run {
            performTextClearance()
            performTextInput("newText")
        }

        onNodeWithText("newText").assertIsDisplayed()
    }

    @Test
    fun lockedNotEditable() = runComposeUiTest {
        setUpExistingState()
        assertTrue(onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty())

        onNodeWithText("Header").performClick()
        assertTrue(onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isEmpty())
    }

    private fun ComposeUiTest.setUpExistingState(): EntryForm2.SingleTextState {
        val state = EntryForm2.SingleTextState(
            value = TextFieldState("Long text input"),
            initialLockState = EntryLockState.UNLOCKED,
        )
        setContent { Content(state) }
        onNodeWithText("Long text input").assertIsDisplayed()
        return state
    }

    @Composable
    private fun Content(state: EntryForm2.SingleTextState) {
        EntryForm2(focusState = EntryForm2.rememberFocusState(listOf(state))) {
            LongTextSection(
                state = state,
                headerText = { Text("Header") },
            )
        }
    }
}
