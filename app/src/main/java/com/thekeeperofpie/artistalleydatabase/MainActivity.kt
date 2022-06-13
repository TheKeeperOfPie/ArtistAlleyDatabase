package com.thekeeperofpie.artistalleydatabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Dimension
import com.mxalbert.sharedelements.SharedElementsRoot
import com.thekeeperofpie.artistalleydatabase.add.AddEntryViewModel
import com.thekeeperofpie.artistalleydatabase.add.AddScreen
import com.thekeeperofpie.artistalleydatabase.detail.DetailScreen
import com.thekeeperofpie.artistalleydatabase.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
}