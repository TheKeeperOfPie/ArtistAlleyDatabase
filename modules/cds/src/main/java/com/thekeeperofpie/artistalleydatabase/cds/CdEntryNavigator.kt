package com.thekeeperofpie.artistalleydatabase.cds

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.form.EntryDetailsScreen
import com.thekeeperofpie.artistalleydatabase.form.EntryNavigator
import com.thekeeperofpie.artistalleydatabase.form.EntryUtils
import com.thekeeperofpie.artistalleydatabase.form.EntryUtils.entryDetailsComposable

class CdEntryNavigator : EntryNavigator, BrowseSelectionNavigator {

    override fun initialize(
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder
    ) {
        navGraphBuilder.entryDetailsComposable("cdEntryDetails") { id, imageFile, imageRatio ->
            val viewModel = hiltViewModel<CdEntryEditViewModel>().initialize(id)
            EntryDetailsScreen(
                { id },
                { imageFile },
                { imageRatio },
                imageUri = { viewModel.imageUri },
                onImageSelected = { viewModel.imageUri = it },
                onImageSelectError = {
                    viewModel.errorResource = UtilsStringR.error_fail_to_load_image to it
                },
                onImageClickOpen = {
                    imageFile?.let { EntryUtils.openInternalImage(navHostController, it) }
                },
                areSectionsLoading = { viewModel.areSectionsLoading },
                sections = { viewModel.sections },
                saving = { viewModel.saving },
                onClickSave = { viewModel.onClickSave(navHostController) },
                errorRes = { viewModel.errorResource },
                onErrorDismiss = { viewModel.errorResource = null },
                onConfirmDelete = { viewModel.onConfirmDelete(navHostController) }
            )
        }
    }

    override fun navigate(navHostController: NavHostController, entry: BrowseEntryModel) {
        TODO("Not yet implemented")
    }
}