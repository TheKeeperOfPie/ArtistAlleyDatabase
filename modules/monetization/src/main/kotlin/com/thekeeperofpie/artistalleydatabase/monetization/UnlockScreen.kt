package com.thekeeperofpie.artistalleydatabase.monetization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.conditionally

@OptIn(ExperimentalMaterial3Api::class)
object UnlockScreen {

    private val MIN_WIDTH = 300.dp

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        viewModel: UnlockScreenViewModel = hiltViewModel(),
        onClickSettings: () -> Unit,
        bottomNavigationState: BottomNavigationState?,
    ) {
        // TODO: Show error message if monetization not available
        val monetizationProvider = LocalMonetizationProvider.current
        val snackbarHostState = remember { SnackbarHostState() }
        val error = monetizationProvider?.error?.let { stringResource(it.first) }
        LaunchedEffect(error) {
            if (error != null) {
                snackbarHostState.showSnackbar(
                    message = error,
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                )
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.monetization_feature_tiers_header),
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        if (upIconOption != null) {
                            UpIconButton(option = upIconOption)
                        }
                    },
                    actions = {
                        IconButton(onClick = onClickSettings) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(
                                    R.string.monetization_settings_content_description
                                )
                            )
                        }
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier.padding(
                        bottom = bottomNavigationState?.bottomOffsetPadding() ?: 0.dp
                    )
                )
            },
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
                        .widthIn(min = MIN_WIDTH)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    TierSection(
                        header = stringResource(R.string.monetization_feature_tier_free_header),
                        body = stringResource(R.string.monetization_feature_tier_free_body),
                    )

                    TierSection(
                        header = stringResource(R.string.monetization_feature_tier_ads_header),
                        body = stringResource(R.string.monetization_feature_tier_ads_body),
                    )

                    val adsEnabled by viewModel.adsEnabled.collectAsState()
                    if (adsEnabled) {
                        Button(onClick = {}, enabled = false) {
                            Text(
                                text = stringResource(
                                    R.string.monetization_enable_ads_button_completed
                                )
                            )
                        }
                    } else {
                        Button(onClick = {
                            monetizationProvider?.requestEnableAds() ?: viewModel.enableAdsDebug()
                        }) {
                            Text(text = stringResource(R.string.monetization_enable_ads_button))
                        }
                    }

                    TierSection(
                        header = stringResource(R.string.monetization_feature_tier_subscription_header),
                        body = stringResource(R.string.monetization_feature_tier_subscription_body),
                    )

                    val subscribed by viewModel.subscribed.collectAsState()
                    if (subscribed) {
                        Button(onClick = {}, enabled = false) {
                            Text(
                                text = stringResource(
                                    R.string.monetization_subscribe_button_completed
                                )
                            )
                        }
                    } else {
                        Button(onClick = viewModel::onSubscribeClick) {
                            Text(text = stringResource(R.string.monetization_subscribe_button))
                        }
                    }

                    Spacer(Modifier.height(88.dp))
                }
            }
        }
    }

    @Composable
    private fun TierSection(header: String, body: String) {
        Text(
            text = header,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
        )

        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
