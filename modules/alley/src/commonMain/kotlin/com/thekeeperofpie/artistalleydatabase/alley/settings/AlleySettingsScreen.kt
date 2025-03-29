package com.thekeeperofpie.artistalleydatabase.alley.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.AnnotatedString
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
import artistalleydatabase.modules.alley.generated.resources.alley_answer_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_author_link
import artistalleydatabase.modules.alley.generated.resources.alley_server_link
import artistalleydatabase.modules.alley.generated.resources.alley_settings_export
import artistalleydatabase.modules.alley.generated.resources.alley_settings_export_copy_instructions
import artistalleydatabase.modules.alley.generated.resources.alley_settings_export_copy_share
import artistalleydatabase.modules.alley.generated.resources.alley_settings_export_full
import artistalleydatabase.modules.alley.generated.resources.alley_settings_export_partial
import artistalleydatabase.modules.alley.generated.resources.alley_settings_export_summary
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import_success
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import_summary
import artistalleydatabase.modules.alley.generated.resources.alley_sheet_link
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.links.Logo
import com.thekeeperofpie.artistalleydatabase.alley.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconWithTooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsScreen
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalShareHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.appendParagraph
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
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
        var exportPartialText by mutableStateOf<String?>(null)
        var importState by mutableStateOf<LoadingResult<*>>(LoadingResult.empty<Unit>())
    }

    sealed interface Event {
        data object ExportPartial : Event
        data object ExportFull : Event
        data class Import(val data: String) : Event
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
            upIconOption = UpIconOption.Back(navigationController::popBackStack),
            modifier = Modifier.widthIn(max = 1200.dp),
            customSection = {
                when (it.id) {
                    "header" -> Header()
                    "export" -> ExportSection(
                        exportPartialText = { state.exportPartialText },
                        onClickExportPartial = {
                            eventSink(AlleySettingsScreen.Event.ExportPartial)
                        },
                        onClickExportFull = {
                            eventSink(AlleySettingsScreen.Event.ExportFull)
                        },
                    )
                    "import" -> ImportSection(
                        state = { state.importState },
                        onResetState = { state.importState = LoadingResult.empty<Unit>() },
                        onClickImport = { eventSink(AlleySettingsScreen.Event.Import(it)) }
                    )
                    "faq" -> FaqSection(
                        onInstallClick = { PlatformSpecificConfig.requestInstall() },
                        onExportClick = {
                            eventSink(AlleySettingsScreen.Event.ExportPartial)
                        }
                    )
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
                modifier = Modifier.weight(1f).padding(16.dp)
            )

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

@Composable
private fun ExportSection(
    exportPartialText: () -> String?,
    onClickExportPartial: () -> Unit,
    onClickExportFull: () -> Unit,
) {
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

            Button(onClickExportPartial) {
                Text(text = stringResource(Res.string.alley_settings_export_partial))
            }

            Button(onClickExportFull) {
                Text(text = stringResource(Res.string.alley_settings_export_full))
            }
        }

        val exportPartialText = exportPartialText()
        if (exportPartialText != null) {
            HorizontalDivider()

            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(Res.string.alley_settings_export_copy_instructions),
                    modifier = Modifier.weight(1f)
                )

                // TODO: Add copy action once CMP supports it on all platforms
                //   https://youtrack.jetbrains.com/issue/CMP-7624

                // TODO: Support sharing for non-Android platforms
                val shareHandler = LocalShareHandler.current
                if (shareHandler != null) {
                    IconWithTooltip(
                        imageVector = Icons.Default.Share,
                        tooltipText = stringResource(Res.string.alley_settings_export_copy_share),
                        onClick = { shareHandler.shareText(exportPartialText) },
                        contentDescription = stringResource(Res.string.alley_settings_export_copy_share),
                    )
                }
            }

            SelectionContainer {
                Text(
                    text = exportPartialText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ImportSection(
    state: () -> LoadingResult<*>,
    onResetState: () -> Unit,
    onClickImport: (data: String) -> Unit,
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

            val enabled by remember {
                derivedStateOf {
                    val state = state()
                    !state.loading && state.error == null && importData.isNotEmpty()
                }
            }
            Button(enabled = enabled, onClick = { onClickImport(importData) }) {
                Box {
                    Text(text = stringResource(Res.string.alley_settings_import))
                    if (state().loading) {
                        CircularProgressIndicator()
                    }
                }
            }
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

        val state = state()
        val error = state.error
        val message = if (state.success) {
            stringResource(Res.string.alley_settings_import_success)
        } else {
            error?.message()
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
    }
}

@Composable
private fun FaqSection(onInstallClick: () -> Unit, onExportClick: () -> Unit) {
    OutlinedCard(Modifier.padding(16.dp).fillMaxWidth()) {
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

        val colorScheme = MaterialTheme.colorScheme
        if (PlatformSpecificConfig.installable) {
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
                                "internet connection."
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
                                linkInteractionListener = { onExportClick() },
                            )
                        ) {
                            append("Export")
                        }
                    }
                    append(" your favorites to transfer them between desktop and mobile.")
                }
            )
        }

        HorizontalDivider()

        QuestionAnswer(
            question = "I'm a tabling artist and my info is missing or incorrect",
            answer = {
                append("Submit the ")
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
                    " and we'll get your info updated! Thank you for helping making the site " +
                            "better!"
                )
            }
        )
    }
}

@Composable
private fun QuestionAnswer(
    question: String,
    answer: String,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    extraContent: @Composable () -> Unit = {},
) = QuestionAnswer(
    question = question,
    answer = { append(answer) },
    inlineContent = inlineContent,
    extraContent = extraContent,
)

@Composable
private fun QuestionAnswer(
    question: String,
    answer: AnnotatedString.Builder.() -> Unit,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    extraContent: @Composable () -> Unit = {},
) {
    Column {
        var expanded by rememberSaveable { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
                    .padding(horizontal = 16.dp)
            )

            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(Res.string.alley_answer_expand_content_description),
                onClick = { expanded = !expanded },
            )
        }

        AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
            if (expanded) {
                val answerText = remember(answer) {
                    buildAnnotatedString(answer)
                }
                Text(
                    text = answerText,
                    inlineContent = inlineContent,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 12.dp,
                    )
                )
            }
        }

        extraContent()
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
    var exportPartialText by remember { mutableStateOf<String?>(null) }
    ExportSection(
        exportPartialText = { exportPartialText },
        onClickExportPartial = { exportPartialText = "Preview partial export" },
        onClickExportFull = { exportPartialText = "Preview full export" },
    )
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
    )
}

@Preview
@Composable
private fun FaqPreview() = PreviewDark {
    FaqSection(onInstallClick = {}, onExportClick = {})
}
