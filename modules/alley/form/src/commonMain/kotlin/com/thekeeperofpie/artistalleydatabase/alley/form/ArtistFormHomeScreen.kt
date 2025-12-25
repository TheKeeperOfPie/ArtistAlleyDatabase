package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils

internal object ArtistFormHomeScreen {

    @Composable
    operator fun invoke(onClickNext: () -> Unit) {
        Scaffold {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(it)
            ) {
                OutlinedCard(modifier = Modifier.widthIn(max = 960.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        IntroText()

                        FilledTonalButton(onClick = onClickNext) {
                            Text(text = "Open form")
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun IntroText() {
        Text(buildAnnotatedString {
            pushStyle(ParagraphStyle(textAlign = TextAlign.Center))
            withStyle(MaterialTheme.typography.titleLarge.toSpanStyle()) {
                appendLine("Welcome to the Artist Alley Directory!")
            }
            pop()

            appendLine()
            appendLine(
                "You were linked here because you're an artist tabling at one of the " +
                        "conventions we tag, Anime Expo or Anime NYC."
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
                "On the next page, you will be required to log in with a Google account. This is " +
                        "for spam protection. Cloudflare will get access to your email, but we do"
            )
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(" NOT ")
            }
            appendLine(
                "collect this information. The access key in the link you were provided is " +
                        "used to identify you and should not be shared with anyone."
            )
            appendLine()
            appendLine(
                "If you have questions/concerns, or want to submit your info in an " +
                        "alternative manner, please contact " +
                        "${AlleyUtils.primaryContactDiscordUsername} on Discord."
            )
        })
    }
}
