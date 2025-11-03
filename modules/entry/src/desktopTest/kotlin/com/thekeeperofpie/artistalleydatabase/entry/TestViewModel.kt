package com.thekeeperofpie.artistalleydatabase.entry

import com.thekeeperofpie.artistalleydatabase.image.crop.CropController
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Inject
internal class TestViewModel(
    private val hasError: Boolean = false,
    private val cropUri: String? = null,
    entries: Map<String, TestEntry> = mapOf(),
    json: Json,
    cropController: CropController,
    appFileSystem: AppFileSystem,
    customDispatchers: CustomDispatchers,
) : EntryDetailsViewModel<TestEntry, TestModel>(
    TestEntry::class,
    appFileSystem,
    "test",
    json = json,
    settings = TestSettings(cropUri),
    cropControllerFunction = { cropController },
    customDispatchers = customDispatchers,
) {
    val entries = MutableStateFlow(entries)

    override val sections = emptyList<EntrySection>()

    private var model: TestModel? = null

    override suspend fun buildAddModel() =
        TestModel(null, cropUri?.toString())

    override suspend fun saveSingleEntry(
        saveImagesResult: Map<EntryId, List<EntryImageController.SaveResult>>,
        skipIgnoreableErrors: Boolean,
    ) = if (hasError && !skipIgnoreableErrors) false else {
        if (entryIds.isEmpty()) {
            val id = Uuid.random().toString()
            entries.update { it + (id to TestEntry(id, model!!.data, skipIgnoreableErrors)) }
        } else {
            entries.update {
                it + entryIds.map {
                    it.valueId to TestEntry(it.valueId, model!!.data, skipIgnoreableErrors)
                }
            }
        }
        true
    }

    override fun initializeForm(model: TestModel) {
        this.model = model
    }

    override suspend fun buildSingleEditModel(entryId: EntryId) =
        TestModel(entryId.valueId, entries.value[entryId.valueId]?.data)

    override suspend fun buildMultiEditModel() = throw AssertionError("Should not be called")

    override suspend fun saveMultiEditEntry(
        saveImagesResult: Map<EntryId, List<EntryImageController.SaveResult>>,
        skipIgnoreableErrors: Boolean,
    ) = throw AssertionError("Should not be called")

    override suspend fun deleteEntry(entryId: EntryId) {
        entries.update { it - entryId.valueId }
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
