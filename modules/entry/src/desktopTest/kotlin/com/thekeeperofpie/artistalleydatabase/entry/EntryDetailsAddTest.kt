package com.thekeeperofpie.artistalleydatabase.entry

import androidx.compose.runtime.snapshotFlow
import app.cash.turbine.turbineScope
import com.google.common.truth.Truth.assertThat
import com.thekeeperofpie.artistalleydatabase.test_utils.await
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EntryDetailsAddTest : EntryDetailsTestBase() {

    @Test
    fun addEmpty_clickSave_success() = runTest {
        turbineScope {
            val viewModel = testViewModel()

            snapshotFlow { viewModel.sectionsLoading }.await { !it }
            val navigateUpEvents = viewModel.navigateUpEvents.testIn(backgroundScope)
            val entries = viewModel.entries.filter { it.isNotEmpty() }.testIn(backgroundScope)
            viewModel.onClickSave()
            advanceUntilIdle()

            assertModel(entries.awaitItem().values.single())
            navigateUpEvents.awaitItem()
        }
    }

    @Test
    fun addEmpty_clickSave_error() = runTest {
        turbineScope {
            val viewModel = testViewModel(hasError = true)
            snapshotFlow { viewModel.sectionsLoading }.await { !it }

            val navigateUpEvents = viewModel.navigateUpEvents.testIn(backgroundScope)
            val entries = viewModel.entries.testIn(backgroundScope)
            viewModel.onClickSave()
            advanceUntilIdle()

            assertThat(entries.awaitItem()).isEmpty()
            navigateUpEvents.expectNoEvents()
        }
    }

    @Test
    fun addEmpty_longClickSave_success() = runTest {
        turbineScope {
            val viewModel = testViewModel()
            snapshotFlow { viewModel.sectionsLoading }.await { !it }

            val navigateUpEvents = viewModel.navigateUpEvents.testIn(backgroundScope)
            val entries = viewModel.entries.filter { it.isNotEmpty() }.testIn(backgroundScope)
            viewModel.onLongClickSave()
            advanceUntilIdle()

            assertModel(entries.awaitItem().values.single(), skipIgnoreableErrors = true)
            navigateUpEvents.awaitItem()
        }
    }

    @Test
    fun addEmpty_longClickSave_error() = runTest {
        turbineScope {
            val viewModel = testViewModel(hasError = true)
            snapshotFlow { viewModel.sectionsLoading }.await { !it }

            val navigateUpEvents = viewModel.navigateUpEvents.testIn(backgroundScope)
            val entries = viewModel.entries.filter { it.isNotEmpty() }.testIn(backgroundScope)
            viewModel.onLongClickSave()
            advanceUntilIdle()

            assertModel(
                model = entries.awaitItem().values.single(),
                skipIgnoreableErrors = true
            )
            navigateUpEvents.awaitItem()
        }
    }

    @Test
    fun delete_doesNothing() = runTest {
        turbineScope {
            val viewModel = testViewModel(hasError = true)
            snapshotFlow { viewModel.sectionsLoading }.await { !it }

            val navigateUpEvents = viewModel.navigateUpEvents.testIn(backgroundScope)
            val entries = viewModel.entries.testIn(backgroundScope)
            viewModel.onConfirmDelete()
            advanceUntilIdle()
            snapshotFlow { viewModel.deleting }.await { !it }
            advanceUntilIdle()
            snapshotFlow { viewModel.deleting }.await { !it }

            assertThat(entries.awaitItem().entries).isEmpty()
            navigateUpEvents.expectNoEvents()
        }
    }
}
