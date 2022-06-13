package com.thekeeperofpie.artistalleydatabase.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.NavDestinations
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.ui.LazyStaggeredGrid
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
object SearchScreen {

    @Composable
    operator fun invoke(
        query: String = "",
        onQueryChange: (String) -> Unit = {},
        entries: LazyPagingItems<ArtEntryModel> =
            emptyFlow<PagingData<ArtEntryModel>>().collectAsLazyPagingItems(),
        onClickEntry: (entry: ArtEntryModel, widthToHeightRatio: Float?) -> Unit = { _, _ -> },
        onClickAddFab: () -> Unit = {}
    ) {
        ArtistAlleyDatabaseTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Chrome(query, onQueryChange, onClickAddFab) {
                    Content(entries, it, onClickEntry)
                }
            }
        }
    }

    @Composable
    private fun Chrome(
        query: String,
        onQueryChange: (String) -> Unit,
        onClickAddFab: () -> Unit,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        Scaffold(
            topBar = {
                TextField(
                    query,
                    placeholder = {
                        Text(stringResource(id = R.string.search))
                    },
                    onValueChange = onQueryChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onClickAddFab,
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.search_add_entry)
                    )
                }
            },
            content = content
        )
    }

    @Composable
    private fun Content(
        entries: LazyPagingItems<ArtEntryModel>,
        paddingValues: PaddingValues,
        onClickEntry: (entry: ArtEntryModel, widthToHeightRatio: Float?) -> Unit
    ) {
        LazyStaggeredGrid<ArtEntryModel>(
            2,
            Modifier.padding(paddingValues)
        ) {
            items(entries, key = { it.value.id }) {
                ArtEntry(it, onClickEntry)
            }
        }
    }

    @Composable
    private fun ArtEntry(
        entry: ArtEntryModel?,
        onClickEntry: (entry: ArtEntryModel, widthToHeightRatio: Float?) -> Unit
    ) {
        val imageModifier = Modifier.fillMaxWidth()
        if (entry == null) {
            Spacer(
                modifier = imageModifier
                    .background(Color.LightGray)
            )
        } else {
            SharedElement(
                key = "${entry.value.id}_image",
                screenKey = NavDestinations.SEARCH
            ) {
                val widthToHeightRatio = remember { mutableStateOf<Float?>(null) }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(entry.localImageFile)
                        .crossfade(true)
                        .memoryCacheKey("coil_memory_entry_image_search_${entry.value.id}")
                        .listener { _, result ->
                            widthToHeightRatio.value =
                                result.drawable.intrinsicHeight.coerceAtLeast(0)
                                    .toFloat() / result.drawable.intrinsicWidth.coerceAtLeast(1)
                        }
                        .build(),
                    contentDescription = stringResource(
                        R.string.art_entry_image_content_description
                    ),
                    contentScale = ContentScale.Fit,
                    modifier = imageModifier.clickable {
                        onClickEntry(
                            entry,
                            widthToHeightRatio.value
                        )
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    SearchScreen()
}