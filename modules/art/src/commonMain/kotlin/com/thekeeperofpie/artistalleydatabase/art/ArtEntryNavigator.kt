package com.thekeeperofpie.artistalleydatabase.art

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.thekeeperofpie.artistalleydatabase.art.browse.selection.ArtBrowseSelectionScreen
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.entry.EntryDetailsScreen
import com.thekeeperofpie.artistalleydatabase.entry.EntryHomeScreen
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils.entryDetailsComposable
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils.navToEntryDetails
import com.thekeeperofpie.artistalleydatabase.image.rememberImageHandler
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalComposeUiApi::class)
@SingletonScope
@Inject
class ArtEntryNavigator : BrowseSelectionNavigator {

    fun initialize(
        onClickNav: () -> Unit,
        navigationController: NavigationController,
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder,
        artEntryComponent: ArtEntryComponent,
    ) {
        navGraphBuilder.sharedElementComposable(ArtNavDestinations.HOME.id) {
            val viewModel = viewModel { artEntryComponent.artSearchViewModel() }
            var query by viewModel.query.collectAsMutableStateWithLifecycle()
            EntryHomeScreen(
                onClickNav = onClickNav,
                query = { query },
                onQueryChange = { query = it },
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
            val column = ArtEntryColumn.valueOf(arguments.read { getString("queryType") })
            val title = arguments.read { getString("title") }
            val queryId = arguments.read { getStringOrNull("queryId") }
            val queryString = arguments.read { getStringOrNull("queryString") }
            val viewModel = viewModel { artEntryComponent.artBrowseSelectionViewModel() }
            val query: Either<String, String> = if (queryId != null) {
                Either.Left(queryId)
            } else {
                Either.Right(queryString!!)
            }

            viewModel.initialize(column, query)
            ArtBrowseSelectionScreen(
                upIconOption = UpIconOption.Back(navigationController),
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
            val viewModel = viewModel { artEntryComponent.artEntryDetailsViewModel() }
                .apply { initialize(entryIds.map { EntryId(ArtEntryUtils.SCOPED_ID_TYPE, it) }) }

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

            LaunchedEffect(Unit) {
                viewModel.navigateUpEvents.collect { navHostController.navigateUp() }
            }
            val imageHandler = rememberImageHandler()
            EntryDetailsScreen(
                viewModel = viewModel,
                onClickBack = { navHostController.navigateUp() },
                imageCornerDp = imageCornerDp,
                onClickOpenImage = {
                    viewModel.entryImageController.onClickOpenImage(imageHandler, it)
                },
                onClickSave = viewModel::onClickSave,
                onLongClickSave = viewModel::onLongClickSave,
                onConfirmDelete = viewModel::onConfirmDelete,
                onClickSaveTemplate = viewModel::onClickSaveTemplate,
                onExitConfirm = viewModel::onExitConfirm,
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
