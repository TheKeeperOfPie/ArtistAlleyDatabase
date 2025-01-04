package com.thekeeperofpie.artistalleydatabase.chooser

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.withResumed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.thekeeperofpie.artistalleydatabase.ApplicationComponent
import com.thekeeperofpie.artistalleydatabase.navigation.NavDestinations
import com.thekeeperofpie.artistalleydatabase.ui.theme.AndroidTheme
import com.thekeeperofpie.artistalleydatabase.utils.ComponentProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
class ChooserActivity : ComponentActivity() {

    private val applicationComponent by lazy {
        (application as ComponentProvider).singletonComponent<ApplicationComponent>()
    }

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
            AndroidTheme(
                settings = applicationComponent.settingsProvider,
                navHostController = navController,
            ) {
                Surface {
                    SharedTransitionLayout {
                        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                            NavHost(
                                navController = navController,
                                startDestination = NavDestinations.HOME
                            ) {
                                sharedElementComposable(NavDestinations.HOME) {
                                    val viewModel =
                                        viewModel { applicationComponent.chooserViewModel() }
                                    ChooserScreen(
                                        query = { viewModel.query.orEmpty() },
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
                                                    // TODO: This was removed for multiplatform
//                                                    viewModel.invalidate()
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
}
