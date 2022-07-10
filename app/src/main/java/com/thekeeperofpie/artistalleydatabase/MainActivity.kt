package com.thekeeperofpie.artistalleydatabase

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.thekeeperofpie.artistalleydatabase.add.AddEntryViewModel
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseViewModel
import com.thekeeperofpie.artistalleydatabase.browse.selection.BrowseSelectionScreen
import com.thekeeperofpie.artistalleydatabase.browse.selection.BrowseSelectionViewModel
import com.thekeeperofpie.artistalleydatabase.detail.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.detail.DetailsViewModel
import com.thekeeperofpie.artistalleydatabase.export.ExportScreen
import com.thekeeperofpie.artistalleydatabase.export.ExportViewModel
import com.thekeeperofpie.artistalleydatabase.home.HomeScreen
import com.thekeeperofpie.artistalleydatabase.home.HomeViewModel
import com.thekeeperofpie.artistalleydatabase.importing.ImportScreen
import com.thekeeperofpie.artistalleydatabase.importing.ImportViewModel
import com.thekeeperofpie.artistalleydatabase.navigation.NavDestinations
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArtistAlleyDatabaseTheme {
                Surface {
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val selectedItem = remember { mutableStateOf(NavDrawerItems.ITEMS[0]) }

                    fun onClickNav() = scope.launch { drawerState.open() }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            NavDrawerItems.ITEMS.forEach {
                                NavigationDrawerItem(
                                    icon = { Icon(it.icon, contentDescription = null) },
                                    label = { Text(stringResource(it.titleRes)) },
                                    selected = it == selectedItem.value,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        selectedItem.value = it
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }
                        },
                        content = {
                            when (selectedItem.value) {
                                NavDrawerItems.Home -> Home(::onClickNav)
                                NavDrawerItems.Browse -> BrowseScreen(::onClickNav)
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
                                        onToggleDryRun = { viewModel.dryRun = !viewModel.dryRun },
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
                    )
                }
            }
        }
    }

    @Composable
    private fun Home(onClickNav: () -> Unit) {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = NavDestinations.HOME) {
                composable(NavDestinations.HOME) {
                    val viewModel = hiltViewModel<HomeViewModel>()
                    HomeScreen(
                        onClickNav = onClickNav,
                        query = viewModel.query.collectAsState().value.value,
                        onQueryChange = viewModel::onQuery,
                        options = viewModel.options,
                        onOptionChanged = { viewModel.refreshQuery() },
                        entries = viewModel.results.collectAsLazyPagingItems(),
                        selectedItems = viewModel.selectedEntries.keys,
                        onClickAddFab = {
                            navController.navigate(NavDestinations.ADD_ENTRY)
                        },
                        onClickEntry = { index, entry ->
                            if (viewModel.selectedEntries.isNotEmpty()) {
                                viewModel.selectEntry(index, entry)
                            } else {
                                val entryImageRatio = entry.value.imageWidthToHeightRatio
                                navController.navigate(
                                    NavDestinations.ENTRY_DETAILS +
                                            "?entry_id=${entry.value.id}" +
                                            (entry.localImageFile
                                                ?.let { "&entry_image_file=${it.toPath()}" }
                                                ?: "") +
                                            "&entry_image_ratio=${entryImageRatio}"
                                )
                            }
                        },
                        onLongClickEntry = viewModel::selectEntry,
                        onClickClear = viewModel::clearSelected,
                        onConfirmDelete = viewModel::onDeleteSelected,
                    )
                }

                composable(NavDestinations.ADD_ENTRY) {
                    val viewModel = hiltViewModel<AddEntryViewModel>()
                    AddEntryScreen(
                        imageUris = viewModel.imageUris,
                        onImagesSelected = {
                            viewModel.imageUris.clear()
                            viewModel.imageUris.addAll(it)
                        },
                        onImageSelectError = {
                            viewModel.errorResource = R.string.error_fail_to_load_image to it
                        },
                        onImageSizeResult = viewModel::onImageSizeResult,
                        sections = viewModel.sections,
                        onClickSaveTemplate = viewModel::onClickSaveTemplate,
                        onClickSave = { viewModel.onClickSave(navController) },
                        errorRes = viewModel.errorResource,
                        onErrorDismiss = { viewModel.errorResource = null }
                    )
                }

                addDetailsScreen(navController)
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
                            navController.navigate(
                                "selection" +
                                        "?column=$column" +
                                        "&title=${value.text}" +
                                        "&query=${value.query}"
                            )
                        },
                    )
                }

                composable(
                    "selection" +
                            "?column={column}" +
                            "&title={title}" +
                            "&query={query}",
                    arguments = listOf(
                        navArgument("column") {
                            type = NavType.StringType
                            nullable = false
                        },
                        navArgument("title") {
                            type = NavType.StringType
                        },
                        navArgument("query") {
                            type = NavType.StringType
                        },
                    )
                ) {
                    val arguments = it.arguments!!
                    val column = ArtEntryColumn.valueOf(arguments.getString("column")!!)
                    val title = arguments.getString("title")!!
                    val query = arguments.getString("query")!!
                    val viewModel = hiltViewModel<BrowseSelectionViewModel>()
                    viewModel.initialize(column, query)
                    BrowseSelectionScreen(
                        title = { title },
                        loading = { viewModel.loading },
                        entries = viewModel.entries.collectAsLazyPagingItems(),
                        selectedItems = viewModel.selectedEntries.keys,
                        onClickEntry = { index, entry ->
                            if (viewModel.selectedEntries.isNotEmpty()) {
                                viewModel.selectEntry(index, entry)
                            } else {
                                val entryImageRatio = entry.value.imageWidthToHeightRatio
                                navController.navigate(
                                    NavDestinations.ENTRY_DETAILS +
                                            "?entry_id=${entry.value.id}" +
                                            (entry.localImageFile
                                                ?.let { "&entry_image_file=${it.toPath()}" }
                                                ?: "") +
                                            "&entry_image_ratio=${entryImageRatio}"
                                )
                            }
                        },
                        onLongClickEntry = viewModel::selectEntry,
                        onClickClear = viewModel::clearSelected,
                        onConfirmDelete = viewModel::onDeleteSelected,
                    )
                }

                addDetailsScreen(navController)
            }
        }
    }

    private fun NavGraphBuilder.addDetailsScreen(navController: NavHostController) {
        composable(
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
            val entryId = arguments.getString("entry_id")!!
            val entryImageFile = arguments.getString("entry_image_file")?.let(::File)
            val entryImageRatio = arguments.getFloat("entry_image_ratio", 1f)

            val viewModel = hiltViewModel<DetailsViewModel>()
            viewModel.initialize(entryId, entryImageRatio)

            DetailsScreen(
                entryId,
                entryImageFile,
                entryImageRatio,
                imageUri = viewModel.imageUri,
                onImageSelected = { viewModel.imageUri = it },
                onImageSelectError = {
                    viewModel.errorResource = R.string.error_fail_to_load_image to it
                },
                onImageSizeResult = viewModel::onImageSizeResult,
                onImageClickOpen = {
                    entryImageFile?.let {
                        val imageUri = FileProvider.getUriForFile(
                            this@MainActivity,
                            "$packageName.fileprovider",
                            it
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
                },
                areSectionsLoading = viewModel.areSectionsLoading,
                sections = viewModel.sections,
                onClickSave = { viewModel.onClickSave(navController) },
                errorRes = viewModel.errorResource,
                onErrorDismiss = { viewModel.errorResource = null },
                onConfirmDelete = { viewModel.onConfirmDelete(navController) }
            )
        }
    }
}