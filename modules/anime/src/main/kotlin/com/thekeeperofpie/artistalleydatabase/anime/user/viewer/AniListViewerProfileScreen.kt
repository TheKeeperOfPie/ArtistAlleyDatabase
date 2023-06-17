package com.thekeeperofpie.artistalleydatabase.anime.user.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState

object AniListViewerProfileScreen {

    @Composable
    operator fun invoke(
        needAuth: @Composable () -> Boolean,
        onClickAuth: () -> Unit,
        onSubmitAuthToken: (String) -> Unit,
        navigationCallback: AnimeNavigator.NavigationCallback,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        if (needAuth()) {
            AuthPrompt(onClickAuth = onClickAuth, onSubmitAuthToken = onSubmitAuthToken)
        } else {
            val viewModel = hiltViewModel<AniListUserViewModel>()
                .apply { initialize(null) }
            AniListUserScreen(
                viewModel = viewModel,
                navigationCallback = navigationCallback,
                bottomNavigationState = bottomNavigationState,
                showLogOut = true,
            )
        }
    }

    @Composable
    private fun AuthPrompt(onClickAuth: () -> Unit, onSubmitAuthToken: (String) -> Unit) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                Text(
                    stringResource(R.string.anime_auth_prompt_label),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                )

                Text(
                    stringResource(R.string.anime_auth_prompt_text),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                var showWhy by remember { mutableStateOf(false) }
                AnimatedVisibility(visible = showWhy) {
                    Text(
                        stringResource(R.string.anime_auth_prompt_why),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedVisibility(
                        visible = !showWhy,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally(),
                    ) {
                        Button(onClick = { showWhy = true }) {
                            Text(stringResource(R.string.anime_auth_button_why))
                        }
                    }

                    FilledTonalButton(onClick = onClickAuth) {
                        Text(stringResource(R.string.anime_auth_button_log_in))
                    }
                }

                Text(
                    stringResource(R.string.anime_auth_prompt_paste),
                    modifier = Modifier.padding(top = 20.dp)
                )

                var value by remember { mutableStateOf("") }
                TextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier
                        .sizeIn(minWidth = 200.dp, minHeight = 200.dp)
                        .padding(16.dp),
                )

                FilledTonalButton(onClick = {
                    val token = value
                    value = ""
                    onSubmitAuthToken(token)
                }) {
                    Text(stringResource(UtilsStringR.confirm))
                }
            }
        }
    }
}
