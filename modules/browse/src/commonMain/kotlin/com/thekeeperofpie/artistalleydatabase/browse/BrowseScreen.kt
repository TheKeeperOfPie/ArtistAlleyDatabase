package com.thekeeperofpie.artistalleydatabase.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import artistalleydatabase.modules.browse.generated.resources.Res
import artistalleydatabase.modules.browse.generated.resources.browse
import com.thekeeperofpie.artistalleydatabase.entry.EntryImage
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object BrowseScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        tabs: List<TabContent>,
        onClick: (tabContent: TabContent, value: BrowseEntryModel) -> Unit = { _, _ -> },
        onPageRequested: (page: Int) -> Unit = {},
    ) {
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collectLatest(onPageRequested)
        }
        val selectedTabIndex = pagerState.currentPage
        Scaffold(
            topBar = {
                Column {
                    val colors = TopAppBarDefaults.topAppBarColors()
                    AppBar(
                        text = stringResource(Res.string.browse),
                        colors = colors,
                        upIconOption = upIconOption,
                    )
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        val coroutineScope = rememberCoroutineScope()
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTabIndex == index,
                                text = { Text(stringResource(tab.textRes())) },
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                            )
                        }
                    }
                }
            },
        ) {
            HorizontalPager(
                state = pagerState,
                key = { tabs[it].id },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
            ) {
                val tab = tabs[it]
                val content = tab.content()
                LazyColumn(Modifier.fillMaxSize()) {
                    if (content is Either.Left) {
                        val items = content.value
                        items(items.size) {
                            val value = items[it]
                            EntryRow(
                                image = { value.image },
                                link = { value.link },
                                text = { value.text },
                                onClick = { onClick(tab, value) }
                            )
                        }
                    } else if (content is Either.Right) {
                        items(
                            count = content.value.itemCount,
                            key = content.value.itemKey { it.queryIdOrString.eitherValueUnchecked()!! },
                            contentType = content.value.itemContentType { "BrowseEntryModel" },
                        ) { index ->
                            val item = content.value[index]
                            EntryRow(
                                image = { item?.image },
                                link = { item?.link },
                                text = { item?.text ?: "" },
                                onClick = { item?.let { onClick(tab, it) } },
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun EntryRow(
        image: () -> String?,
        link: () -> String? = { null },
        text: () -> String,
        onClick: () -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            EntryImage(
                image = image,
                link = link,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .heightIn(min = 54.dp)
                    .width(42.dp)
            )
            Text(
                text = text(),
                modifier = Modifier
                    .weight(1f, true)
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
            )
        }
    }

    @Immutable
    data class TabContent(
        val id: String,
        val textRes: () -> StringResource,
        val content: @Composable () -> Either<List<BrowseEntryModel>,
                LazyPagingItems<BrowseEntryModel>>,
        val onSelected: (NavHostController, BrowseEntryModel) -> Unit,
    )
}
