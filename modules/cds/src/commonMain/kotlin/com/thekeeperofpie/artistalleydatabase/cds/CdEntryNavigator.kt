package com.thekeeperofpie.artistalleydatabase.cds

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.cds.browse.selection.CdBrowseSelectionScreen
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryColumn
import com.thekeeperofpie.artistalleydatabase.cds.utils.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.EntryDetailsScreen
import com.thekeeperofpie.artistalleydatabase.entry.EntryHomeScreen
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils.entryDetailsComposable
import com.thekeeperofpie.artistalleydatabase.image.rememberImageHandler
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils_compose.BackHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import me.tatarka.inject.annotations.Inject
import kotlin.math.roundToInt

@SingletonScope
@Inject
class CdEntryNavigator : BrowseSelectionNavigator {

    fun initialize(
        onClickNav: () -> Unit,
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder,
        cdEntryComponent: CdEntryComponent,
    ) {
        navGraphBuilder.sharedElementComposable(CdNavDestinations.HOME.id) {
            val viewModel = viewModel { cdEntryComponent.cdSearchViewModel() }
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
            val viewModel = viewModel { cdEntryComponent.cdBrowseSelectionViewModel() }
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
            val viewModel = viewModel { cdEntryComponent.cdEntryDetailsViewModel() }
                .apply { initialize(entryIds.map { EntryId(CdEntryUtils.SCOPED_ID_TYPE, it) }) }

            var enabled by remember { mutableStateOf(true) }
            BackHandler(enabled) {
                if (viewModel.onNavigateBack()) {
                    enabled = false
                }
            }
            LaunchedEffect(enabled) {
                if (!enabled) {
                    navHostController.navigateUp()
                }
            }

            val imageHandler = rememberImageHandler()
            EntryDetailsScreen(
                viewModel = viewModel,
                onClickBack = { navHostController.navigateUp() },
                imageCornerDp = imageCornerDp,
                onClickOpenImage = {
                    viewModel.entryImageController.onClickOpenImage(imageHandler, it)
                },
                onClickSave = { viewModel.onClickSave(navHostController) },
                onLongClickSave = { viewModel.onLongClickSave(navHostController) },
                onConfirmDelete = { viewModel.onConfirmDelete(navHostController) },
                onExitConfirm = { viewModel.onExitConfirm(navHostController) },
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
