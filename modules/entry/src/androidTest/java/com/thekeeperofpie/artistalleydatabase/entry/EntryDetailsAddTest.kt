package com.thekeeperofpie.artistalleydatabase.entry

import com.google.common.truth.Truth.assertThat
import com.thekeeperofpie.artistalleydatabase.test_utils.during
import com.thekeeperofpie.artistalleydatabase.test_utils.withDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class EntryDetailsAddTest : EntryDetailsTestBase() {

    @Test
    fun addEmpty_clickSave_success() = runTest {
        val viewModel = testViewModel().apply { initialize(emptyList()) }
        await().until { !viewModel.sectionsLoading }

        viewModel.runSaveAndWait(this, navHostController, success = true) {
            onClickSave(navHostController)
        }

        assertModel(viewModel.entries.values.single())
        verify(navHostController).navigateUp()
    }

    @Test
    fun addEmpty_clickSave_error() = runTest {
        val viewModel = testViewModel(hasError = true).apply { initialize(emptyList()) }
        await().until { !viewModel.sectionsLoading }

        viewModel.runSaveAndWait(this, navHostController, success = false) {
            onClickSave(navHostController)
        }

        assertThat(viewModel.entries).isEmpty()
        verify(navHostController, never()).navigateUp()
    }

    @Test
    fun addEmpty_longClickSave_success() = runTest {
        val viewModel = testViewModel().apply { initialize(emptyList()) }
        await().until { !viewModel.sectionsLoading }

        viewModel.runSaveAndWait(this, navHostController, success = true) {
            onLongClickSave(navHostController)
        }

        assertModel(viewModel.entries.values.single(), skipIgnoreableErrors = true)
        verify(navHostController).navigateUp()
    }

    @Test
    fun addEmpty_longClickSave_error() = runTest {
        val viewModel = testViewModel(hasError = true).apply { initialize(emptyList()) }
        await().until { !viewModel.sectionsLoading }

        viewModel.runSaveAndWait(this, navHostController, success = true) {
            onLongClickSave(navHostController)
        }

        assertModel(viewModel.entries.values.single(), skipIgnoreableErrors = true)
        verify(navHostController).navigateUp()
    }

    @Test
    fun delete_doesNothing() = runTest {
        val viewModel = testViewModel(hasError = true).apply { initialize(emptyList()) }
        await().until { !viewModel.sectionsLoading }

        withDispatchers {
            viewModel.onConfirmDelete(navHostController)
            await().during(2.seconds).until { !viewModel.deleting }
            advanceUntilIdle()
            await().during(2.seconds).until { !viewModel.deleting }
        }

        assertThat(viewModel.entries).isEmpty()
        verify(navHostController, never()).navigateUp()
    }
}
