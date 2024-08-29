package com.thekeeperofpie.artistalleydatabase.entry

import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.image.crop.CropController
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.deleteRecursively
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import io.github.petertrr.diffutils.text.DiffTagGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.StringResource
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
abstract class EntryDetailsViewModel<Entry : Any, Model>(
    private val entryClass: KClass<Entry>,
    private val appFileSystem: AppFileSystem,
    protected val scopedIdType: String,
    @StringRes private val imageContentDescriptionRes: Int,
    private val appJson: AppJson,
    settings: CropSettings,
    val cropController: CropController,
) : ViewModel() {

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

    var errorResource by mutableStateOf<Pair<Either<StringResource, Int>, Throwable?>?>(null)

    var sectionsLoading by mutableStateOf(true)
        private set

    var saving by mutableStateOf(false)
        private set

    var deleting by mutableStateOf(false)

    val entryImageController = EntryImageController(
        scope = viewModelScope,
        appFileSystem = appFileSystem,
        scopedIdType = scopedIdType,
        onError = { errorResource = Either.Left<StringResource, Int>(it.first) to it.second },
        imageContentDescriptionRes = imageContentDescriptionRes,
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
        viewModelScope.launch(Dispatchers.Main) {
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
        viewModelScope.launch(CustomDispatchers.IO) {
            val model = when (type) {
                Type.ADD -> buildAddModel()
                Type.SINGLE_EDIT -> buildSingleEditModel(entryIds.single())
                Type.MULTI_EDIT -> buildMultiEditModel()
            }
            val (initialEntry, initialImages) = withContext(CustomDispatchers.Main) {
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

    fun onExitConfirm(onBackPressedDispatcher: OnBackPressedDispatcher) {
        showExitPrompt = false
        initialEntryHashCode = null
        initialImagesHashCode = null
        onBackPressedDispatcher.onBackPressed()
    }

    fun onExitDismiss() {
        showExitPrompt = false
    }

    fun onConfirmDelete(navHostController: NavHostController) {
        if (deleting || saving) return
        if (type != Type.SINGLE_EDIT) {
            // Don't delete from details page unless editing a single entry to avoid mistakes
            return
        }

        deleting = true

        val entryId = entryIds.single()
        viewModelScope.launch(CustomDispatchers.IO) {
            SystemFileSystem.deleteRecursively(
                EntryUtils.getEntryImageFolder(appFileSystem, entryId)
            )
            deleteEntry(entryId)
            withContext(CustomDispatchers.Main) {
                initialEntryHashCode = null
                initialImagesHashCode = null
                navHostController.navigateUp()
            }
        }
    }

    fun onClickSave(navHostController: NavHostController) {
        save(navHostController, skipIgnoreableErrors = false)
    }

    fun onLongClickSave(navHostController: NavHostController) {
        save(navHostController, skipIgnoreableErrors = true)
    }

    private fun save(navHostController: NavHostController, skipIgnoreableErrors: Boolean) {
        if (saving || deleting) return
        saving = true

        viewModelScope.launch(CustomDispatchers.IO) {
            val success = saveEntry(skipIgnoreableErrors)
            withContext(CustomDispatchers.Main) {
                if (success) {
                    initialEntryHashCode = null
                    initialImagesHashCode = null
                    navHostController.navigateUp()
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
        val serializer = appJson.json.serializersModule.serializer(entryClass, emptyList(), true)
        return appJson.json.encodeToString(serializer, entry)
    }

    private fun serializedImagesString(entryImages: List<EntryImage>) =
        appJson.json.encodeToString(entryImages.map(::EntryImageTriple))

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
