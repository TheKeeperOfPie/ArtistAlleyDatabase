package com.thekeeperofpie.artistalleydatabase.chooser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.mxalbert.sharedelements.SharedElementsRoot
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooserActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val action = intent.action
        if (action != Intent.ACTION_GET_CONTENT && action != Intent.ACTION_PICK) {
            finish()
            return
        }

        val allowMultiple = intent.extras?.getBoolean(Intent.EXTRA_ALLOW_MULTIPLE) ?: false

        setContent {
            ArtistAlleyDatabaseTheme {
                Surface {
                    SharedElementsRoot {
                        val viewModel = hiltViewModel<ChooserViewModel>()
                        ChooserScreen(
                            query = { viewModel.query.collectAsState().value?.query.orEmpty() },
                            onQueryChange = viewModel::onQuery,
                            options = { viewModel.options },
                            onOptionChanged = { viewModel.refreshQuery() },
                            entries = { viewModel.results.collectAsLazyPagingItems() },
                            selectedItems = { viewModel.selectedEntries.keys },
                            onClickEntry = { index, entry ->
                                if (allowMultiple) {
                                    viewModel.selectEntry(index, entry)
                                } else {
                                    viewModel.getResult(entry)?.let {
                                        setResult(Activity.RESULT_OK, it)
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
                                    setResult(Activity.RESULT_OK, it)
                                    finish()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}