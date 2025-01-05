package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.crash_intro
import artistalleydatabase.modules.utils_compose.generated.resources.crash_open_discord
import artistalleydatabase.modules.utils_compose.generated.resources.crash_share_icon_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.crash_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object CrashScreen {

    @Composable
    operator fun invoke(
        crash: @Composable () -> String,
        onClickBack: () -> Unit,
        onClickShare: (crash: String) -> Unit,
        onClickOpenDiscord: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(Res.string.crash_title))
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = onClickBack) },
                    actions = {
                        val crash = crash()
                        IconButton(onClick = { onClickShare(crash) }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(
                                    Res.string.crash_share_icon_content_description
                                )
                            )
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(Res.string.crash_intro),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )

                FilledTonalButton(
                    onClick = onClickOpenDiscord,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = stringResource(Res.string.crash_open_discord))
                }

                SelectionContainer {
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        Text(
                            text = crash(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
