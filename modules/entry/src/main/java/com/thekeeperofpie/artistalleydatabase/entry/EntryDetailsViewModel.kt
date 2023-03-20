package com.thekeeperofpie.artistalleydatabase.entry

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class EntryDetailsViewModel<Entry, Model>(
    protected val application: Application,
    protected val scopedIdType: String,
    @StringRes private val imageContentDescriptionRes: Int,
    entrySettings: EntrySettings,
) : ViewModel() {

    protected enum class Type {
        ADD, SINGLE_EDIT, MULTI_EDIT
    }

    protected lateinit var entryIds: List<EntryId>
    protected lateinit var type: Type

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
                navHostController.popBackStack()
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
                    navHostController.popBackStack()
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
        saveImagesResult: Map<EntryId, EntryImageController.SaveResult>,
        skipIgnoreableErrors: Boolean = false
    ): Boolean

    abstract suspend fun saveMultiEditEntry(
        saveImagesResult: Map<EntryId, EntryImageController.SaveResult>,
        skipIgnoreableErrors: Boolean = false
    ): Boolean

    abstract suspend fun deleteEntry(entryId: EntryId)
}