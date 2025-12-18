package com.thekeeperofpie.artistalleydatabase.alley.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_author_link
import artistalleydatabase.modules.alley.generated.resources.alley_server_link
import artistalleydatabase.modules.alley.generated.resources.alley_settings_clear
import artistalleydatabase.modules.alley.generated.resources.alley_settings_clear_action
import artistalleydatabase.modules.alley.generated.resources.alley_settings_clear_cancel
import artistalleydatabase.modules.alley.generated.resources.alley_settings_clear_confirm
import artistalleydatabase.modules.alley.generated.resources.alley_settings_clear_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_settings_clear_summary
import artistalleydatabase.modules.alley.generated.resources.alley_settings_export
import artistalleydatabase.modules.alley.generated.resources.alley_settings_export_summary
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import_file
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import_success
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import_summary
import artistalleydatabase.modules.alley.generated.resources.alley_sheet_link
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.fullName
import com.thekeeperofpie.artistalleydatabase.alley.links.Logo
import com.thekeeperofpie.artistalleydatabase.alley.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.alley.ui.QuestionAnswer
import com.thekeeperofpie.artistalleydatabase.alley.ui.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsScreen
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsSection
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.appendParagraph
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration.Companion.seconds

object AlleySettingsScreen {
    @Stable
    class State(
        val sections: List<SettingsSection>,
    ) {
        var importState by mutableStateOf<LoadingResult<*>>(LoadingResult.empty<Unit>())
    }

    sealed interface Event {
        data class Import(val data: String) : Event
        data class ImportFile(val file: PlatformFile?) : Event
        data object ClearUserData : Event
    }
}

@Composable
internal fun AlleySettingsScreen(
    state: AlleySettingsScreen.State,
    eventSink: (AlleySettingsScreen.Event) -> Unit,
) {
    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
        val navigationController = LocalNavigationController.current
        SettingsScreen(
            sections = state.sections,
            upIconOption = UpIconOption.Back(navigationController),
            automaticallyInsertDividers = false,
            modifier = Modifier.widthIn(max = 1200.dp),
        ) {
            when (it.id) {
                "header" -> Header()
                "export" -> ExportSection()
                "import" -> ImportSection(
                    state = { state.importState },
                    onResetState = { state.importState = LoadingResult.empty<Unit>() },
                    onClickImport = { eventSink(AlleySettingsScreen.Event.Import(it)) },
                    onImportFile = { eventSink(AlleySettingsScreen.Event.ImportFile(it)) },
                )
                "clear" -> ClearSection(
                    onClear = { eventSink(AlleySettingsScreen.Event.ClearUserData) },
                )
                "faq" -> FaqSection(onInstallClick = { PlatformSpecificConfig.requestInstall() })
                else -> throw IllegalArgumentException()
            }
        }
    }
}

@Composable
private fun Header() {
    OutlinedCard(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val colorScheme = MaterialTheme.colorScheme
            val typography = MaterialTheme.typography
            val text = remember(colorScheme, typography) {
                buildAnnotatedString {
                    append("Built by ")
                    withStyle(SpanStyle(color = colorScheme.primary)) {
                        withLink(LinkAnnotation.Url(BuildKonfig.authorOneUrl)) {
                            append(BuildKonfig.authorOneName)
                        }
                    }
                    append(" and ")
                    withStyle(SpanStyle(color = colorScheme.primary)) {
                        withLink(LinkAnnotation.Url(BuildKonfig.authorTwoUrl)) {
                            append(BuildKonfig.authorTwoName)
                        }
                    }
                    append(" for the ")
                    withStyle(SpanStyle(color = colorScheme.primary)) {
                        withLink(LinkAnnotation.Url(BuildKonfig.serverUrl)) {
                            append(BuildKonfig.serverName)
                        }
                    }
                    append(
                        "\n\nAnime NYC data provided by ${BuildKonfig.authorAnycOneName}, " +
                                "${BuildKonfig.authorAnycTwoName}, and "
                    )
                    withStyle(SpanStyle(color = colorScheme.primary)) {
                        withLink(LinkAnnotation.Url(BuildKonfig.authorAnycThreeUrl)) {
                            append(BuildKonfig.authorAnycThreeName)
                        }
                    }

                    append("\n\n")

                    withStyle(typography.labelMedium.toSpanStyle()) {
                        append("Some ANYC data also provided by ${BuildKonfig.authorAnycHistoricalOneName} who has since been removed from the project")
                    }
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).padding(16.dp)
            )

            val uriHandler = LocalUriHandler.current

            TooltipIconButton(
                icon = Logo.GITHUB.icon,
                tooltipText = BuildKonfig.authorTwoUrl,
                onClick = { uriHandler.openUri(BuildKonfig.authorTwoUrl) },
                contentDescription = stringResource(Res.string.alley_author_link),
            )

            TooltipIconButton(
                icon = Logo.DISCORD.icon,
                tooltipText = BuildKonfig.serverUrl,
                onClick = { uriHandler.openUri(BuildKonfig.serverUrl) },
                contentDescription = stringResource(Res.string.alley_server_link),
            )

            TooltipIconButton(
                icon = Icons.Default.Description,
                tooltipText = BuildKonfig.sheetLink,
                onClick = { uriHandler.openUri(BuildKonfig.sheetLink) },
                contentDescription = stringResource(Res.string.alley_sheet_link),
            )
        }
    }
}

