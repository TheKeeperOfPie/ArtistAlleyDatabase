package com.thekeeperofpie.artistalleydatabase

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.Dispatchers
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
                                NavDrawerItems.Home -> HomeScreen()
                                NavDrawerItems.Browse -> BrowseScreen()
                                NavDrawerItems.Import -> {
                                    val viewModel = hiltViewModel<ImportViewModel>()
                                    ImportScreen(
                                        uriString = viewModel.importUriString.orEmpty(),
                                        onUriStringEdit = { viewModel.importUriString = it },
                                        onContentUriSelected = {
                                            viewModel.importUriString = it?.toString()
                                        },
                                        onClickImport = {
                                            viewModel.onClickImport {
                                                selectedItem.value = NavDrawerItems.Home
                                            }
                                        },
                                        errorRes = viewModel.errorResource,
                                        onErrorDismiss = { viewModel.errorResource = null }
                                    )
                                }
                                NavDrawerItems.Export -> {
                                    val viewModel = hiltViewModel<ExportViewModel>()
                                    ExportScreen(
                                        uriString = { viewModel.exportUriString.orEmpty() },
                                        onUriStringEdit = { viewModel.exportUriString = it },
                                        onContentUriSelected = {
                                            viewModel.exportUriString = it?.toString()
                                        },
                                        userReadable = { viewModel.userReadable },
                                        onToggleUserReadable = {
                                            viewModel.userReadable = !viewModel.userReadable
                                        },
                                        onClickExport = viewModel::onClickExport,
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
    private fun HomeScreen() {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = NavDestinations.HOME) {
                composable(NavDestinations.HOME) {
                    val viewModel = hiltViewModel<HomeViewModel>()
                    HomeScreen(
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
                                viewModel.viewModelScope.launch(Dispatchers.Main) {
                                    val entryImageRatio = entry.value.imageWidthToHeightRatio
                                    navController.navigate(
                                        NavDestinations.ENTRY_DETAILS +
                                                "?entry_id=${entry.value.id}" +
                                                "&entry_image_file=${entry.localImageFile.toPath()}" +
                                                "&entry_image_ratio=${entryImageRatio}"
                                    )
                                }
                            }
                        },
                        onLongClickEntry = viewModel::selectEntry,
                        onClickClear = viewModel::clearSelected,
                        onClickDelete = viewModel::deleteSelected
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
                        onClickSave = { viewModel.onClickSave(navController) },
                        errorRes = viewModel.errorResource,
                        onErrorDismiss = { viewModel.errorResource = null }
                    )
                }

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
                    val entryImageFile = arguments.getString("entry_image_file")
                    val entryImageRatio = arguments.getFloat("entry_image_ratio", 1f)

                    val viewModel = hiltViewModel<DetailsViewModel>()
                    viewModel.initialize(entryId, entryImageRatio)

                    DetailsScreen(
                        entryId,
                        entryImageFile?.let(::File),
                        entryImageRatio,
                        imageUri = viewModel.imageUri,
                        onImageSelected = { viewModel.imageUri = it },
                        onImageSelectError = {
                            viewModel.errorResource = R.string.error_fail_to_load_image to it
                        },
                        onImageSizeResult = viewModel::onImageSizeResult,
                        areSectionsLoading = viewModel.areSectionsLoading,
                        sections = viewModel.sections,
                        onClickSave = { viewModel.onClickSave(navController) },
                        errorRes = viewModel.errorResource,
                        onErrorDismiss = { viewModel.errorResource = null },
                        showDeleteDialog = viewModel.showDeleteDialog,
                        onDismissDeleteDialog = { viewModel.showDeleteDialog = false },
                        onClickDelete = { viewModel.showDeleteDialog = true },
                        onConfirmDelete = { viewModel.onConfirmDelete(navController) }
                    )
                }
            }
        }
    }

    @Composable
    private fun BrowseScreen() {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = "browse") {
                composable("browse") {
                    val viewModel = hiltViewModel<BrowseViewModel>()
                    BrowseScreen(
                        tabs = viewModel.tabs,
                        onClick = { column, value ->
                            navController.navigate(
                                "selection" +
                                        "?column=$column" +
                                        "&value=$value"
                            )
                        },
                    )
                }

                composable(
                    "selection" +
                            "?column={column}" +
                            "&value={value}",
                    arguments = listOf(
                        navArgument("column") {
                            type = NavType.StringType
                            nullable = false
                        },
                        navArgument("value") {
                            type = NavType.StringType
                            nullable = true
                        },
                    )
                ) {
                    val arguments = it.arguments!!
                    val column = ArtEntryColumn.valueOf(arguments.getString("column")!!)
                    val value = arguments.getString("value")!!
                    val viewModel = hiltViewModel<BrowseSelectionViewModel>()
                    viewModel.initialize(column, value)
                    BrowseSelectionScreen(
                        title = { value },
                        loading = { viewModel.loading },
                        entries = viewModel.entries.collectAsLazyPagingItems(),
                    )
                }
            }
        }
    }
}