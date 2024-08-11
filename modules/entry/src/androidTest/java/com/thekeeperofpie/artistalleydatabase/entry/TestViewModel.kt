package com.thekeeperofpie.artistalleydatabase.entry

import android.net.Uri
import com.benasher44.uuid.Uuid
import com.thekeeperofpie.artistalleydatabase.test_utils.mockStrict
import com.thekeeperofpie.artistalleydatabase.test_utils.whenever
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.AppJson
import java.io.File

internal class TestViewModel(
    private val hasError: Boolean = false,
    private val cropUri: Uri? = null,
    val entries: MutableMap<String, TestEntry> = mutableMapOf(),
    private val testDirectory: File,
    appJson: AppJson,
) : EntryDetailsViewModel<TestEntry, TestModel>(
    TestEntry::class,
    mockStrict {
        whenever(filesDir) {
            testDirectory.resolve(Uuid.randomUUID().toString()).apply { mkdirs() }
        }
    },
    "test",
    -1,
    TestSettings(cropUri),
    appJson = appJson,
) {

    override val sections = emptyList<EntrySection>()

    private var model: TestModel? = null

    override suspend fun buildAddModel() =
        TestModel(null, cropUri?.toString())

    override suspend fun saveSingleEntry(
        saveImagesResult: Map<EntryId, List<EntryImageController.SaveResult>>,
        skipIgnoreableErrors: Boolean
    ) = if (hasError && !skipIgnoreableErrors) false else {
        if (entryIds.isEmpty()) {
            val id = Uuid.randomUUID().toString()
            entries[id] = TestEntry(id, model!!.data, skipIgnoreableErrors)
        } else {
            entryIds.forEach {
                entries[it.valueId] = TestEntry(it.valueId, model!!.data, skipIgnoreableErrors)
            }
        }
        true
    }

    override fun initializeForm(model: TestModel) {
        this.model = model
    }

    override suspend fun buildSingleEditModel(entryId: EntryId) =
        TestModel(entryId.valueId, entries[entryId.valueId]?.data)

    override suspend fun buildMultiEditModel() = throw AssertionError("Should not be called")

    override suspend fun saveMultiEditEntry(
        saveImagesResult: Map<EntryId, List<EntryImageController.SaveResult>>,
        skipIgnoreableErrors: Boolean
    ) = throw AssertionError("Should not be called")

    override suspend fun deleteEntry(entryId: EntryId) {
        entries.remove(entryId.valueId)
    }

    fun editModel(data: String) {
        model!!.data = data
    }

    // TODO: Add tests for unsaved changes prompt
    override fun entry() = when (type) {
        Type.ADD -> null
        Type.SINGLE_EDIT -> TestEntry("", model!!.data, false)
        Type.MULTI_EDIT -> null
    }
}
