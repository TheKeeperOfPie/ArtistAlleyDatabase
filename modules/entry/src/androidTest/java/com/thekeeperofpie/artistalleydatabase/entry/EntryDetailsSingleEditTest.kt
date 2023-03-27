package com.thekeeperofpie.artistalleydatabase.entry

import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import java.util.UUID

class EntryDetailsSingleEditTest : EntryDetailsTestBase() {

    @Test
    fun clickSave_success() = runTest {
        val entry = TestEntry(UUID.randomUUID().toString(), "testData")
        val viewModel = testViewModel(existingEntries = arrayOf(entry)).apply {
            initialize(listOf(EntryId("test", entry.id)))
        }

        await().until { !viewModel.sectionsLoading }

        viewModel.editModel("newData")
        viewModel.runSaveAndWait(this, navHostController, success = true) {
            onClickSave(navHostController)
        }

        assertModel(viewModel.entries.values.single(), "newData")
        verify(navHostController).popBackStack()
    }

    @Test
    fun clickSave_error() = runTest {
        val entry = TestEntry(UUID.randomUUID().toString(), "testData")
        val viewModel = testViewModel(hasError = true, existingEntries = arrayOf(entry)).apply {
            initialize(listOf(EntryId("test", entry.id)))
        }

        await().until { !viewModel.sectionsLoading }

        viewModel.editModel("newData")
        viewModel.runSaveAndWait(this, navHostController, success = false) {
            onClickSave(navHostController)
        }

        assertModel(viewModel.entries.values.single(), "testData")
        verify(navHostController, Mockito.never()).popBackStack()
    }
}