@Composable
private fun ExportSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.alley_settings_export),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(Res.string.alley_settings_export_summary),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            val navigationController = LocalNavigationController.current
            Button(onClick = { navigationController.navigate(Destinations.Export) }) {
                Text(text = stringResource(Res.string.alley_settings_export))
            }
        }
    }
}

@Composable
private fun ImportSection(
    state: () -> LoadingResult<*>,
    onResetState: () -> Unit,
    onClickImport: (data: String) -> Unit,
    onImportFile: (PlatformFile?) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        var importData by rememberSaveable { mutableStateOf("") }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.alley_settings_import),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(Res.string.alley_settings_import_summary),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            val launcher = rememberFilePickerLauncher(onResult = onImportFile)
            Button(onClick = launcher::launch) {
                Text(text = stringResource(Res.string.alley_settings_import_file))
            }

            val enabled by remember {
                derivedStateOf {
                    val state = state()
                    !state.loading && state.error == null && importData.isNotEmpty()
                }
            }
            Button(enabled = enabled, onClick = { onClickImport(importData) }) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = stringResource(Res.string.alley_settings_import))
                    if (state().loading) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        val state = state()
        val error = state.error
        val message = if (state.success) {
            stringResource(Res.string.alley_settings_import_success)
        } else {
            error?.let { "${it.messageText()} ${it.throwable?.message}" }
        }
        if (message != null) {
            Text(
                text = message,
                color = if (error == null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = importData,
            onValueChange = {
                if (state().error != null) {
                    onResetState()
                }
                importData = it
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ClearSection(onClear: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.alley_settings_clear),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(Res.string.alley_settings_clear_summary),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            var showConfirmationDialog by remember { mutableStateOf(false) }
            Button(onClick = { showConfirmationDialog = true }) {
                Text(stringResource(Res.string.alley_settings_clear_action))
            }

            if (showConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    text = {
                        Text(stringResource(Res.string.alley_settings_clear_explanation))
                    },
                    confirmButton = {
                        Button(onClick = {
                            onClear()
                            showConfirmationDialog = false
                        }) {
                            Text(stringResource(Res.string.alley_settings_clear_confirm))
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showConfirmationDialog = false }) {
                            Text(stringResource(Res.string.alley_settings_clear_cancel))
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun FaqSection(onInstallClick: () -> Unit) {
    OutlinedCard(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Text(
            text = "FAQ",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        QuestionAnswer(
            "What is this?",
            "This site tracks artist alleys from conventions, offering a way to view " +
                    "artist catalogs and stamp rallies ahead of time."
        )

        HorizontalDivider()

        QuestionAnswer(
            question = "How do I use it?",
            answer = {
                appendParagraph("The Artists and Rallies tabs show the entries for the selected con.")
                appendParagraph("You can change the convention/year by clicking the header on each tab.")
                append("If you see an artist you're interested in, click the heart ")
                appendInlineContent("heart")
                append(" to add them to your favorites, which can be viewed in the center tab. This serves as a shopping list for when you're at con.")
            },
            inlineContent = remember {
                mapOf(
                    "heart" to InlineTextContent(
                        Placeholder(
                            width = 1.em,
                            height = 1.em,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        Icon(imageVector = Icons.Filled.FavoriteBorder, contentDescription = null)
                    }
                )
            },
        )

        HorizontalDivider()

        val animeExpo2023 = stringResource(DataYear.ANIME_EXPO_2023.fullName)
        val animeExpo2024 = stringResource(DataYear.ANIME_EXPO_2024.fullName)
        val animeExpo2025 = stringResource(DataYear.ANIME_EXPO_2025.fullName)
        val animeNyc2024 = stringResource(DataYear.ANIME_NYC_2024.fullName)
        val animeNyc2025 = stringResource(DataYear.ANIME_NYC_2025.fullName)
        val colorScheme = MaterialTheme.colorScheme
        // TODO: Add 2026
        QuestionAnswer(
            question = "Can I access the raw data?",
            answer = {
                appendParagraph("The backing database is a series of spreadsheets, which you can find here:")
                val array = arrayOf(
                    animeExpo2023 to BuildKonfig.sheetIdAnimeExpo2023,
                    animeExpo2024 to BuildKonfig.sheetIdAnimeExpo2024,
                    animeExpo2025 to BuildKonfig.sheetIdAnimeExpo2025,
                    animeNyc2024 to BuildKonfig.sheetIdAnimeNyc2024,
                    animeNyc2025 to BuildKonfig.sheetIdAnimeNyc2025,
                )
                array.forEachIndexed { index, (name, sheetId) ->
                    withStyle(SpanStyle(color = colorScheme.primary)) {
                        withLink(LinkAnnotation.Url("https://docs.google.com/spreadsheets/d/$sheetId/view")) {
                            append(name)
                        }
                    }
                    if (index != array.lastIndex) {
                        appendLine()
                    }
                }
            },
        )

        HorizontalDivider()

        if (PlatformSpecificConfig.installable) {
            val navigationController = LocalNavigationController.current
            QuestionAnswer(
                "Can I use this offline?",
                answer = {
                    append("This site can be ")
                    withStyle(
                        SpanStyle(
                            color = colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        )
                    ) {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "install",
                                linkInteractionListener = { onInstallClick() },
                            )
                        ) {
                            append("installed as an offline app")
                        }
                    }
                    appendParagraph(
                        ", allowing you to browse during a convention without an " +
                                "internet connection. This may not work on Apple devices, but" +
                                "should work on Android."
                    )
                    withStyle(
                        SpanStyle(
                            color = colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        )
                    ) {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "export",
                                linkInteractionListener = {
                                    navigationController.navigate(Destinations.Export)
                                },
                            )
                        ) {
                            append("Export")
                        }
                    }
                    append(" your favorites to transfer them between desktop and mobile.")
                }
            )

            HorizontalDivider()
        }

        val latestConvention = stringResource(DataYear.LATEST.fullName)
        QuestionAnswer(
            question = "I'm a tabling artist and my info is missing or incorrect",
            answer = {
                append("If you're tabling at the latest convention ($latestConvention), submit the ")
                withStyle(
                    SpanStyle(
                        color = colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    )
                ) {
                    withLink(LinkAnnotation.Url(BuildKonfig.artistFormLink)) {
                        append("artist form")
                    }
                }
                append(
                    " and we'll get your info updated! Thank you for helping make the site better!"
                )
            }
        )

        HorizontalDivider()

        QuestionAnswer(
            question = "I have questions, comments, concerns, suggestions, or feedback",
            answer = {
                append("DM ")
                append(BuildKonfig.authorTwoUsername)
                append(" on Discord or visit us in the ")
                withStyle(SpanStyle(color = colorScheme.primary)) {
                    withLink(LinkAnnotation.Url(BuildKonfig.serverUrl)) {
                        append(BuildKonfig.serverChannel)
                    }
                }
                append(" channel.")
            }
        )
    }
}

@Preview
@Composable
private fun HeaderPreview() = PreviewDark {
    Header()
}

@Preview
@Composable
private fun ExportPreview() = PreviewDark {
    ExportSection()
}

@Preview
@Composable
private fun ImportPreview() = PreviewDark {
    var state by remember { mutableStateOf(LoadingResult.empty<Unit>()) }
    val scope = rememberCoroutineScope()
    ImportSection(
        state = { state },
        onResetState = { state = LoadingResult.empty() },
        onClickImport = {
            scope.launch {
                state = LoadingResult.loading()
                delay(2.seconds)
                state = LoadingResult.error("Failed to import")
            }
        },
        onImportFile = {},
    )
}

@Preview
@Composable
private fun FaqPreview() = PreviewDark {
    FaqSection(onInstallClick = {})
}
