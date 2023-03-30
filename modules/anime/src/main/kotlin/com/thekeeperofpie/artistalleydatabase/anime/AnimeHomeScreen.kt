package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText

object AnimeHomeScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        onClickNav: () -> Unit,
        needAuth: () -> Boolean,
        onClickAuth: () -> Unit,
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = { },
        authTokenDebugPreview: () -> String? = { null },
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.anime_home_title),
                    onClickNav = onClickNav
                )
            },
            snackbarHost = {
                SnackbarErrorText(
                    errorRes()?.first,
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss
                )
            },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                if (needAuth()) {
                    AuthPrompt(onClickAuth)
                } else {
                    Text("Authed ${authTokenDebugPreview()}...")
                }
            }
        }
    }

    @Composable
    private fun AuthPrompt(onClickAuth: () -> Unit) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(id = R.string.anime_auth_prompt))
            TextButton(onClick = onClickAuth) {
                Text(stringResource(id = R.string.anime_auth_button))
            }
        }
    }
}