package com.thekeeperofpie.artistalleydatabase.alley.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_author_link
import artistalleydatabase.modules.alley.generated.resources.alley_server_link
import artistalleydatabase.modules.alley.generated.resources.alley_sheet_link
import com.thekeeperofpie.artistalleydatabase.alley.links.Logo
import com.thekeeperofpie.artistalleydatabase.alley.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconWithTooltip
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsScreen
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
internal object SettingsScreen {

    @Composable
    operator fun invoke(sections: List<SettingsSection>) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
            SettingsScreen(
                sections = sections,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                modifier = Modifier.widthIn(max = 1200.dp),
                customSection = {
                    when (it.id) {
                        "header" -> Header()
                        else -> throw IllegalArgumentException()
                    }
                }
            )
        }
    }

    @Composable
    private fun Header() {
        OutlinedCard(Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val colorScheme = MaterialTheme.colorScheme
                val text = remember(colorScheme) {
                    buildAnnotatedString {
                        append("Built by ")
                        withStyle(SpanStyle(color = colorScheme.primary)) {
                            withLink(LinkAnnotation.Url(BuildKonfig.authorUrl)) {
                                append(BuildKonfig.authorName)
                            }
                        }
                        append(" for the ")
                        withStyle(SpanStyle(color = colorScheme.primary)) {
                            withLink(LinkAnnotation.Url(BuildKonfig.serverUrl)) {
                                append(BuildKonfig.serverName)
                            }
                        }
                    }
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(Modifier.weight(1f))

                val uriHandler = LocalUriHandler.current

                IconWithTooltip(
                    imageVector = Logo.GITHUB.icon,
                    tooltipText = BuildKonfig.authorUrl,
                    onClick = { uriHandler.openUri(BuildKonfig.authorUrl) },
                    contentDescription = stringResource(Res.string.alley_author_link),
                )

                IconWithTooltip(
                    imageVector = Logo.DISCORD.icon,
                    tooltipText = BuildKonfig.serverUrl,
                    onClick = { uriHandler.openUri(BuildKonfig.serverUrl) },
                    contentDescription = stringResource(Res.string.alley_server_link),
                )

                IconWithTooltip(
                    imageVector = Icons.Default.Description,
                    tooltipText = BuildKonfig.sheetLink,
                    onClick = { uriHandler.openUri(BuildKonfig.sheetLink) },
                    contentDescription = stringResource(Res.string.alley_sheet_link),
                )
            }
        }
    }
}
