package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.form.CropUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ArtEntryEditViewModel @Inject constructor(
    application: Application,
    appJson: AppJson,
    artEntryDao: ArtEntryDetailsDao,
    dataConverter: DataConverter,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
    aniListAutocompleter: AniListAutocompleter,
    settings: ArtSettings,
) : ArtEntryDetailsViewModel(
    application,
    appJson,
    artEntryDao,
    dataConverter,
    mediaRepository,
    characterRepository,
    aniListAutocompleter,
    settings,
) {
    companion object {
        private const val TAG = "ArtEntryEditViewModel"
    }

    var entryId: String? = null
    lateinit var entry: ArtEntry

    var imageUri by mutableStateOf<Uri?>(null)

    var areSectionsLoading by mutableStateOf(true)

    var entryImageRatio by mutableStateOf(1f)

    private var imageCropUri by mutableStateOf<Uri?>(null)
    private var cropDocumentRequested by mutableStateOf(false)
    private var cropReady by mutableStateOf(false)

    val cropState = CropUtils.CropState(
        imageCropNeedsDocument = { imageCropUri == null },
        onImageCropDocumentChosen = ::onImageCropDocumentChosen,
        onImageRequestCrop = ::onImageRequestCrop,
        onCropFinished = ::onCropFinished,
        cropReady = { cropReady },
        onCropConfirmed = { cropDocumentRequested = true },
        cropDocumentRequested = { cropDocumentRequested },
    )

    private var deleting = false

    var saving by mutableStateOf(false)
        private set

    fun initialize(entryId: String, entryImageRatio: Float) = apply {
        if (this.entryId != null) return@apply
        this.entryId = entryId
        this.entryImageRatio = entryImageRatio
        imageUri = ArtEntryUtils.getImageFile(application, entryId).takeIf { it.exists() }?.toUri()

        if (entryImageRatio > 1f) {
            onImageSizeResult(1, 2)
        } else {
            onImageSizeResult(2, 1)
        }

        viewModelScope.launch(Dispatchers.IO) {
            entry = artEntryDao.getEntry(entryId)
            delay(AnimationUtils.multipliedByAnimatorScale(application, 350L).coerceAtLeast(350L))
            withContext(Dispatchers.Main) {
                initializeForm(buildModel(entry))
                areSectionsLoading = false
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val uri = settings.loadCropDocumentUri()
            if (uri != null) {
                val outputStream = try {
                    application.contentResolver.openOutputStream(uri)
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading crop URI: $uri")
                    null
                }
                if (outputStream != null) {
                    outputStream.close()
                    launch(Dispatchers.Main) {
                        imageCropUri = uri
                    }
                }
            }
        }
    }

    fun onConfirmDelete(navHostController: NavHostController) {
        if (deleting || saving) return
        deleting = true

        viewModelScope.launch(Dispatchers.IO) {
            artEntryDao.delete(entryId!!)

            withContext(Dispatchers.Main) {
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

    private fun onImageCropDocumentChosen(uri: Uri?) {
        uri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                application.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error persisting URI grant $uri")
                launch(Dispatchers.Main) {
                    errorResource = UtilsStringR.error_fail_to_crop_image to e
                }
                return@launch
            }

            settings.saveCropDocumentUri(uri)
            val entryId = entryId
            val imageUri = imageUri
            val inputStream = if (imageUri != null) {
                application.contentResolver.openInputStream(imageUri)
            } else if (entryId != null) {
                ArtEntryUtils.getImageFile(application, entryId).inputStream()
            } else null

            inputStream?.use { input ->
                application.contentResolver.openOutputStream(uri)?.use { output ->
                    input.copyTo(output)
                }
            }?.run {
                launch(Dispatchers.Main) {
                    imageCropUri = uri
                    cropReady = true
                }
            }
        }
    }

    private fun onImageRequestCrop() {
        val imageCropUri = imageCropUri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val entryId = entryId
            val imageUri = imageUri
            val inputStream = if (imageUri != null) {
                application.contentResolver.openInputStream(imageUri)
            } else if (entryId != null) {
                ArtEntryUtils.getImageFile(application, entryId).inputStream()
            } else null

            inputStream?.use { input ->
                application.contentResolver.openOutputStream(imageCropUri)?.use { output ->
                    input.copyTo(output)
                }
            }

            application.grantUriPermission(
                CropUtils.PHOTOS_PACKAGE_NAME,
                imageCropUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            launch(Dispatchers.Main) {
                cropReady = true
            }
        }
    }

    private fun onCropFinished(success: Boolean) {
        val imageCropUri = imageCropUri ?: return
        if (success) {
            viewModelScope.launch(Dispatchers.IO) {
                val outputFile = ArtEntryUtils.getCropTempFile(application)
                application.contentResolver.openInputStream(imageCropUri)?.use { input ->
                    outputFile.outputStream().use { output -> input.copyTo(output) }
                }

                // Clear the temporary crop file contents
                application.contentResolver.openOutputStream(imageCropUri, "wt")?.use {
                    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                        .compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                val widthHeightRatio = outputFile.inputStream().use {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(it, null, options)
                    options.outHeight.toFloat() / options.outWidth
                }

                launch(Dispatchers.Main) {
                    entryImageRatio = widthHeightRatio
                    imageUri = outputFile.toUri()
                        .buildUpon()
                        .appendQueryParameter(
                            "invalidateIteration",
                            invalidateIteration++.toString()
                        )
                        .build()
                    cropReady = false
                }
            }
        }
    }

    private fun save(navHostController: NavHostController, skipIgnoreableErrors: Boolean) {
        if (saving || deleting) return
        saving = true

        viewModelScope.launch(Dispatchers.IO) {
            val success =
                saveEntry(imageUri, entryId!!, skipIgnoreableErrors = skipIgnoreableErrors)
            withContext(Dispatchers.Main) {
                if (success) {
                    navHostController.popBackStack()
                } else {
                    saving = false
                }
            }
        }
    }
}