package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils

internal object ArtistFormHomeScreen {

    @Composable
    operator fun invoke(onOpenForm: () -> Unit) {
        Scaffold {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
                    .padding(it)
                    .padding(16.dp)
            ) {
                OutlinedCard(modifier = Modifier.widthIn(max = 960.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val scrollState = rememberScrollState()
                        val scrollAreaState = rememberScrollAreaState(scrollState)
                        ScrollArea(
                            state = scrollAreaState,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box {
                                Column(
                                    modifier = Modifier.verticalScroll(scrollState)
                                ) {
                                    IntroText(modifier = Modifier.fillMaxWidth().padding(24.dp))
                                }
                                Box(modifier = Modifier.matchParentSize()) {
                                    PrimaryVerticalScrollbar(
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }

                        HorizontalDivider()
                        FilledTonalButton(
                            onClick = onOpenForm,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = "Open form")
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun IntroText(modifier: Modifier = Modifier) {
        Text(
            text = buildAnnotatedString {
                pushStyle(ParagraphStyle(textAlign = TextAlign.Center))
                withStyle(MaterialTheme.typography.titleLarge.toSpanStyle()) {
                    appendLine("Welcome to the Artist Alley Directory!")
                }
                pop()

                appendLine()
                appendLine(
                    "You were linked here because you're an artist tabling at one of the " +
                            "conventions we tag, Anime Expo or Anime NYC. The following page " +
                            "will let you edit the data on your artist profile."
                )
                appendLine()
                append("You can see the public site ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    withLink(LinkAnnotation.Url(AlleyUtils.siteUrl)) {
                        appendLine("here.")
                    }
                }
                appendLine()
                append(
                    "If you have questions/concerns, or want to submit your info in an " +
                            "alternative manner, please contact " +
                            "${AlleyUtils.primaryContactDiscordUsername} on Discord or visit us in "
                )
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    withLink(LinkAnnotation.Url(AlleyUtils.serverUrl)) {
                        append("the AX Discord server")
                    }
                }
                appendLine(".")
            },
            modifier = modifier
        )
    }
}
