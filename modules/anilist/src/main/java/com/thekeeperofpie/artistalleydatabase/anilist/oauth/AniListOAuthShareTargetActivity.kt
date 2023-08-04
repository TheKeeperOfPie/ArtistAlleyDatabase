package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anilist.R
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class AniListOAuthShareTargetActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: intent.dataString
        setContent {
            Theme {
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Scaffold(
                        topBar = {
                            AppBar(text = stringResource(UtilsStringR.app_name))
                        },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(it)
                                .padding(16.dp),
                        ) {
                            val viewModel = hiltViewModel<AniListOAuthViewModel>()
                                .apply { initialize(text) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val state = viewModel.state
                                when (state) {
                                    // TODO: Can this just be hidden into a translucent Activity
                                    //  without showing the user anything?
                                    AniListOAuthViewModel.State.Done,
                                    AniListOAuthViewModel.State.Loading -> {
                                        CircularProgressIndicator()
                                        Text(
                                            stringResource(R.string.aniList_oAuth_loading),
                                            Modifier.padding(10.dp)
                                        )
                                    }
                                    is AniListOAuthViewModel.State.Error -> Error(state)
                                }

                                LaunchedEffect(state) {
                                    if (state == AniListOAuthViewModel.State.Done) {
                                        closeAndFinish()
                                    }
                                }

                                HorizontalDivider()
                                TextButton(
                                    onClick = { closeAndFinish() },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(text = stringResource(R.string.aniList_oAuth_close_button))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Theme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
    ) {
        val context = LocalContext.current

        val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) darkColorScheme() else lightColorScheme()
        }

        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(
                    (view.context as Activity).window,
                    view
                ).isAppearanceLightStatusBars = darkTheme
            }
        }

        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }

    @Composable
    private fun Error(state: AniListOAuthViewModel.State.Error) {
        Text(
            text = stringResource(state.textRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 10.dp,
            ),
        )

        if (state.exception != null) {
            Text(
                text = state.exception.stackTraceToString(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 10.dp,
                ),
            )
        }
    }

    private fun closeAndFinish() {
        startActivity(
            Intent(this, AniListOAuthTrampolineActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
        finish()
    }
}
