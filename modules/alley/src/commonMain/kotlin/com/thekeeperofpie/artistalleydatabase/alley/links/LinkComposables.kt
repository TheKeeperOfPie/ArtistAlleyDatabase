package com.thekeeperofpie.artistalleydatabase.alley.links

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.thekeeperofpie.artistalleydatabase.alley.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

@Composable
fun LinkRow(link: LinkModel?, isLast: Boolean, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val bottomPadding = if (isLast) 12.dp else 8.dp
    Tooltip(
        link?.link,
        Alignment.BottomEnd,
        onClick = { link?.link?.let { uriHandler.openUri(it) } }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    bottom = bottomPadding,
                )
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
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
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .placeholder(
                        visible = link == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )

            val clipboardManager = LocalClipboardManager.current
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(Res.string.alley_copy_link_icon_content_description),
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(enabled = link != null) {
                        clipboardManager.setText(buildAnnotatedString { append(link?.link) })
                    }
                    .heightIn(min = 24.dp)
                    .padding(horizontal = 16.dp)
                    .placeholder(
                        visible = link == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}
