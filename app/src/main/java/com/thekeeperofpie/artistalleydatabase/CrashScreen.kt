package com.thekeeperofpie.artistalleydatabase

import android.content.Intent
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider

@OptIn(ExperimentalMaterial3Api::class)
object CrashScreen {

    @Composable
    operator fun invoke(
        settings: SettingsProvider,
        onClickBack: () -> Unit,
    ) {
        val crash by settings.lastCrash.collectAsState()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(R.string.crash_title))
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = onClickBack) },
                    actions = {
                        val shareTitle = stringResource(R.string.crash_share_chooser_title)
                        val shareIntent = remember(shareTitle) {
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, crash)
                                type = "text/plain"
                            }
                            Intent.createChooser(intent, shareTitle)
                        }
                        val context = LocalContext.current
                        IconButton(onClick = { context.startActivity(shareIntent) }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(
                                    R.string.crash_share_icon_content_description
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
                    text = stringResource(R.string.crash_intro),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )

                val uriHandler = LocalUriHandler.current
                FilledTonalButton(
                    onClick = { uriHandler.openUri(BuildConfig.discordServerInviteLink) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = stringResource(R.string.crash_open_discord))
                }

                SelectionContainer {
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        Text(
                            text = crash,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
