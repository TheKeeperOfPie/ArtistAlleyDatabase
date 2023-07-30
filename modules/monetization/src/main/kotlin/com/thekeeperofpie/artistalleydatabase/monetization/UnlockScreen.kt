package com.thekeeperofpie.artistalleydatabase.monetization

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen.AdsTier
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen.FreeTier
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen.SubscriptionTier

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object UnlockScreen {

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        viewModel: UnlockScreenViewModel = hiltViewModel(),
        onClickSettings: () -> Unit,
        bottomNavigationState: BottomNavigationState? = null,
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
                monetizationProvider.error = null
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
            val pagerState = rememberPagerState(pageCount = { 3 })
            val adsEnabled by viewModel.adsEnabled.collectAsState()
            val subscribed by viewModel.subscribed.collectAsState(false)
            Box {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        top = 24.dp,
                        bottom = 88.dp,
                    ),
                    pageSpacing = 16.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    when (it) {
                        0 -> FreeTier()
                        1 -> AdsTier(
                            adsEnabled = { adsEnabled },
                            onChangeEnableAds = {
                                if (it) {
                                    monetizationProvider?.requestEnableAds()
                                        ?: viewModel.enableAdsDebug()
                                } else {
                                    monetizationProvider?.revokeAds()
                                        ?: viewModel.disableAdsDebug()
                                }
                            },
                            subscribed = { subscribed },
                        )
                        2 -> SubscriptionTier(subscribed = { subscribed })
                    }
                }

                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    pageCount = pagerState.pageCount,
                    modifier = Modifier
                        .padding(bottom = 104.dp)
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }

    @Composable
    private fun Tier(@StringRes headerTextRes: Int, content: @Composable ColumnScope.() -> Unit) {
        val colorPrimary = MaterialTheme.colorScheme.primary
        val cardBorder = remember { BorderStroke(2.dp, colorPrimary) }
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            border = cardBorder,
        ) {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier
                    .fillMaxSize()
                    .fadingEdgeBottom(firstStop = 0.9f)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(headerTextRes),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier
                            .padding(start = 32.dp, end = 32.dp, top = 32.dp, bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    // TODO: Move strings to resources
                    content()

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    @Composable
    fun FreeTier() {
        Tier(R.string.monetization_feature_tier_free_header) {
            BulletPoint(
                type = SupportedType.SUPPORTED,
                text = "No ads"
            )

            BulletPoint(
                type = SupportedType.SUPPORTED,
                text = "Anime/manga search and filter"
            )
            BulletPoint(
                type = SupportedType.SUPPORTED,
                text = "View details for anime, manga, characters, staff, etc."
            )
            BulletPoint(
                type = SupportedType.SUPPORTED,
                text = "News from AnimeNewsNetwork and Crunchyroll"
            )
            BulletPoint(type = SupportedType.SUPPORTED, text = "Global user activity")
            BulletPoint(type = SupportedType.UNSUPPORTED, text = "AniList account log in")
            BulletPoint(
                type = SupportedType.UNSUPPORTED,
                text = "Experimental database/catalog features"
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    @Composable
    fun AdsTier(
        adsEnabled: () -> Boolean,
        onChangeEnableAds: (Boolean) -> Unit,
        subscribed: () -> Boolean,
    ) {
        Tier(R.string.monetization_feature_tier_ads_header) {
            BulletPoint(
                type = SupportedType.UNSUPPORTED,
                text = "No ads"
            )

            BulletPoint(
                type = SupportedType.FROM_PREVIOUS_TIER,
                text = "Anime/manga search and filter, view details, news feeds, ..."
            )
            BulletPoint(type = SupportedType.SUPPORTED, text = "AniList account log in")
            BulletPoint(
                type = SupportedType.SUPPORTED,
                text = "Edit, rate, favorite anime/manga/etc."
            )
            BulletPoint(
                type = SupportedType.SUPPORTED,
                text = "Search more types like characters, staff, studios, users"
            )
            BulletPoint(type = SupportedType.SUPPORTED, text = "Watching/reading and user lists")
            BulletPoint(type = SupportedType.SUPPORTED, text = "Following and global user activity")
            BulletPoint(
                type = SupportedType.UNSUPPORTED,
                text = "Experimental database/catalog features"
            )

            if (subscribed()) {
                Text(
                    text = stringResource(R.string.monetization_subscribed_ads_not_needed),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            } else {
                val adsEnabled = adsEnabled()
                if (adsEnabled) {
                    Text(
                        text = stringResource(R.string.monetization_enable_ads_completed),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                Button(
                    onClick = { onChangeEnableAds(!adsEnabled) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(
                            if (adsEnabled) {
                                R.string.monetization_disable_ads_button
                            } else {
                                R.string.monetization_enable_ads_button
                            }
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    @Composable
    fun SubscriptionTier(subscribed: () -> Boolean) {
        Tier(R.string.monetization_feature_tier_subscription_header) {
            BulletPoint(
                type = SupportedType.FROM_PREVIOUS_TIER,
                text = "No ads"
            )

            BulletPoint(
                type = SupportedType.FROM_PREVIOUS_TIER,
                text = "Anime/manga search and filter, view details, news feeds, ...",
            )
            BulletPoint(
                type = SupportedType.FROM_PREVIOUS_TIER,
                text = "AniList account log in, edit entries/lists, comprehensive search, ...",
            )
            BulletPoint(
                type = SupportedType.SUPPORTED,
                text = "Experimental database/catalog features",
            )

            BulletPoint(
                type = SupportedType.SUPPORTED,
                text = "Track art prints/CDs/merch from anime conventions",
            )

            BulletPoint(
                type = SupportedType.SUPPORTED,
                text = "Tag and search metadata for artist, convention source, sizing, characters, media, etc.",
            )

            val subscriptionProvider = LocalSubscriptionProvider.current
            if (subscriptionProvider == null) {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                ) {
                    Text(text = stringResource(R.string.monetization_subscription_not_supported))
                }
            } else {
                val error by subscriptionProvider.error.collectAsState()
                val errorText = error?.let { stringResource(it.first) }
                if (errorText != null) {
                    val errorColor = MaterialTheme.colorScheme.error
                    OutlinedCard(
                        border = remember { BorderStroke(width = 1.dp, color = errorColor) },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                    ) {
                        Text(
                            text = errorText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = errorColor,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }

                LaunchedEffect(Unit) { subscriptionProvider.loadSubscriptionDetails() }

                val subscriptionDetails by subscriptionProvider.subscriptionDetails.collectAsState()
                val subscription = subscriptionDetails.result
                val subscribed = subscribed()
                if (subscribed) {
                    Text(
                        text = stringResource(R.string.monetization_subscribe_completed),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                val uriHandler = LocalUriHandler.current
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Button(
                        enabled = subscribed || subscription != null,
                        onClick = {
                            if (subscribed) {
                                uriHandler.openUri(
                                    subscriptionProvider.getManageSubscriptionUrl(subscription)
                                )
                            } else if (subscription != null) {
                                subscriptionProvider.requestSubscribe(subscription)
                            }
                        },
                    ) {
                        Text(
                            text = stringResource(
                                if (subscribed) {
                                    R.string.monetization_manage_subscription_button
                                } else {
                                    R.string.monetization_subscribe_button
                                }
                            )
                        )
                    }

                    if (!subscribed && subscription == null) {
                        CircularProgressIndicator()
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    @Composable
    private fun BulletPoint(type: SupportedType, text: String, modifier: Modifier = Modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 10.dp)
                .then(modifier)
        ) {
            Icon(
                imageVector = when (type) {
                    SupportedType.FROM_PREVIOUS_TIER,
                    SupportedType.SUPPORTED,
                    -> Icons.Filled.Check
                    SupportedType.UNSUPPORTED -> Icons.Filled.Close
                },
                contentDescription = stringResource(
                    when (type) {
                        SupportedType.FROM_PREVIOUS_TIER,
                        SupportedType.SUPPORTED,
                        -> R.string.monetization_bullet_point_supported_icon_content_description
                        SupportedType.UNSUPPORTED -> R.string.monetization_bullet_point_not_supported_icon_content_description
                    }
                ),
                tint = when (type) {
                    SupportedType.FROM_PREVIOUS_TIER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    SupportedType.SUPPORTED -> MaterialTheme.colorScheme.primary
                    SupportedType.UNSUPPORTED -> MaterialTheme.colorScheme.error
                }
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }

    private enum class SupportedType {
        FROM_PREVIOUS_TIER, SUPPORTED, UNSUPPORTED,
    }
}

@Composable
@Preview
private fun PreviewFree() {
    FreeTier()
}

@Composable
@Preview
private fun PreviewAds() {
    AdsTier(adsEnabled = { false }, onChangeEnableAds = {}, subscribed = { false })
}

@Composable
@Preview
private fun PreviewSubscription() {
    SubscriptionTier(subscribed = { false })
}
