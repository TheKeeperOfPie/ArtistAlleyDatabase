package com.thekeeperofpie.artistalleydatabase.art

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.art.browse.selection.ArtBrowseSelectionScreen
import com.thekeeperofpie.artistalleydatabase.art.browse.selection.ArtBrowseSelectionViewModel
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.compose.AddBackPressInvokeFirst
import com.thekeeperofpie.artistalleydatabase.compose.BackPressStageHandler
import com.thekeeperofpie.artistalleydatabase.entry.EntryDetailsScreen
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryNavigator
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils.entryDetailsComposable
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils.navToEntryDetails

class ArtEntryNavigator : EntryNavigator, BrowseSelectionNavigator {

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
                        navHostController.navToEntryDetails(
                            "artEntryDetails",
                            listOf(entry.id.valueId)
                        )
                    }
                },
                onLongClickEntry = viewModel::selectEntry,
                onClickClear = viewModel::clearSelected,
                onClickEdit = {
                    navHostController.navToEntryDetails(
                        "artEntryDetails",
                        viewModel.selectedEntries.values.map { it.id.valueId }
                    )
                },
                onConfirmDelete = viewModel::onDeleteSelected,
            )
        }

        navGraphBuilder.entryDetailsComposable(route = "artEntryDetails") { entryIds ->
            val viewModel = hiltViewModel<ArtEntryDetailsViewModel>()
                .apply { initialize(entryIds.map { EntryId(ArtEntryUtils.SCOPED_ID_TYPE, it) }) }

            BackPressStageHandler {
                AddBackPressInvokeFirst(label = "ArtEntryNavigator exit") {
                    viewModel.onNavigateBack()
                }

                val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                    ?.onBackPressedDispatcher

                EntryDetailsScreen(
                    onClickBack = { navHostController.popBackStack() },
                    imageState = { viewModel.entryImageController.imageState },
                    onImageClickOpen = {
                        viewModel.entryImageController.onImageClickOpen(navHostController, it)
                    },
                    areSectionsLoading = { viewModel.sectionsLoading },
                    sections = { viewModel.sections },
                    saving = { viewModel.saving },
                    onClickSave = { viewModel.onClickSave(navHostController) },
                    onLongClickSave = { viewModel.onLongClickSave(navHostController) },
                    errorRes = { viewModel.errorResource },
                    onErrorDismiss = { viewModel.errorResource = null },
                    onConfirmDelete = { viewModel.onConfirmDelete(navHostController) },
                    onClickSaveTemplate = { viewModel.onClickSaveTemplate() },
                    cropState = viewModel.entryImageController.cropState,
                    showExitPrompt = viewModel.showExitPrompt,
                    onExitConfirm = { backPressedDispatcher?.let(viewModel::onExitConfirm) },
                    onExitDismiss = viewModel::onExitDismiss,
                )
            }
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