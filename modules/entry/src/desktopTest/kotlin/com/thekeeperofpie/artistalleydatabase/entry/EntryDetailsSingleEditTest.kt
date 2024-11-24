package com.thekeeperofpie.artistalleydatabase.entry

import androidx.compose.runtime.snapshotFlow
import app.cash.turbine.turbineScope
import com.thekeeperofpie.artistalleydatabase.test_utils.await
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
class EntryDetailsSingleEditTest : EntryDetailsTestBase() {

    @Test
    fun clickSave_success() = runTest {
        turbineScope {
            val entryId = Uuid.random().toString()
            val entry = TestEntry(entryId, "testData")
            val viewModel = testViewModel(
                entryIds = listOf(EntryId("test", entry.id)),
                entries = arrayOf(entry),
            )

            snapshotFlow { viewModel.sectionsLoading }.await { !it }

            val navigateUpEvents = viewModel.navigateUpEvents.testIn(backgroundScope)
            val entries = viewModel.entries.filter { it.isNotEmpty() }.testIn(backgroundScope)
            viewModel.editModel("newData")
            viewModel.onClickSave()
            advanceUntilIdle()

            assertModel(entries.await { it[entryId]?.data == "newData" }.values.single(), "newData")
            navigateUpEvents.awaitItem()
        }
    }

    @Test
    fun clickSave_error() = runTest {
        turbineScope {
            val entry = TestEntry(Uuid.random().toString(), "testData")
            val viewModel = testViewModel(
                hasError = true,
                entryIds = listOf(EntryId("test", entry.id)),
                entries = arrayOf(entry),
            )

            snapshotFlow { viewModel.sectionsLoading }.await { !it }

            val navigateUpEvents = viewModel.navigateUpEvents.testIn(backgroundScope)
            val entries = viewModel.entries.filter { it.isNotEmpty() }.testIn(backgroundScope)
            viewModel.editModel("newData")
            viewModel.onClickSave()
            advanceUntilIdle()

            assertModel(entries.awaitItem().values.single(), "testData")
            navigateUpEvents.expectNoEvents()
        }
    }
}
