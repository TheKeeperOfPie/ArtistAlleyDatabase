package com.thekeeperofpie.artistalleydatabase.entry

import android.net.Uri
import com.thekeeperofpie.artistalleydatabase.test_utils.mockStrict

internal class TestViewModel(
    private val hasError: Boolean = false,
    private val cropUri: Uri? = null,
) : EntryDetailsViewModel<TestEntry, TestModel>(mockStrict(), "test", -1, TestSettings(cropUri)) {

    val output = mutableListOf<TestEntry>()

    private var model: TestModel? = null

    override suspend fun buildAddModel() =
        TestModel(cropUri?.toString())

    override suspend fun saveSingleEntry(
        saveImagesResult: Map<EntryId, EntryImageController.SaveResult>,
        skipIgnoreableErrors: Boolean
    ) = if (hasError && !skipIgnoreableErrors) false else {
        output += TestEntry(model!!.data, skipIgnoreableErrors)
        true
    }

    override fun initializeForm(model: TestModel) {
        this.model = model
    }

    override suspend fun buildSingleEditModel(entryId: EntryId) =
        throw AssertionError("Should not be called")

    override suspend fun buildMultiEditModel() = throw AssertionError("Should not be called")

    override suspend fun saveMultiEditEntry(
        saveImagesResult: Map<EntryId, EntryImageController.SaveResult>,
        skipIgnoreableErrors: Boolean
    ) = throw AssertionError("Should not be called")

    override suspend fun deleteEntry(entryId: EntryId) =
        throw AssertionError("Should not be called")
}