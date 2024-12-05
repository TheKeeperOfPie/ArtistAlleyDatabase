package com.thekeeperofpie.artistalleydatabase.anime.user.viewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_auth_button_log_in
import artistalleydatabase.modules.anime.generated.resources.anime_auth_prompt_label
import artistalleydatabase.modules.anime.generated.resources.anime_auth_prompt_paste
import artistalleydatabase.modules.anime.generated.resources.anime_auth_prompt_text
import artistalleydatabase.modules.anime.generated.resources.anime_settings_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.confirm
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderValues
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import org.jetbrains.compose.resources.stringResource

object AniListViewerProfileScreen {

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        needsAuth: @Composable () -> Boolean,
        onClickAuth: () -> Unit,
        onSubmitAuthToken: (String) -> Unit,
        onClickSettings: () -> Unit,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        if (needsAuth()) {
            AuthPrompt(
                onClickAuth = onClickAuth,
                onSubmitAuthToken = onSubmitAuthToken,
                onClickSettings = onClickSettings,
                bottomNavigationState = bottomNavigationState,
            )
        } else {
            val animeComponent = LocalAnimeComponent.current
            val viewModel = viewModel {
                animeComponent.aniListUserViewModel(
                    createSavedStateHandle(),
                    AnimeDestination.MediaDetails.route,
                )
            }
            val headerValues = UserHeaderValues(null) { viewModel.entry?.user }
            AniListUserScreen(
                viewModel = viewModel,
                upIconOption = upIconOption,
                headerValues = headerValues,
                bottomNavigationState = bottomNavigationState,
                showLogOut = true,
                onClickSettings = onClickSettings,
            )
        }
    }

    @Composable
    private fun AuthPrompt(
        onClickAuth: () -> Unit,
        onSubmitAuthToken: (String) -> Unit,
        onClickSettings: (() -> Unit)?,
        bottomNavigationState: BottomNavigationState?,
    ) {
        Scaffold(
            modifier = Modifier.conditionally(bottomNavigationState != null) {
                nestedScroll(bottomNavigationState!!.nestedScrollConnection)
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .widthIn(min = 300.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        stringResource(Res.string.anime_auth_prompt_label),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 32.dp)
                    )

                    Text(
                        stringResource(Res.string.anime_auth_prompt_text),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .widthIn(min = 300.dp)
                            .width(IntrinsicSize.Min)
                    )

                    FilledTonalButton(onClick = onClickAuth) {
                        Text(stringResource(Res.string.anime_auth_button_log_in))
                    }

                    Text(
                        stringResource(Res.string.anime_auth_prompt_paste),
                        modifier = Modifier.padding(top = 20.dp)
                    )

                    var value by remember { mutableStateOf("") }
                    TextField(
                        value = value,
                        onValueChange = { value = it },
                        modifier = Modifier
                            .size(width = 200.dp, height = 200.dp)
                            .padding(16.dp),
                    )

                    FilledTonalButton(onClick = {
                        val token = value
                        value = ""
                        onSubmitAuthToken(token)
                    }) {
                        Text(stringResource(UtilsStrings.confirm))
                    }

                    Spacer(Modifier.height(88.dp))
                }

                if (onClickSettings != null) {
                    IconButton(
                        onClick = onClickSettings,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(
                                Res.string.anime_settings_content_description
                            )
                        )
                    }
                }
            }
        }
    }
}
