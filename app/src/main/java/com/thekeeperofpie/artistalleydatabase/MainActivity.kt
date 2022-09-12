package com.thekeeperofpie.artistalleydatabase

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.MainThread
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.mxalbert.sharedelements.SharedElementsRoot
import com.thekeeperofpie.artistalleydatabase.add.AddEntryScreen
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.art.ArtAddEntryViewModel
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.search.ArtSearchViewModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseViewModel
import com.thekeeperofpie.artistalleydatabase.browse.selection.BrowseSelectionScreen
import com.thekeeperofpie.artistalleydatabase.browse.selection.BrowseSelectionViewModel
import com.thekeeperofpie.artistalleydatabase.cds.CdAddEntryViewModel
import com.thekeeperofpie.artistalleydatabase.cds.CdDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.cds.search.CdSearchViewModel
import com.thekeeperofpie.artistalleydatabase.detail.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.detail.DetailsViewModel
import com.thekeeperofpie.artistalleydatabase.edit.MultiEditScreen
import com.thekeeperofpie.artistalleydatabase.edit.MultiEditViewModel
import com.thekeeperofpie.artistalleydatabase.export.ExportScreen
import com.thekeeperofpie.artistalleydatabase.export.ExportViewModel
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.home.HomeScreen
import com.thekeeperofpie.artistalleydatabase.importing.ImportScreen
import com.thekeeperofpie.artistalleydatabase.importing.ImportViewModel
import com.thekeeperofpie.artistalleydatabase.navigation.NavDestinations
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.search.advanced.AdvancedSearchScreen
import com.thekeeperofpie.artistalleydatabase.search.advanced.AdvancedSearchViewModel
import com.thekeeperofpie.artistalleydatabase.search.results.SearchResultsScreen
import com.thekeeperofpie.artistalleydatabase.search.results.SearchResultsViewModel
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val STARTING_NAV_DESTINATION = "starting_nav_destination"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArtistAlleyDatabaseTheme {
                Surface {
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val navDrawerItems = NavDrawerItems.items()
                    val defaultSelectedItemIndex = intent.getStringExtra(STARTING_NAV_DESTINATION)
                        ?.let { navId -> navDrawerItems.indexOfFirst { it.id == navId } }
                        ?: 0
                    var selectedItemIndex by rememberSaveable {
                        mutableStateOf(defaultSelectedItemIndex)
                    }
                    val selectedItem = navDrawerItems[selectedItemIndex]

                    fun onClickNav() = scope.launch { drawerState.open() }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                navDrawerItems.forEachIndexed { index, item ->
                                    NavigationDrawerItem(
                                        icon = { Icon(item.icon, contentDescription = null) },
                                        label = { Text(stringResource(item.titleRes)) },
                                        selected = item == selectedItem,
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            selectedItemIndex = index
                                        },
                                        modifier = Modifier
                                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                                    )
                                }
                            }
                        },
                        content = {
                            val block = @Composable {
                                when (selectedItem) {
                                    NavDrawerItems.Art -> ArtScreen(::onClickNav)
                                    NavDrawerItems.Cds -> CdsScreen(::onClickNav)
                                    NavDrawerItems.Browse -> BrowseScreen(::onClickNav)
                                    NavDrawerItems.Search -> SearchScreen(::onClickNav)
                                    NavDrawerItems.Import -> {
                                        val viewModel = hiltViewModel<ImportViewModel>()
                                        ImportScreen(
                                            onClickNav = ::onClickNav,
                                            uriString = viewModel.importUriString.orEmpty(),
                                            onUriStringEdit = { viewModel.importUriString = it },
                                            onContentUriSelected = {
                                                viewModel.importUriString = it?.toString()
                                            },
                                            dryRun = { viewModel.dryRun },
                                            onToggleDryRun = {
                                                viewModel.dryRun = !viewModel.dryRun
                                            },
                                            replaceAll = { viewModel.replaceAll },
                                            onToggleReplaceAll = {
                                                viewModel.replaceAll = !viewModel.replaceAll
                                            },
                                            onClickImport = viewModel::onClickImport,
                                            importProgress = { viewModel.importProgress },
                                            errorRes = viewModel.errorResource,
                                            onErrorDismiss = { viewModel.errorResource = null }
                                        )
                                    }
                                    NavDrawerItems.Export -> {
                                        val viewModel = hiltViewModel<ExportViewModel>()
                                        ExportScreen(
                                            onClickNav = ::onClickNav,
                                            uriString = { viewModel.exportUriString.orEmpty() },
                                            onUriStringEdit = { viewModel.exportUriString = it },
                                            onContentUriSelected = {
                                                viewModel.exportUriString = it?.toString()
                                            },
                                            onClickExport = viewModel::onClickExport,
                                            exportProgress = { viewModel.exportProgress },
                                            errorRes = viewModel.errorResource,
                                            onErrorDismiss = { viewModel.errorResource = null }
                                        )
                                    }
                                }.run { /* exhaust */ }
                            }

                            if (BuildConfig.DEBUG) {
                                Column {
                                    Box(modifier = Modifier.weight(1f)) {
                                        block()
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(colorResource(R.color.launcher_background))
                                            .fillMaxWidth()
                                            .height(56.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.debug_variant),
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            } else {
                                block()
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun ArtScreen(onClickNav: () -> Unit) {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = NavDestinations.HOME) {
                composable(NavDestinations.HOME) {
                    val viewModel = hiltViewModel<ArtSearchViewModel>()
                    HomeScreen(
                        onClickNav = onClickNav,
                        query = { viewModel.query.collectAsState().value?.query.orEmpty() },
                        onQueryChange = viewModel::onQuery,
                        options = { viewModel.options },
                        onOptionChanged = { viewModel.refreshQuery() },
                        entries = { viewModel.results.collectAsLazyPagingItems() },
                        selectedItems = { viewModel.selectedEntries.keys },
                        onClickAddFab = {
                            navController.navigate(NavDestinations.ADD_ENTRY)
                        },
                        onClickEntry = { index, entry ->
                            if (viewModel.selectedEntries.isNotEmpty()) {
                                viewModel.selectEntry(index, entry)
                            } else {
                                navController.navToEntryDetails(entry)
                            }
                        },
                        onLongClickEntry = viewModel::selectEntry,
                        onClickClear = viewModel::clearSelected,
                        onClickEdit = { editSelected(navController, viewModel.selectedEntries) },
                        onConfirmDelete = viewModel::deleteSelected,
                    )
                }

                composable(NavDestinations.ADD_ENTRY) {
                    val viewModel = hiltViewModel<ArtAddEntryViewModel>()
                    AddEntryScreen(
                        imageUris = { viewModel.imageUris },
                        onImagesSelected = {
                            viewModel.imageUris.clear()
                            viewModel.imageUris.addAll(it)
                        },
                        onImageSelectError = {
                            viewModel.errorResource = UtilsStringR.error_fail_to_load_image to it
                        },
                        onImageSizeResult = viewModel::onImageSizeResult,
                        sections = { viewModel.sections },
                        saving = { viewModel.saving },
                        onClickSaveTemplate = viewModel::onClickSaveTemplate,
                        onClickSave = { viewModel.onClickSave(navController) },
                        errorRes = { viewModel.errorResource },
                        onErrorDismiss = { viewModel.errorResource = null }
                    )
                }

                addArtDetailsScreen(navController)
                addEditScreen(navController)
            }
        }
    }

    @Composable
    private fun CdsScreen(onClickNav: () -> Unit) {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = NavDestinations.HOME) {
                composable(NavDestinations.HOME) {
                    val viewModel = hiltViewModel<CdSearchViewModel>()
                    HomeScreen(
                        onClickNav = onClickNav,
                        query = { viewModel.query.collectAsState().value?.query.orEmpty() },
                        onQueryChange = viewModel::onQuery,
                        options = { viewModel.options },
                        onOptionChanged = { viewModel.refreshQuery() },
                        entries = { viewModel.results.collectAsLazyPagingItems() },
                        selectedItems = { viewModel.selectedEntries.keys },
                        onClickAddFab = {
                            navController.navigate(NavDestinations.ADD_ENTRY)
                        },
                        onClickEntry = { index, entry ->
                            if (viewModel.selectedEntries.isNotEmpty()) {
                                viewModel.selectEntry(index, entry)
                            } else {
                                navController.navToEntryDetails(entry)
                            }
                        },
                        onLongClickEntry = viewModel::selectEntry,
                        onClickClear = viewModel::clearSelected,
                        onClickEdit = { editSelected(navController, viewModel.selectedEntries) },
                        onConfirmDelete = viewModel::deleteSelected,
                    )
                }

                composable(NavDestinations.ADD_ENTRY) {
                    val viewModel = hiltViewModel<CdAddEntryViewModel>()
                    AddEntryScreen(
                        imageUris = { viewModel.imageUris },
                        onImagesSelected = {
                            viewModel.imageUris.clear()
                            viewModel.imageUris.addAll(it)
                        },
                        onImageSelectError = {
                            viewModel.errorResource = UtilsStringR.error_fail_to_load_image to it
                        },
                        sections = { viewModel.sections },
                        saving = { viewModel.saving },
                        onClickSaveTemplate = { TODO() },
                        onClickSave = { viewModel.onClickSave(navController) },
                        errorRes = { viewModel.errorResource },
                        onErrorDismiss = { viewModel.errorResource = null }
                    )
                }

                entryDetailsComposable { id, imageFile, imageRatio ->
                    val viewModel = hiltViewModel<CdDetailsViewModel>().initialize(id)
                    DetailsScreen(
                        { id },
                        { imageFile },
                        { imageRatio },
                        imageUri = { viewModel.imageUri },
                        onImageSelected = { viewModel.imageUri = it },
                        onImageSelectError = {
                            viewModel.errorResource = UtilsStringR.error_fail_to_load_image to it
                        },
                        onImageClickOpen = { imageFile?.let(::openInternalImage) },
                        areSectionsLoading = { viewModel.areSectionsLoading },
                        sections = { viewModel.sections },
                        saving = { viewModel.saving },
                        onClickSave = { viewModel.onClickSave(navController) },
                        errorRes = { viewModel.errorResource },
                        onErrorDismiss = { viewModel.errorResource = null },
                        onConfirmDelete = { viewModel.onConfirmDelete(navController) }
                    )
                }
            }
        }
    }

    @Composable
    private fun BrowseScreen(onClickNav: () -> Unit) {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = "browse") {
                composable("browse") {
                    val viewModel = hiltViewModel<BrowseViewModel>()
                    BrowseScreen(
                        onClickNav = onClickNav,
                        tabs = viewModel.tabs,
                        onClick = { column, value ->
                            val query = value.query
                            val queryParam = if (query is Either.Left) {
                                "&queryId=${query.value}"
                            } else {
                                "&queryString=${query.rightOrNull()}"
                            }
                            navController.navigate(
                                "selection" +
                                        "?column=$column" +
                                        "&title=${value.text}" +
                                        queryParam
                            )
                        },
                    )
                }

                composable(
                    "selection" +
                            "?column={column}" +
                            "&title={title}" +
                            "&queryId={queryId}" +
                            "&queryString={queryString}",
                    arguments = listOf(
                        navArgument("column") {
                            type = NavType.StringType
                        },
                        navArgument("title") {
                            type = NavType.StringType
                        },
                        navArgument("queryId") {
                            type = NavType.IntType
                            defaultValue = -1
                        },
                        navArgument("queryString") {
                            type = NavType.StringType
                            nullable = true
                        },
                    )
                ) {
                    val arguments = it.arguments!!
                    val column = ArtEntryColumn.valueOf(arguments.getString("column")!!)
                    val title = arguments.getString("title")!!
                    val queryId = arguments.getInt("queryId", -1)
                    val queryString = arguments.getString("queryString")
                    val viewModel = hiltViewModel<BrowseSelectionViewModel>()
                    val query: Either<Int, String> = if (queryId > 0) {
                        Either.Left(queryId)
                    } else {
                        Either.Right(queryString!!)
                    }

                    viewModel.initialize(column, query)
                    BrowseSelectionScreen(
                        title = { title },
                        loading = { viewModel.loading },
                        entries = { viewModel.entries.collectAsLazyPagingItems() },
                        selectedItems = { viewModel.selectedEntries.keys },
                        onClickEntry = { index, entry ->
                            if (viewModel.selectedEntries.isNotEmpty()) {
                                viewModel.selectEntry(index, entry)
                            } else {
                                navController.navToEntryDetails(entry)
                            }
                        },
                        onLongClickEntry = viewModel::selectEntry,
                        onClickClear = viewModel::clearSelected,
                        onClickEdit = { editSelected(navController, viewModel.selectedEntries) },
                        onConfirmDelete = viewModel::onDeleteSelected,
                    )
                }

                addArtDetailsScreen(navController)
                addEditScreen(navController)
            }
        }
    }

    @Composable
    private fun SearchScreen(onClickNav: () -> Unit) {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    val viewModel = hiltViewModel<AdvancedSearchViewModel>()
                    AdvancedSearchScreen(
                        onClickNav = onClickNav,
                        loading = { false },
                        sections = { viewModel.sections },
                        onClickClear = viewModel::onClickClear,
                        onClickSearch = {
                            val queryId = viewModel.onClickSearch()
                            navController.navigate("results?queryId=$queryId")
                        },
                    )
                }

                composable(
                    "results?queryId={queryId}",
                    arguments = listOf(
                        navArgument("queryId") {
                            type = NavType.StringType
                            nullable = false
                        },
                    )
                ) {
                    val arguments = it.arguments!!
                    val queryId = arguments.getString("queryId")!!
                    val viewModel = hiltViewModel<SearchResultsViewModel>()
                    viewModel.initialize(queryId)
                    SearchResultsScreen(
                        loading = { viewModel.loading },
                        entries = { viewModel.entries.collectAsLazyPagingItems() },
                        selectedItems = { viewModel.selectedEntries.keys },
                        onClickEntry = { index, entry ->
                            if (viewModel.selectedEntries.isNotEmpty()) {
                                viewModel.selectEntry(index, entry)
                            } else {
                                navController.navToEntryDetails(entry)
                            }
                        },
                        onLongClickEntry = viewModel::selectEntry,
                        onClickClear = viewModel::clearSelected,
                        onClickEdit = { editSelected(navController, viewModel.selectedEntries) },
                        onConfirmDelete = viewModel::onDeleteSelected,
                    )
                }

                addArtDetailsScreen(navController)
                addEditScreen(navController)
            }
        }
    }

    private fun NavGraphBuilder.addArtDetailsScreen(navController: NavHostController) {
        entryDetailsComposable { id, imageFile, imageRatio ->
            val viewModel = hiltViewModel<DetailsViewModel>().initialize(id, imageRatio)
            DetailsScreen(
                { id },
                { imageFile },
                { imageRatio },
                imageUri = { viewModel.imageUri },
                onImageSelected = { viewModel.imageUri = it },
                onImageSelectError = {
                    viewModel.errorResource = UtilsStringR.error_fail_to_load_image to it
                },
                onImageSizeResult = viewModel::onImageSizeResult,
                onImageClickOpen = { imageFile?.let(::openInternalImage) },
                areSectionsLoading = { viewModel.areSectionsLoading },
                sections = { viewModel.sections },
                saving = { viewModel.saving },
                onClickSave = { viewModel.onClickSave(navController) },
                errorRes = { viewModel.errorResource },
                onErrorDismiss = { viewModel.errorResource = null },
                onConfirmDelete = { viewModel.onConfirmDelete(navController) }
            )
        }
    }

    private fun NavGraphBuilder.addEditScreen(navController: NavHostController) {
        composable(
            NavDestinations.MULTI_EDIT +
                    "?entry_ids={entry_ids}",
            arguments = listOf(
                navArgument("entry_ids") {
                    type = NavType.StringType
                    nullable = false
                },
            )
        ) {
            val arguments = it.arguments!!
            val entryIds = arguments.getString("entry_ids")!!.split(',')

            val viewModel = hiltViewModel<MultiEditViewModel>()
            viewModel.initialize(entryIds)

            MultiEditScreen(
                imageUris = { viewModel.imageUris },
                onImageSelected = { index, uri -> viewModel.setImageUri(index, uri) },
                onImageSelectError = {
                    viewModel.errorResource = UtilsStringR.error_fail_to_load_image to it
                },
                loading = { viewModel.loading },
                sections = { viewModel.sections },
                saving = { viewModel.saving },
                onClickSave = { viewModel.onClickSave(navController) },
                errorRes = { viewModel.errorResource },
                onErrorDismiss = { viewModel.errorResource = null },
            )
        }
    }

    private fun editSelected(
        navController: NavHostController,
        values: Map<Int, EntryGridModel>,
    ) {
        val entryIds = values.map { it.value.id }
        navController.navigate(
            NavDestinations.MULTI_EDIT +
                    "?entry_ids=${entryIds.joinToString(",")}"
        )
    }

    private fun NavGraphBuilder.entryDetailsComposable(
        block: @Composable (id: String, imageFile: File?, imageRatio: Float) -> Unit
    ) = composable(
        NavDestinations.ENTRY_DETAILS +
                "?entry_id={entry_id}" +
                "&entry_image_file={entry_image_file}" +
                "&entry_image_ratio={entry_image_ratio}",
        arguments = listOf(
            navArgument("entry_id") {
                type = NavType.StringType
                nullable = false
            },
            navArgument("entry_image_file") {
                type = NavType.StringType
                nullable = true
            },
            navArgument("entry_image_ratio") {
                type = NavType.FloatType
            },
        )
    ) {
        val arguments = it.arguments!!
        val id = arguments.getString("entry_id")!!
        val imageFile = arguments.getString("entry_image_file")?.let(::File)
        val imageRatio = arguments.getFloat("entry_image_ratio", 1f)
        block(id, imageFile, imageRatio)
    }

    private fun NavHostController.navToEntryDetails(entry: EntryGridModel) {
        val imageRatio = entry.imageWidthToHeightRatio
        val imageFileParameter = "&entry_image_file=${entry.localImageFile?.toPath()}"
            .takeIf { entry.localImageFile != null }
            .orEmpty()
        navigate(
            NavDestinations.ENTRY_DETAILS
                    + "?entry_id=${entry.id}"
                    + "&entry_image_ratio=$imageRatio"
                    + imageFileParameter
        )
    }

    @MainThread
    private fun openInternalImage(file: File) {
        val imageUri = FileProvider.getUriForFile(
            this@MainActivity,
            "$packageName.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(imageUri, "image/*")
        }

        val chooserIntent = Intent.createChooser(
            intent,
            getString(R.string.art_entry_open_full_image_content_description)
        )
        startActivity(chooserIntent)
    }
}