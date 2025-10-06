package com.thekeeperofpie.artistalleydatabase.entry

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEditable
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.compose.ui.text.TextRange
import app.cash.burst.Burst
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("JUnitMalformedDeclaration")
@OptIn(ExperimentalTestApi::class)
@Burst
class EntryFormMultiTextTest {

    @Test
    fun autocomplete() = runComposeUiTest {
        val state = EntryForm2.PendingTextState()
        val contents = SnapshotStateList<EntryForm2.MultiTextState.Entry>()
        setContent { Content(state, contents) }
        onNode(hasSetTextAction()).performTextInput("inputOn")
        onNodeWithText("predictionOne").assertDoesNotExist()
        onNode(hasSetTextAction()).performTextInput("e")

        waitUntilExactlyOneExists(hasText("predictionOne"), timeoutMillis = 2000)

        onNodeWithText("inputOne").assertIsDisplayed()
        onNodeWithText("predictionOne").assertIsDisplayed()
        onNodeWithText("predictionTwo").assertIsDisplayed()
        onNodeWithText("predictionOne").performClick()

        onNodeWithText("predictionOne").assertIsDisplayed()
        onNodeWithText("predictionTwo").assertDoesNotExist()
        onNodeWithText("inputOne").assertDoesNotExist()
    }

    @Test
    fun moveUp(entryType: EntryType = EntryType.CUSTOM) = runComposeUiTest {
        setUpAndAssertTwoExistingItems(entryType)

        onAllNodesWithContentDescription("More actions").onLast().performClick()
        onNodeWithText("Move up").performClick()

        onAllNodesWithText("item", substring = true).run {
            onFirst().assert(hasText("itemTwo"))
            onLast().assert(hasText("itemOne"))
        }
    }

    @Test
    fun moveDown(entryType: EntryType = EntryType.CUSTOM) = runComposeUiTest {
        setUpAndAssertTwoExistingItems(entryType)

        onAllNodesWithContentDescription("More actions").onFirst().performClick()
        onNodeWithText("Move down").performClick()

        onAllNodesWithText("item", substring = true).run {
            onFirst().assert(hasText("itemTwo"))
            onLast().assert(hasText("itemOne"))
        }
    }

    @Test
    fun delete(entryType: EntryType = EntryType.CUSTOM) = runComposeUiTest {
        setUpAndAssertTwoExistingItems(entryType)

        onAllNodesWithContentDescription("More actions").onFirst().performClick()
        onNodeWithText("Delete").performClick()

        onNodeWithText("item", substring = true).assertIsDisplayed()
        onNodeWithText("itemOne").assertDoesNotExist()
    }

    @Test
    fun backspaceMovesUpwards(entryType: EntryType = EntryType.CUSTOM) = runComposeUiTest {
        setUpAndAssertTwoExistingItems(entryType)

        onAllNodesWithText("item", substring = true).run {
            onFirst().assert(hasText("itemOne"))
            onLast().assert(hasText("itemTwo"))
        }

        onAllNodes(hasSetTextAction()).onLast().run {
            performTextInput("input")
            performKeyInput { repeat(5) { pressKey(Key.Backspace) } }
            assert(hasText(""))
            performKeyInput { pressKey(Key.Backspace) }
            assert(hasText("itemTwo"))
        }
    }

    @Test
    fun keyboardActionMovesDownwards(
        entryType: EntryType = EntryType.CUSTOM,
        imeNextAction: ImeNextAction,
    ) = runComposeUiTest {
        setUpAndAssertTwoExistingItems(entryType)

        onAllNodes(hasSetTextAction()).onLast().run {
            performTextInput("itemThree")
            when (imeNextAction) {
                ImeNextAction.NEXT -> performImeAction()
                ImeNextAction.TAB -> performKeyInput { pressKey(Key.Tab) }
                ImeNextAction.ENTER -> performKeyInput { pressKey(Key.Enter) }
            }
            assert(hasText(""))
            performTextInput("itemFour")
        }

        onAllNodesWithText("", substring = true).run {
            get(0).assert(hasText("Header"))
            get(1).assert(hasText("itemOne"))
            get(2).assert(hasText("itemTwo"))
            get(3).assert(hasText("itemThree"))
            get(4).assert(hasText("itemFour"))
            assertTrue(fetchSemanticsNodes().size == 5)
        }
    }

    @Test
    fun clear(entryType: EntryType = EntryType.CUSTOM) = runComposeUiTest {
        val (_, contents) = setUpAndAssertTwoExistingItems(entryType)
        contents.clear()

        onNodeWithText("itemOne").assertDoesNotExist()
        onNodeWithText("itemTwo").assertDoesNotExist()

        onAllNodesWithText(text = "", substring = true).run {
            get(0).assert(hasText("Header"))
            get(1).assert(hasText(""))
            assertTrue(fetchSemanticsNodes().size == 2)
        }
    }

    @Test
    fun lockedHasNoEditableText(entryType: EntryType = EntryType.CUSTOM) = runComposeUiTest {
        setUpAndAssertTwoExistingItems(entryType)
        assertTrue(onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty())

        onNodeWithText("Header").performClick()

        onNode(hasSetTextAction()).assertDoesNotExist()
    }

