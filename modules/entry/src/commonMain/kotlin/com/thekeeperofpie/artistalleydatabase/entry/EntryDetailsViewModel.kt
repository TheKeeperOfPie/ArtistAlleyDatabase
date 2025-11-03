package com.thekeeperofpie.artistalleydatabase.entry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.image.crop.CropControllerFactory
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.deleteRecursively
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import io.github.petertrr.diffutils.text.DiffTagGenerator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.StringResource
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
abstract class EntryDetailsViewModel<Entry : Any, Model>(
    private val entryClass: KClass<Entry>,
    private val appFileSystem: AppFileSystem,
    protected val scopedIdType: String,
    private val json: Json,
    settings: CropSettings,
    cropControllerFactory: CropControllerFactory,
    protected val customDispatchers: CustomDispatchers,
) : ViewModel() {

    val cropController = cropControllerFactory.create(viewModelScope)

    val navigateUpEvents = MutableSharedFlow<Unit>()

    companion object {

        // TODO: Move color definitions somewhere else/use theme?
        private val diffGenerator = DiffRowGenerator(
            showInlineDiffs = true,
            inlineDiffByWord = true,
            oldTag = object : DiffTagGenerator {
                override fun generateOpen(tag: DiffRow.Tag) = "<font color=\"red\"><s>"
                override fun generateClose(tag: DiffRow.Tag) = "</font></s>"
            },
            newTag = object : DiffTagGenerator {
                override fun generateOpen(tag: DiffRow.Tag) = "<font color=\"#39B5E5\"><b>"
                override fun generateClose(tag: DiffRow.Tag) = "</font></b>"
            },
        )
    }

    protected enum class Type {
        ADD, SINGLE_EDIT, MULTI_EDIT
    }

    abstract val sections: List<EntrySection>

    var showExitPrompt by mutableStateOf(false)

    var errorResource by mutableStateOf<Pair<StringResource, Throwable?>?>(null)

    var sectionsLoading by mutableStateOf(true)
        private set

    var saving by mutableStateOf(false)
        private set

    var deleting by mutableStateOf(false)

    val entryImageController = EntryImageController(
        scope = viewModelScope,
        appFileSystem = appFileSystem,
        scopedIdType = scopedIdType,
        onError = { errorResource = it },
        onImageSizeResult = { width, height -> onImageSizeResult(height / width.toFloat()) },
    )

    protected lateinit var entryIds: List<EntryId>
    protected lateinit var type: Type

    /**
     * Tracks the initial entry state to compare against and prompt to confirm exit if unsaved
     * changes are detected. Set to null to skip exit prompt.
     */
    private var initialEntryHashCode: Int? = null
    private var initialEntrySerializedForm: String? = null
    private var initialImagesHashCode: Int? = null
    private var initialImagesSerializedForm: String? = null
    var entrySerializedFormDiff by mutableStateOf("" to "")

    init {
        viewModelScope.launch(customDispatchers.main) {
            cropController.cropResults.collect {
                entryImageController.onCropResult(it)
            }
        }
    }

    fun initialize(entryIds: List<EntryId>) {
        if (this::entryIds.isInitialized) return
        this.entryIds = entryIds
        this.type = when (entryIds.size) {
            0 -> Type.ADD
            1 -> Type.SINGLE_EDIT
            else -> Type.MULTI_EDIT
        }

        entryImageController.initialize(entryIds)
        viewModelScope.launch(customDispatchers.io) {
            val model = when (type) {
                Type.ADD -> buildAddModel()
                Type.SINGLE_EDIT -> buildSingleEditModel(entryIds.single())
                Type.MULTI_EDIT -> buildMultiEditModel()
            }
            val (initialEntry, initialImages) = withContext(customDispatchers.main) {
                model?.run(::initializeForm)
                sectionsLoading = false
                entry() to entryImageController.images.toList()
            }

            initialEntryHashCode = initialEntry.hashCode()
            initialImagesHashCode = initialImages.hashCode()

            initialEntrySerializedForm = serializedEntryString(initialEntry)
            initialImagesSerializedForm = serializedImagesString(initialImages)
        }
    }

    protected open fun onImageSizeResult(widthToHeightRatio: Float) {
    }

    protected abstract suspend fun buildAddModel(): Model?

    protected abstract suspend fun buildSingleEditModel(entryId: EntryId): Model

    protected abstract suspend fun buildMultiEditModel(): Model

    protected abstract fun initializeForm(model: Model)

    protected abstract fun entry(): Entry?

    /**
     * @return if navigation allowed
     */
    fun onNavigateBack(): Boolean {
        if (initialEntryHashCode == null) return true
        val entry = entry()
        val currentEntryHashCode = entry.hashCode()
        val images = entryImageController.images.toList()
        val currentImageHashCode = images.hashCode()
        if (initialEntryHashCode == currentEntryHashCode
            && initialImagesHashCode == currentImageHashCode
        ) {
            return true
        }

        val entryDiffRows = diffGenerator.generateDiffRows(
            initialEntrySerializedForm?.lines().orEmpty(),
            serializedEntryString(entry).lines()
        )

        val imagesDiffRows = diffGenerator.generateDiffRows(
            initialImagesSerializedForm?.lines().orEmpty(),
            serializedImagesString(images).lines(),
        )

        val allRows = imagesDiffRows + entryDiffRows
        entrySerializedFormDiff = allRows.joinToString(separator = "\n") { it.oldLine } to
                allRows.joinToString(separator = "\n") { it.newLine }

        showExitPrompt = true
        return false
    }

    fun onExitConfirm() {
        showExitPrompt = false
        initialEntryHashCode = null
        initialImagesHashCode = null
        viewModelScope.launch {
            navigateUpEvents.emit(Unit)
        }
    }

    fun onExitDismiss() {
        showExitPrompt = false
    }

    fun onConfirmDelete() {
        if (deleting || saving) return
        if (type != Type.SINGLE_EDIT) {
            // Don't delete from details page unless editing a single entry to avoid mistakes
            return
        }

        deleting = true

        val entryId = entryIds.single()
        viewModelScope.launch(customDispatchers.io) {
            appFileSystem.deleteRecursively(
                EntryUtils.getEntryImageFolder(appFileSystem, entryId)
            )
            deleteEntry(entryId)
            withContext(customDispatchers.main) {
                initialEntryHashCode = null
                initialImagesHashCode = null
                navigateUpEvents.emit(Unit)
            }
        }
    }

    fun onClickSave() = save(skipIgnoreableErrors = false)

    fun onLongClickSave() = save(skipIgnoreableErrors = true)

    fun getImagePathForShare(image: EntryImage): Path? {
        val entryId = image.entryId ?: return null
        return EntryUtils.getImagePath(appFileSystem, entryId)
    }

    private fun save(skipIgnoreableErrors: Boolean) {
        if (saving || deleting) return
        saving = true

        viewModelScope.launch(customDispatchers.io) {
            val success = saveEntry(skipIgnoreableErrors)
            withContext(customDispatchers.main) {
                if (success) {
                    initialEntryHashCode = null
                    initialImagesHashCode = null
                    navigateUpEvents.emit(Unit)
                } else {
                    saving = false
                }
            }
        }
    }

    private suspend fun saveEntry(skipIgnoreableErrors: Boolean): Boolean {
        val saveImagesResult = entryImageController.saveImages() ?: return false
        val success = if (type == Type.MULTI_EDIT) {
            saveMultiEditEntry(saveImagesResult, skipIgnoreableErrors)
        } else {
            saveSingleEntry(saveImagesResult, skipIgnoreableErrors)
        }

        if (!success) {
            return false
        }

        entryImageController.cleanUpImages(entryIds, saveImagesResult)
        return true
    }

    abstract suspend fun saveSingleEntry(
        saveImagesResult: Map<EntryId, List<EntryImageController.SaveResult>>,
        skipIgnoreableErrors: Boolean = false,
    ): Boolean

    abstract suspend fun saveMultiEditEntry(
        saveImagesResult: Map<EntryId, List<EntryImageController.SaveResult>>,
        skipIgnoreableErrors: Boolean = false,
    ): Boolean

    abstract suspend fun deleteEntry(entryId: EntryId)

    private fun serializedEntryString(entry: Entry?): String {
        val serializer = json.serializersModule.serializer(entryClass, emptyList(), true)
        return json.encodeToString(serializer, entry)
    }

    private fun serializedImagesString(entryImages: List<EntryImage>) =
        json.encodeToString(entryImages.map(::EntryImageTriple))

    @Serializable
    data class EntryImageTriple(
        val width: Int,
        val height: Int,
        val uri: String?,
    ) {
        constructor(entryImage: EntryImage) : this(
            width = entryImage.finalWidth,
            height = entryImage.finalHeight,
            uri = entryImage.finalUri?.toString(),
        )
    }
}
