package com.thekeeperofpie.artistalleydatabase.browse

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.form.EntryImage
import kotlinx.coroutines.launch

object BrowseScreen {

    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        tabs: List<TabContent>,
        onClick: (tabContent: TabContent, value: BrowseEntryModel) -> Unit = { _, _ -> },
    ) {
        val pagerState = rememberPagerState()
        val selectedTabIndex = pagerState.currentPage
        Scaffold(
            topBar = {
                Column {
                    val colors = TopAppBarDefaults.smallTopAppBarColors()
                    AppBar(
                        text = stringResource(R.string.browse),
                        colors = colors,
                        onClickNav = onClickNav
                    )
                    TabRow(
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
                count = tabs.size,
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
            ) {
                LazyColumn(Modifier.fillMaxSize()) {
                    val tab = tabs[it]
                    val content = tab.content()
                    items(content.size) {
                        val value = content[it]
                        EntryRow(
                            image = { value.image },
                            link = { value.link },
                            text = { value.text },
                            onClick = { onClick(tab, value) }
                        )
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
        @StringRes val textRes: () -> Int,
        val content: () -> List<BrowseEntryModel>,
        val onSelected: (NavHostController, BrowseEntryModel) -> Unit,
    )
}