    @Test
    fun lockedHasOnlyUnlockAction(entryType: EntryType = EntryType.CUSTOM) = runComposeUiTest {
        setUpAndAssertTwoExistingItems(entryType)

        onNodeWithText("Header").performClick()

        onNode(
            hasClickAction() and isDisplayed() and
                    hasContentDescription("Open more").not() and
                    (SemanticsMatcher.keyIsDefined(SemanticsActions.SetSelection).not() or
                            isEditable())
        ).assert(hasText("Header"))
    }

    @Test
    fun existingCustomEntriesAreEditable() = runComposeUiTest {
        setUpAndAssertTwoExistingItems(EntryType.CUSTOM)
        onAllNodes(hasSetTextAction()).run {
            get(0).performTextInput("prefix")
            get(1).performTextInputSelection(TextRange("itemTwo".length))
            get(1).performTextInput("Suffix")
        }

        onAllNodesWithText("", substring = true).run {
            get(0).assert(hasText("Header"))
            get(1).assert(hasText("prefixitemOne"))
            get(2).assert(hasText("itemTwoSuffix"))
            get(3).assert(hasText(""))
            assertTrue(fetchSemanticsNodes().size == 4)
        }
    }

    @Test
    fun openInNewNavigation(entryType: EntryType = EntryType.CUSTOM) = runComposeUiTest {
        val generateEntry: (String) -> EntryForm2.MultiTextState.Entry = {
            when (entryType) {
                EntryType.CUSTOM -> EntryForm2.MultiTextState.Entry.Custom(it)
                EntryType.PREFILLED -> testPrefilled(it)
            }
        }

        val navigationEvents = mutableListOf<EntryForm2.MultiTextState.Entry>()
        setUpAndAssertTwoExistingItems(entryType, onNavigate = { navigationEvents += it })

        // Test while this is locked, since that's the majority use case
        onNodeWithText("Header").performClick()

        onAllNodesWithContentDescription("Open more").onFirst().performClick()
        assertEquals(navigationEvents, listOf(generateEntry("itemOne")))

        onAllNodesWithContentDescription("Open more").onLast().performClick()
        assertEquals(navigationEvents, listOf(generateEntry("itemOne"), generateEntry("itemTwo")))
    }

    @Test
    fun lockSavesPendingEntry(entryType: EntryType = EntryType.CUSTOM) = runComposeUiTest {
        setUpAndAssertTwoExistingItems(entryType)

        onAllNodes(hasSetTextAction()).onLast().performTextInput("itemThree")

        onNodeWithText("Header").performClick() // Lock
        onNodeWithText("Header").performClick() // Unlock

        onAllNodes(hasSetTextAction()).onLast().run {
            assert(hasText(""))
            performTextInput("itemFour")
        }

        onAllNodesWithText("", substring = true).run {
            get(0).assert(hasText("Header"))
            get(1).assert(hasText("itemOne"))
            get(2).assert(hasText("itemTwo"))
            get(3).assert(hasText("itemThree"))
            get(4).assert(hasText("itemFour"))
            assertTrue(fetchSemanticsNodes().size == 5)
        }
    }

    @Composable
    private fun Content(
        state: EntryForm2.PendingTextState,
        contents: SnapshotStateList<EntryForm2.MultiTextState.Entry>,
        onNavigate: (EntryForm2.MultiTextState.Entry) -> Unit = {},
    ) {
        EntryForm2 {
            MultiTextSection(
                state = state,
                headerText = { Text("Header") },
                focusRequester = remember { FocusRequester() },
                onFocusChanged = {},
                trailingIcon = { null },
                entryPredictions = ::predictions,
                onNavigate = onNavigate,
                items = contents,
                onItemCommitted = { contents += it },
                removeLastItem = { contents.removeLastOrNull()?.text },
            )
        }
    }

    private fun ComposeUiTest.setUpAndAssertTwoExistingItems(
        entryType: EntryType,
        onNavigate: (EntryForm2.MultiTextState.Entry) -> Unit = {},
    ): Pair<EntryForm2.PendingTextState, SnapshotStateList<EntryForm2.MultiTextState.Entry>> {
        val generateEntry: (String) -> EntryForm2.MultiTextState.Entry = {
            when (entryType) {
                EntryType.CUSTOM -> EntryForm2.MultiTextState.Entry.Custom(it)
                EntryType.PREFILLED -> testPrefilled(it)
            }
        }
        val state = EntryForm2.PendingTextState()
        val contents =
            mutableListOf(generateEntry("itemOne"), generateEntry("itemTwo")).toMutableStateList()
        setContent { Content(state = state, contents = contents, onNavigate = onNavigate) }

        onAllNodesWithText("item", substring = true).run {
            onFirst().assert(hasText("itemOne"))
            onLast().assert(hasText("itemTwo"))
        }
        return state to contents
    }

    private fun predictions(input: String) = when (input) {
        "inputOne" -> listOf("predictionOne", "predictionTwo")
            .map(::testPrefilled)
            .let { flowOf(value = it) }
        else -> emptyFlow()
    }

    private fun testPrefilled(text: String) = EntryForm2.MultiTextState.Entry.Prefilled(
        value = text,
        id = text,
        text = text,
        serializedValue = text,
        searchableValue = text,
    )

    enum class EntryType { CUSTOM, PREFILLED, }

    enum class ImeNextAction { NEXT, TAB, ENTER }
}
