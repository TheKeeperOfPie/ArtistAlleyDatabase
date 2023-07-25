package com.thekeeperofpie.artistalleydatabase.entry

import android.app.Application
import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.github.difflib.text.DiffRowGenerator
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import kotlin.reflect.KClass


@OptIn(ExperimentalSerializationApi::class)
abstract class EntryDetailsViewModel<Entry : Any, Model>(
    private val entryClass: KClass<Entry>,
    protected val application: Application,
    protected val scopedIdType: String,
    @StringRes private val imageContentDescriptionRes: Int,
    entrySettings: EntrySettings,
    private val appJson: AppJson,
) : ViewModel() {

    companion object {

        // TODO: Move color definitions somewhere else/use theme?
        private val diffGenerator = DiffRowGenerator.create()
            .showInlineDiffs(true)
            .inlineDiffByWord(true)
            .oldTag { start -> if (start) "<font color=\"red\"><s>" else "</font></s>" }
            .newTag { start -> if (start) "<font color=\"#39B5E5\"><b>" else "</font></b>" }
            .build()
    }

    protected enum class Type {
        ADD, SINGLE_EDIT, MULTI_EDIT
    }

    abstract val sections: List<EntrySection>

    var showExitPrompt by mutableStateOf(false)

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    var sectionsLoading by mutableStateOf(true)
        private set

    var saving by mutableStateOf(false)
        private set

    var deleting by mutableStateOf(false)

    val entryImageController = EntryImageController(
        scopeProvider = { viewModelScope },
        application = application,
        settings = entrySettings,
        scopedIdType = scopedIdType,
        onError = { errorResource = it },
        imageContentDescriptionRes = imageContentDescriptionRes,
        onImageSizeResult = { width, height -> onImageSizeResult(height / width.toFloat()) }
    )

    protected lateinit var entryIds: List<EntryId>
    protected lateinit var type: Type

    /**
     * Tracks the initial entry state to compare against and prompt to confirm exit if unsaved
     * changes are detected. Set to null to skip exit prompt.
     */
    private var initialEntryHashCode: Int? = null
    private var initialEntrySerializedForm: String? = null
    var entrySerializedFormDiff by mutableStateOf("" to "")

    fun initialize(entryIds: List<EntryId>) {
        if (this::entryIds.isInitialized) return
        this.entryIds = entryIds
        this.type = when (entryIds.size) {
            0 -> Type.ADD
            1 -> Type.SINGLE_EDIT
            else -> Type.MULTI_EDIT
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            val model = when (type) {
                Type.ADD -> buildAddModel()
                Type.SINGLE_EDIT -> {
                    val model = async { buildSingleEditModel(entryIds.single()) }
                    // TODO: Move this delay into the UI layer
                    // Delay to allow the shared element transition to finish
                    delay(AnimationUtils.multipliedByAnimatorScale(application, 350L))
                    model.await()
                }
                Type.MULTI_EDIT -> buildMultiEditModel()
            }
            withContext(CustomDispatchers.Main) {
                model?.run(::initializeForm)

                val entry = entry()
                initialEntryHashCode = entry.hashCode()

                val serializer =
                    appJson.json.serializersModule.serializer(entryClass, emptyList(), true)
                initialEntrySerializedForm = appJson.json.encodeToString(serializer, entry)

                sectionsLoading = false
            }
        }

        entryImageController.initialize(entryIds)
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
        val entry = entry()
        val currentEntryHashCode = entry.hashCode()
        if (initialEntryHashCode == null || initialEntryHashCode == currentEntryHashCode) {
            return true
        }

        val serializer = appJson.json.serializersModule.serializer(entryClass, emptyList(), true)
        val newEntryString = appJson.json.encodeToString(serializer, entry)
        val rows = diffGenerator.generateDiffRows(
            initialEntrySerializedForm?.lines().orEmpty(),
            newEntryString.lines()
        )

        entrySerializedFormDiff = rows.joinToString(separator = "\n") { it.oldLine } to
                rows.joinToString(separator = "\n") { it.newLine }

        showExitPrompt = true
        return false
    }

    fun onExitConfirm(onBackPressedDispatcher: OnBackPressedDispatcher) {
        showExitPrompt = false
        initialEntryHashCode = null
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
            EntryUtils.getEntryImageFolder(application, entryId).deleteRecursively()
            deleteEntry(entryId)
            withContext(CustomDispatchers.Main) {
                initialEntryHashCode = null
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
        skipIgnoreableErrors: Boolean = false
    ): Boolean

    abstract suspend fun saveMultiEditEntry(
        saveImagesResult: Map<EntryId, List<EntryImageController.SaveResult>>,
        skipIgnoreableErrors: Boolean = false
    ): Boolean

    abstract suspend fun deleteEntry(entryId: EntryId)
}
