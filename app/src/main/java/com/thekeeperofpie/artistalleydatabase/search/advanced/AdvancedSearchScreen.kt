package com.thekeeperofpie.artistalleydatabase.search.advanced

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.entry.EntryForm
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection

object AdvancedSearchScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        loading: () -> Boolean = { false },
        sections: () -> List<EntrySection>,
        onClickClear: () -> Unit,
        onClickSearch: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                Column {
                    AppBar(
                        text = stringResource(R.string.nav_drawer_search),
                        onClickNav = onClickNav
                    )
                }
            },
            modifier = Modifier.imePadding(),
        ) {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, true)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    EntryForm(loading, sections)
                }

                AnimatedVisibility(
                    visible = !loading(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    ButtonFooter(
                        R.string.clear to onClickClear,
                        R.string.search to onClickSearch,
                    )
                }
            }
        }
    }
}