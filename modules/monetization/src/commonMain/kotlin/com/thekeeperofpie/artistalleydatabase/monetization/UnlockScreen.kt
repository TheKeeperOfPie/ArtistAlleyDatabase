package com.thekeeperofpie.artistalleydatabase.monetization

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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.monetization.generated.resources.Res
import artistalleydatabase.modules.monetization.generated.resources.monetization_bullet_point_not_supported_icon_content_description
import artistalleydatabase.modules.monetization.generated.resources.monetization_bullet_point_supported_icon_content_description
import artistalleydatabase.modules.monetization.generated.resources.monetization_disable_ads_button
import artistalleydatabase.modules.monetization.generated.resources.monetization_enable_ads_button
import artistalleydatabase.modules.monetization.generated.resources.monetization_feature_tier_ads_header
import artistalleydatabase.modules.monetization.generated.resources.monetization_feature_tier_free_header
import artistalleydatabase.modules.monetization.generated.resources.monetization_feature_tier_subscription_header
import artistalleydatabase.modules.monetization.generated.resources.monetization_feature_tiers_header
import artistalleydatabase.modules.monetization.generated.resources.monetization_manage_subscription_button
import artistalleydatabase.modules.monetization.generated.resources.monetization_settings_content_description
import artistalleydatabase.modules.monetization.generated.resources.monetization_subscribe_button
import artistalleydatabase.modules.monetization.generated.resources.monetization_subscribed_ads_not_needed
import artistalleydatabase.modules.monetization.generated.resources.monetization_subscription_not_supported
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen.AdsTier
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen.FreeTier
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen.SubscriptionTier
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeBottom
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object UnlockScreen {

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        viewModel: UnlockScreenViewModel,
        onClickSettings: (() -> Unit)?,
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

        if (bottomNavigationState != null) {
            SideEffect {
                bottomNavigationState.reset()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.monetization_feature_tiers_header),
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        if (upIconOption != null) {
                            UpIconButton(option = upIconOption)
                        }
                    },
                    actions = {
                        if (onClickSettings != null) {
                            IconButton(onClick = onClickSettings) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = stringResource(
                                        Res.string.monetization_settings_content_description
                                    )
                                )
                            }
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
        ) {
            val pagerState = rememberPagerState(pageCount = { 3 })
            val adsEnabled by viewModel.adsEnabled.collectAsState()
            val subscribed by viewModel.subscribed.collectAsState(false)
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 10.dp,
                    bottom = 24.dp + (bottomNavigationState?.bottomNavBarPadding() ?: 0.dp),
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
        }
    }

    @Composable
    private fun Tier(
        headerTextRes: StringResource,
        footer: @Composable ColumnScope.() -> Unit = {},
        content: @Composable ColumnScope.() -> Unit,
    ) {
        val colorPrimary = MaterialTheme.colorScheme.primary
        val cardBorder = remember { BorderStroke(2.dp, colorPrimary) }
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            border = cardBorder,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .weight(1f)
                        .fadingEdgeBottom(firstStop = 0.9f)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = stringResource(headerTextRes),
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .padding(horizontal = 32.dp, vertical = 10.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        // TODO: Move strings to resources
                        content()

                        Spacer(Modifier.height(16.dp))
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.primary)

                footer()
            }
        }
    }

    @Composable
    fun FreeTier() {
        Tier(Res.string.monetization_feature_tier_free_header) {
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
        Tier(
            headerTextRes = Res.string.monetization_feature_tier_ads_header,
            footer = {
                if (subscribed()) {
                    Text(
                        text = stringResource(Res.string.monetization_subscribed_ads_not_needed),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                } else {
                    val adsEnabled = adsEnabled()
                    Button(
                        onClick = { onChangeEnableAds(!adsEnabled) },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (adsEnabled) {
                                    Res.string.monetization_disable_ads_button
                                } else {
                                    Res.string.monetization_enable_ads_button
                                }
                            )
                        )
                    }
                }
            },
        ) {
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
            BulletPoint(type = SupportedType.SUPPORTED, text = "All user activity and forum access")
            BulletPoint(
                type = SupportedType.UNSUPPORTED,
                text = "Experimental database/catalog features"
            )
        }
    }

    @Composable
    fun SubscriptionTier(subscribed: () -> Boolean) {
        val subscriptionProvider = LocalSubscriptionProvider.current
        Tier(
            headerTextRes = Res.string.monetization_feature_tier_subscription_header,
            footer = {
                if (subscriptionProvider == null) {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp, bottom = 10.dp)
                    ) {
                        Text(text = stringResource(Res.string.monetization_subscription_not_supported))
                    }
                } else {
                    val error by subscriptionProvider.error.collectAsState()
                    val errorText = error?.let { stringResource(it.first) }
                    if (errorText != null) {
                        val errorColor = MaterialTheme.colorScheme.error
                        OutlinedCard(
                            border = remember { BorderStroke(width = 1.dp, color = errorColor) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
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
                    val uriHandler = LocalUriHandler.current
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        if (subscription != null) {
                            val cost = subscription.cost
                            val period = subscription.period
                            if (cost != null && period != null) {
                                // TODO: This doesn't handle > 12 months
                                val months = period.months
                                val periodText = if (months <= 1) {
                                    "month"
                                } else {
                                    "$months months"
                                }
                                Text(
                                    text = "$cost billed every $periodText until you cancel",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        }

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
                                        Res.string.monetization_manage_subscription_button
                                    } else {
                                        Res.string.monetization_subscribe_button
                                    }
                                )
                            )
                        }

                        if (!subscribed && subscription == null) {
                            CircularProgressIndicator()
                        }
                    }
                }
            },
        ) {
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
                        -> Res.string.monetization_bullet_point_supported_icon_content_description
                        SupportedType.UNSUPPORTED -> Res.string.monetization_bullet_point_not_supported_icon_content_description
                    }
                ),
                tint = when (type) {
                    SupportedType.FROM_PREVIOUS_TIER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
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
