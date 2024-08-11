package com.thekeeperofpie.artistalleydatabase.art

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.art.browse.selection.ArtBrowseSelectionScreen
import com.thekeeperofpie.artistalleydatabase.art.browse.selection.ArtBrowseSelectionViewModel
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.search.ArtSearchViewModel
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.entry.EntryDetailsScreen
import com.thekeeperofpie.artistalleydatabase.entry.EntryHomeScreen
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils.entryDetailsComposable
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils.navToEntryDetails
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.Either

class ArtEntryNavigator : BrowseSelectionNavigator {

    fun initialize(
        onClickNav: () -> Unit,
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder,
    ) {
        navGraphBuilder.sharedElementComposable(ArtNavDestinations.HOME.id) {
            val viewModel = hiltViewModel<ArtSearchViewModel>()
            EntryHomeScreen(
                onClickNav = onClickNav,
                query = { viewModel.query },
                onQueryChange = viewModel::onQuery,
                sections = viewModel.sections,
                entries = { viewModel.results.collectAsLazyPagingItems() },
                selectedItems = { viewModel.selectedEntries.keys },
                onClickEntry = { index, entry ->
                    if (viewModel.selectedEntries.isNotEmpty()) {
                        viewModel.selectEntry(index, entry)
                    } else {
                        navHostController.navToEntryDetails(
                            route = ArtNavDestinations.ENTRY_DETAILS.id,
                            listOf(entry.id.valueId)
                        )
                    }
                },
                onLongClickEntry = viewModel::selectEntry,
                onClickAddFab = {
                    navHostController.navToEntryDetails(
                        route = ArtNavDestinations.ENTRY_DETAILS.id,
                        emptyList(),
                    )
                },
                onClickClear = viewModel::clearSelected,
                onClickEdit = {
                    navHostController.navToEntryDetails(
                        ArtNavDestinations.ENTRY_DETAILS.id,
                        viewModel.selectedEntries.values.map { it.id.valueId }
                    )
                },
                onConfirmDelete = viewModel::deleteSelected,
                onNavigate = navHostController::navigate,
            )
        }

        navGraphBuilder.sharedElementComposable(
            ArtNavDestinations.BROWSE_SELECTION.id +
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
                upIconOption = UpIconOption.Back(navHostController),
                title = { title },
                loading = { viewModel.loading },
                entries = { viewModel.entries.collectAsLazyPagingItems() },
                selectedItems = { viewModel.selectedEntries.keys },
                onClickEntry = { index, entry ->
                    if (viewModel.selectedEntries.isNotEmpty()) {
                        viewModel.selectEntry(index, entry)
                    } else {
                        navHostController.navToEntryDetails(
                            ArtNavDestinations.ENTRY_DETAILS.id,
                            listOf(entry.id.valueId)
                        )
                    }
                },
                onLongClickEntry = viewModel::selectEntry,
                onClickClear = viewModel::clearSelected,
                onClickEdit = {
                    navHostController.navToEntryDetails(
                        ArtNavDestinations.ENTRY_DETAILS.id,
                        viewModel.selectedEntries.values.map { it.id.valueId }
                    )
                },
                onConfirmDelete = viewModel::onDeleteSelected,
            )
        }

        navGraphBuilder.entryDetailsComposable(
            route = ArtNavDestinations.ENTRY_DETAILS.id
        ) { entryIds, imageCornerDp ->
            val viewModel = hiltViewModel<ArtEntryDetailsViewModel>()
                .apply { initialize(entryIds.map { EntryId(ArtEntryUtils.SCOPED_ID_TYPE, it) }) }

            val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                ?.onBackPressedDispatcher

            var enabled by remember { mutableStateOf(true) }
            BackHandler(enabled) {
                if (viewModel.onNavigateBack()) {
                    enabled = false
                }
            }
            LaunchedEffect(enabled) {
                if (!enabled) {
                    backPressedDispatcher?.onBackPressed()
                }
            }

            EntryDetailsScreen(
                viewModel = viewModel,
                onClickBack = { navHostController.navigateUp() },
                imageCornerDp = imageCornerDp,
                onImageClickOpen = {
                    viewModel.entryImageController.onImageClickOpen(navHostController, it)
                },
                onClickSave = { viewModel.onClickSave(navHostController) },
                onLongClickSave = { viewModel.onLongClickSave(navHostController) },
                onConfirmDelete = { viewModel.onConfirmDelete(navHostController) },
                onClickSaveTemplate = { viewModel.onClickSaveTemplate() },
                onExitConfirm = { backPressedDispatcher?.let(viewModel::onExitConfirm) },
                onNavigate = navHostController::navigate,
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
            ArtNavDestinations.BROWSE_SELECTION.id +
                    "?queryType=${entry.queryType}" +
                    "&title=${entry.text}" +
                    queryParam
        )
    }
}
