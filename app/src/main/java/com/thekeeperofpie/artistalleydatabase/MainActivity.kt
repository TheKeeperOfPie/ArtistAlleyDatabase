package com.thekeeperofpie.artistalleydatabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key.Companion.Home
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.mxalbert.sharedelements.SharedElementsRoot
import com.thekeeperofpie.artistalleydatabase.add.AddEntryViewModel
import com.thekeeperofpie.artistalleydatabase.add.AddScreen
import com.thekeeperofpie.artistalleydatabase.detail.DetailScreen
import com.thekeeperofpie.artistalleydatabase.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {

        private val NAV_DRAWER_ITEMS = listOf(Icons.Default.Home, Icons.Default.Create)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val selectedItem = remember { mutableStateOf(NAV_DRAWER_ITEMS[0]) }
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    NAV_DRAWER_ITEMS.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item, contentDescription = null) },
                            label = { Text(item.name) },
                            selected = item == selectedItem.value,
                            onClick = {
                                scope.launch { drawerState.close() }
                                selectedItem.value = item
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                },
                content = {
                    Home()
                }
            )
        }
    }

    @Composable
    private fun Home() {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = NavDestinations.SEARCH) {
                composable(NavDestinations.SEARCH) {
                    val viewModel = hiltViewModel<SearchViewModel>()
                    SearchScreen(
                        viewModel.query.collectAsState().value,
                        viewModel::onQuery,
                        viewModel.results.collectAsLazyPagingItems(),
                        onClickAddFab = {
                            navController.navigate(NavDestinations.ADD_ENTRY)
                        },
                        onClickEntry = { entry, widthToHeightRatio ->
                            viewModel.viewModelScope.launch(Dispatchers.Main) {
                                delay(150)
                                navController.navigate(
                                    NavDestinations.ENTRY_DETAILS +
                                            "?entry_id=${entry.value.id}" +
                                            "&entry_image_file=${entry.localImageFile.toPath()}" +
                                            "&entry_image_ratio=${widthToHeightRatio ?: 1f}"
                                )
                            }
                        },
                    )
                }

                composable(NavDestinations.ADD_ENTRY) {
                    val viewModel = hiltViewModel<AddEntryViewModel>()
                    AddScreen(
                        imageUri = viewModel.imageUri,
                        onImageSelected = { viewModel.imageUri = it },
                        onImageSelectError = {
                            viewModel.errorResource = R.string.error_fail_to_load_image to it
                        },
                        artistSection = viewModel.artistSection,
                        locationSection = viewModel.locationSection,
                        seriesSection = viewModel.seriesSection,
                        characterSection = viewModel.characterSection,
                        tagSection = viewModel.tagSection,
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
                    val arguments = it.arguments
                    val entryId = arguments!!.getString("entry_id")!!
                    val entryImageFile = arguments.getString("entry_image_file")
                    val entryImageRatio = arguments.getFloat("entry_image_ratio", 1f)
                    DetailScreen(entryId, entryImageFile?.let(::File), entryImageRatio)
                }
            }
        }
    }
}