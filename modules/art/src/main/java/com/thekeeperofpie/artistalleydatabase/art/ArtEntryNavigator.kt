package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.art.browse.selection.ArtBrowseSelectionScreen
import com.thekeeperofpie.artistalleydatabase.art.browse.selection.ArtBrowseSelectionViewModel
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.form.EntryDetailsScreen
import com.thekeeperofpie.artistalleydatabase.form.EntryNavigator
import com.thekeeperofpie.artistalleydatabase.form.EntryUtils
import com.thekeeperofpie.artistalleydatabase.form.EntryUtils.entryDetailsComposable

class ArtEntryNavigator(
    private val application: Application,
) : EntryNavigator, BrowseSelectionNavigator {

    override fun initialize(
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder
    ) {
        navGraphBuilder.composable(
            "artEntrySelection" +
                    "?queryType={queryType}" +
                    "&title={title}" +
                    "&queryId={queryId}" +
                    "&queryString={queryString}",
            arguments = listOf(
                navArgument("queryType") {
                    type = NavType.StringType
                },
                navArgument("title") {
                    type = NavType.StringType
                },
                navArgument("queryId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("queryString") {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) {
            val arguments = it.arguments!!
            val column = ArtEntryColumn.valueOf(arguments.getString("queryType")!!)
            val title = arguments.getString("title")!!
            val queryId = arguments.getString("queryId")
            val queryString = arguments.getString("queryString")
            val viewModel = hiltViewModel<ArtBrowseSelectionViewModel>()
            val query: Either<String, String> = if (queryId != null) {
                Either.Left(queryId)
            } else {
                Either.Right(queryString!!)
            }

            viewModel.initialize(column, query)
            ArtBrowseSelectionScreen(
                title = { title },
                loading = { viewModel.loading },
                entries = { viewModel.entries.collectAsLazyPagingItems() },
                selectedItems = { viewModel.selectedEntries.keys },
                onClickEntry = { index, entry ->
                    if (viewModel.selectedEntries.isNotEmpty()) {
                        viewModel.selectEntry(index, entry)
                    } else {
                        val imageRatio = entry.imageWidthToHeightRatio
                        navHostController.navigate(
                            "artEntryDetails"
                                    + "?entry_id=${entry.id}"
                                    + "&entry_image_ratio=$imageRatio"
                        )
                    }
                },
                onLongClickEntry = viewModel::selectEntry,
                onClickClear = viewModel::clearSelected,
//                onClickEdit = { editSelected(navController, viewModel.selectedEntries) },
                onConfirmDelete = viewModel::onDeleteSelected,
            )
        }

        navGraphBuilder.entryDetailsComposable(route = "artEntryDetails") { id, imageRatio ->
            val viewModel = hiltViewModel<ArtEntryEditViewModel>().initialize(id, imageRatio)
            EntryDetailsScreen(
                { id },
                { viewModel.imageWidthToHeightRatio },
                imageUri = { viewModel.imageUri },
                onImageSelected = { if (it != null) viewModel.imageUri = it },
                onImageSelectError = {
                    viewModel.errorResource = UtilsStringR.error_fail_to_load_image to it
                },
                onImageSizeResult = { width, height ->
                    viewModel.onImageSizeResult(height / width.toFloat())
                },
                onImageClickOpen = {
                    ArtEntryUtils.getImageFile(application, id).takeIf { it.exists() }
                        ?.let { EntryUtils.openInternalImage(navHostController, it) }
                },
                areSectionsLoading = { viewModel.areSectionsLoading },
                sections = { viewModel.sections },
                saving = { viewModel.saving },
                onClickSave = { viewModel.onClickSave(navHostController) },
                onLongClickSave = { viewModel.onLongClickSave(navHostController) },
                errorRes = { viewModel.errorResource },
                onErrorDismiss = { viewModel.errorResource = null },
                onConfirmDelete = { viewModel.onConfirmDelete(navHostController) },
                cropState = viewModel.cropState,
            )
        }
    }

    override fun navigate(navHostController: NavHostController, entry: BrowseEntryModel) {
        val query = entry.queryIdOrString
        val queryParam = if (query is Either.Left) {
            "&queryId=${query.value}"
        } else {
            "&queryString=${query.rightOrNull()}"
        }
        navHostController.navigate(
            "artEntrySelection" +
                    "?queryType=${entry.queryType}" +
                    "&title=${entry.text}" +
                    queryParam
        )
    }
}