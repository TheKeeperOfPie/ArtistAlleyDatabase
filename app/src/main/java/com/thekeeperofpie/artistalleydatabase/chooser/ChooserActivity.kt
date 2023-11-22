package com.thekeeperofpie.artistalleydatabase.chooser

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.withResumed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.mxalbert.sharedelements.SharedElementsRoot
import com.thekeeperofpie.artistalleydatabase.navigation.NavDestinations
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChooserActivity : ComponentActivity() {

    @Inject
    lateinit var settings: SettingsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val action = intent.action
        if (action != Intent.ACTION_GET_CONTENT && action != Intent.ACTION_PICK) {
            finish()
            return
        }

        val allowMultiple = intent.extras?.getBoolean(Intent.EXTRA_ALLOW_MULTIPLE) ?: false

        setContent {
            val navController = rememberNavController()
            ArtistAlleyDatabaseTheme(settings = settings, navHostController = navController) {
                Surface {
                    SharedElementsRoot {
                        NavHost(
                            navController = navController,
                            startDestination = NavDestinations.HOME
                        ) {
                            composable(NavDestinations.HOME) {
                                val viewModel = hiltViewModel<ChooserViewModel>()
                                ChooserScreen(
                                    query = { viewModel.query.orEmpty() },
                                    entriesSize = { viewModel.entriesSize },
                                    onQueryChange = viewModel::onQuery,
                                    // TODO: Migrate to section search
                                    options = { emptyList() },
                                    onOptionChange = {},
                                    entries = { viewModel.results.collectAsLazyPagingItems() },
                                    selectedItems = { viewModel.selectedEntries.keys },
                                    onClickEntry = { index, entry ->
                                        if (allowMultiple) {
                                            viewModel.selectEntry(index, entry)
                                        } else {
                                            viewModel.getResult(entry)?.let {
                                                setResult(RESULT_OK, it)
                                                finish()
                                            }
                                        }
                                    },
                                    onLongClickEntry = { index, entry ->
                                        if (allowMultiple) {
                                            viewModel.selectEntry(index, entry)
                                        }
                                    },
                                    onClickClear = viewModel::clearSelected,
                                    onClickSelect = {
                                        viewModel.getResults()?.let {
                                            setResult(RESULT_OK, it)
                                            finish()
                                        }
                                    },
                                )

                                LaunchedEffect(true) {
                                    viewModel.viewModelScope.launch(Dispatchers.Main) {
                                        it.withResumed {
                                            viewModel.viewModelScope.launch(Dispatchers.Main) {
                                                viewModel.invalidate()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
