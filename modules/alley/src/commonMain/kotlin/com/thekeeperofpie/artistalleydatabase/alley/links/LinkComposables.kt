package com.thekeeperofpie.artistalleydatabase.alley.links

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_copy_link_icon_content_description
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun LinkRow(
    link: LinkModel?,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    additionalActions: (@Composable () -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .placeholder(
                visible = link == null,
                highlight = PlaceholderHighlight.shimmer(),
            )
            .clickable { link?.link?.let { uriHandler.openUri(it) } }
    ) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    positioning = TooltipAnchorPosition.Below,
                    spacingBetweenTooltipAndAnchor = 0.dp,
                ),
                tooltip = {
                    PlainTooltip(Modifier.clickable { link?.link?.let { uriHandler.openUri(it) } }) {
                        Text(link?.link.orEmpty())
                    }
                },
                state = rememberTooltipState(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                        .padding(
                            start = 16.dp,
                            top = 8.dp,
                            bottom = if (isLast) 12.dp else 8.dp,
                        )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.height(20.dp)
                            .widthIn(min = 20.dp)
                    ) {
                        Icon(
                            imageVector = link?.logo?.icon ?: Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier
                                .height(16.dp)
                                .placeholder(
                                    visible = link == null,
                                    highlight = PlaceholderHighlight.shimmer(),
                                )
                        )
                    }

                    val outlineVariant = MaterialTheme.colorScheme.outline
                    val label = link?.logo?.label?.let { stringResource(it) }
                    val text = remember(link, label, outlineVariant) {
                        buildAnnotatedString {
                            // TODO: This doesn't support localization
                            if (label != null) {
                                withStyle(SpanStyle(color = outlineVariant)) {
                                    append(label)
                                    append(" - ")
                                }
                            }
                            if (link != null) {
                                append(link.identifier)
                            }
                        }
                    }

                    Text(
                        text = text,
                        style = if (color.isSpecified) {
                            MaterialTheme.typography.bodyMedium.copy(color = color)
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                    )
                }
            }
        }

        val clipboardManager = LocalClipboardManager.current
        TooltipIconButton(
            icon = Icons.Default.ContentCopy,
            tooltipText = stringResource(Res.string.alley_copy_link_icon_content_description),
            positioning = TooltipAnchorPosition.Start,
            onClick = {
                if (link != null) {
                    clipboardManager.setText(buildAnnotatedString { append(link.link) })
                }
            },
            useButtonOnClickForTooltipOnClick = true,
            modifier = Modifier
                .fillMaxHeight()
                .placeholder(
                    visible = link == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )

        additionalActions?.invoke()
    }
}
