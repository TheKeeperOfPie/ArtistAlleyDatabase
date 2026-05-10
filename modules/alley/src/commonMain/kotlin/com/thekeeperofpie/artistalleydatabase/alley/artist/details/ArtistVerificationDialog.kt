package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_action_close
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_action_is_this_you
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_prompt_copy_discord_username
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_prompt_dm_on_bluesky
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_prompt_dm_on_instagram
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_prompt_dm_on_x
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_prompt_intro
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_prompt_option_dm_social
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_prompt_option_dm_social_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_prompt_option_login_discord
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_prompt_option_login_discord_button
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.links.Logo
import com.thekeeperofpie.artistalleydatabase.alley.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.Chat
import com.thekeeperofpie.artistalleydatabase.utils_compose.FilledTonalButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIcon
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtistVerificationDialog(artist: ArtistEntry, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.alley_artist_verification_action_close))
            }
        },
        title = { Text(stringResource(Res.string.alley_artist_verification_action_is_this_you)) },
        text = {
            Column {
                Text(
                    stringResource(
                        Res.string.alley_artist_verification_prompt_intro,
                        AlleyUtils.primaryContactDiscordUsername,
                    )
                )

                Spacer(Modifier.height(16.dp))

                val booth = artist.booth
                if (booth != null) {
                    var discordExpanded by remember { mutableStateOf(false) }
                    HeaderRow(
                        expanded = { discordExpanded },
                        icon = Logo.DISCORD.icon,
                        text = {
                            Text(stringResource(Res.string.alley_artist_verification_prompt_option_login_discord))
                        },
                        onClick = { discordExpanded = !discordExpanded },
                    )
                    if (discordExpanded) {
                        Text(
                            text = buildAnnotatedString {
                                appendLine(
                                    "By using Discord's connections feature, we can read your linked social " +
                                            "accounts and grant you access. Note this works even if " +
                                            "the account is hidden on your profile."
                                )
                                appendLine()
                                append(
                                    "This will only ask for the connections permission and will never " +
                                            "ask for your password or other personal information. You can also " +
                                            "use the bot inside the "
                                )
                                withLink(LinkAnnotation.Url(BuildKonfig.serverUrl)) {
                                    append("AX Discord server")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        val uriHandler = LocalUriHandler.current
                        FilledTonalButton(
                            icon = Logo.DISCORD.icon,
                            text = stringResource(Res.string.alley_artist_verification_prompt_option_login_discord_button),
                            onClick = {
                                uriHandler.openUri(BuildKonfig.artistVerifyUrl + "&state=$booth")
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    HorizontalDivider()
                }

                var dmExpanded by remember { mutableStateOf(false) }
                HeaderRow(
                    expanded = { dmExpanded },
                    icon = Icons.AutoMirrored.Default.Chat,
                    text = {
                        Text(stringResource(Res.string.alley_artist_verification_prompt_option_dm_social))
                    },
                    onClick = { dmExpanded = !dmExpanded },
                )
                if (dmExpanded) {
                    Text(
                        text = stringResource(
                            Res.string.alley_artist_verification_prompt_option_dm_social_description,
                            BuildKonfig.authorTwoUsername,
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    ) {
                        val uriHandler = LocalUriHandler.current
                        val socialLinkModels = artist.socialLinkModels
                        val hasBluesky = socialLinkModels.any { it.logo == Logo.BLUESKY }
                        val hasInstagram = socialLinkModels.any { it.logo == Logo.INSTAGRAM }
                        val hasX = socialLinkModels.any { it.logo == Logo.X }
                        val hasAny = hasBluesky || hasInstagram || hasX
                        if (hasBluesky || !hasAny) {
                            FilledTonalButton(
                                icon = Logo.BLUESKY.icon,
                                text = stringResource(Res.string.alley_artist_verification_prompt_dm_on_bluesky),
                                onClick = { uriHandler.openUri(AlleyUtils.contactLinkBluesky) },
                            )
                        }
                        if (hasInstagram || !hasAny) {
                            FilledTonalButton(
                                icon = Logo.INSTAGRAM.icon,
                                text = stringResource(Res.string.alley_artist_verification_prompt_dm_on_instagram),
                                onClick = { uriHandler.openUri(AlleyUtils.contactLinkInstagram) },
                            )
                        }
                        if (hasX || !hasAny) {
                            FilledTonalButton(
                                icon = Logo.X.icon,
                                text = stringResource(Res.string.alley_artist_verification_prompt_dm_on_x),
                                onClick = { uriHandler.openUri(AlleyUtils.contactLinkX) },
                            )
                        }

                        val clipboardManager = LocalClipboardManager.current
                        FilledTonalButton(
                            icon = Logo.DISCORD.icon,
                            text = stringResource(Res.string.alley_artist_verification_prompt_copy_discord_username),
                            onClick = {
                                clipboardManager.setText(
                                    AnnotatedString(
                                        AlleyUtils.primaryContactDiscordUsername.removePrefix("@")
                                    )
                                )
                            },
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun HeaderRow(
    expanded: () -> Boolean,
    icon: ImageVector,
    text: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Box(modifier = Modifier.weight(1f)) {
            text()
        }
        TrailingDropdownIcon(expanded = expanded(), contentDescription = null)
    }
}
