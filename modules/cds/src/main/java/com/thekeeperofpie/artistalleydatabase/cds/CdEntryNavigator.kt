package com.thekeeperofpie.artistalleydatabase.cds

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.cds.browse.selection.CdBrowseSelectionScreen
import com.thekeeperofpie.artistalleydatabase.cds.browse.selection.CdBrowseSelectionViewModel
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryColumn
import com.thekeeperofpie.artistalleydatabase.cds.search.CdSearchViewModel
import com.thekeeperofpie.artistalleydatabase.cds.utils.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.entry.EntryDetailsScreen
import com.thekeeperofpie.artistalleydatabase.entry.EntryHomeScreen
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils.entryDetailsComposable
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import kotlin.math.roundToInt

class CdEntryNavigator : BrowseSelectionNavigator {

    fun initialize(
        onClickNav: () -> Unit,
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder
    ) {
        navGraphBuilder.sharedElementComposable(CdNavDestinations.HOME.id) {
            val viewModel = hiltViewModel<CdSearchViewModel>()
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
                        onCdEntryClick(navHostController, listOf(entry.id.valueId))
                    }
                },
                onLongClickEntry = viewModel::selectEntry,
                onClickAddFab = { onCdEntryClick(navHostController, emptyList()) },
                onClickClear = viewModel::clearSelected,
                onClickEdit = {
                    onCdEntryClick(
                        navHostController,
                        viewModel.selectedEntries.values.map { it.id.valueId },
                    )
                },
                onConfirmDelete = viewModel::deleteSelected,
                onNavigate = navHostController::navigate,
            )
        }

        navGraphBuilder.sharedElementComposable(
            CdNavDestinations.BROWSE_SELECTION.id +
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
            val column = CdEntryColumn.valueOf(arguments.getString("queryType")!!)
            val title = arguments.getString("title")!!
            val queryId = arguments.getString("queryId")
            val queryString = arguments.getString("queryString")
            val viewModel = hiltViewModel<CdBrowseSelectionViewModel>()
            val query: Either<String, String> = if (queryId != null) {
                Either.Left(queryId)
            } else {
                Either.Right(queryString!!)
            }

            viewModel.initialize(column, query)
            CdBrowseSelectionScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = { title },
                loading = { viewModel.loading },
                entries = { viewModel.entries.collectAsLazyPagingItems() },
                selectedItems = { viewModel.selectedEntries.keys },
                onClickEntry = { index, entry ->
                    if (viewModel.selectedEntries.isNotEmpty()) {
                        viewModel.selectEntry(index, entry)
                    } else {
                        onCdEntryClick(navHostController, listOf(entry.id.valueId))
                    }
                },
                onLongClickEntry = viewModel::selectEntry,
                onClickClear = viewModel::clearSelected,
                onClickEdit = {
                    onCdEntryClick(
                        navHostController,
                        viewModel.selectedEntries.values.map { it.id.valueId },
                    )
                },
                onConfirmDelete = viewModel::onDeleteSelected,
            )
        }

        navGraphBuilder.entryDetailsComposable(
            CdNavDestinations.ENTRY_DETAILS.id
        ) { entryIds, imageCornerDp ->
            val viewModel = hiltViewModel<CdEntryDetailsViewModel>()
                .apply { initialize(entryIds.map { EntryId(CdEntryUtils.SCOPED_ID_TYPE, it) }) }

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
            CdNavDestinations.BROWSE_SELECTION.id +
                    "?queryType=${entry.queryType}" +
                    "&title=${entry.text}" +
                    queryParam
        )
    }

    fun onCdEntryClick(
        navHostController: NavHostController,
        entryIds: List<String>,
        imageCornerDp: Dp? = null,
    ) {
        var path = CdNavDestinations.ENTRY_DETAILS.id
        val queryParams = listOfNotNull(
            entryIds.takeIf { it.isNotEmpty() }
                ?.let { "entry_ids=${it.joinToString(separator = "&entry_ids=")}" },
            imageCornerDp?.let { "image_corner_dp=${it.value.roundToInt()}" },
        )
        if (queryParams.isNotEmpty()) {
            path += "?" + queryParams.joinToString(separator = "&")
        }
        navHostController.navigate(path)
    }
}
