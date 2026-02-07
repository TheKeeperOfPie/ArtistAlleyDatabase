package com.thekeeperofpie.artistalleydatabase.alley.edit.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_list_added
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_list_deleted
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_form_timestamp
import com.materialkolor.ktx.harmonize
import com.materialkolor.utils.ColorUtils
import com.thekeeperofpie.artistalleydatabase.alley.models.HistoryListDiff
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.ThemeAwareElevatedCard
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random
import kotlin.time.Instant

@Composable
internal fun HistoryEntryCard(
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    header: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardContent = remember {
        movableContentOf {
            Column(modifier = Modifier.padding(16.dp)) {
                header()

                Spacer(modifier = Modifier.height(12.dp))

                content()
            }
        }
    }
    val modifier = Modifier.fillMaxWidth()
    if (selected) {
        if (onClick == null) {
            OutlinedCard(modifier = modifier) { cardContent() }
        } else {
            OutlinedCard(onClick = onClick, modifier = modifier) { cardContent() }
        }
    } else {
        ThemeAwareElevatedCard(onClick = onClick, modifier = modifier) { cardContent() }
    }
}

@Composable
internal fun HistoryCardHeader(
    editor: String?,
    timestamp: Instant,
    formTimestamp: Instant?,
    additionalActions: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val (color, textColor) = remember(editor, primaryColor) {
            val color = Random(editor.hashCode()).let {
                Color(it.nextFloat(), it.nextFloat(), it.nextFloat())
            }.harmonize(primaryColor, true)
            val textColor = if (ColorUtils.calculateLuminance(color.toArgb()) > 0.5f) {
                Color.Black
            } else {
                Color.White
            }
            color to textColor
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, primaryColor, CircleShape)
                .padding(4.dp)
        ) {
            Text(
                text = editor?.take(2)?.uppercase().orEmpty(),
                color = textColor,
                autoSize = TextAutoSize.StepBased(minFontSize = 12.sp),
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmallEmphasized,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            if (editor != null) {
                Text(
                    text = editor,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                )
            }
            Text(
                text = LocalDateTimeFormatter.current.formatDateTime(timestamp),
                style = MaterialTheme.typography.labelSmall,
            )

            if (formTimestamp != null) {
                Text(
                    text = stringResource(
                        Res.string.alley_edit_artist_history_form_timestamp,
                        LocalDateTimeFormatter.current.formatDateTime(formTimestamp),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        additionalActions?.invoke()
    }
}

@Composable
internal fun HistorySingleChangeRow(label: StringResource, value: String?) {
    if (value != null) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                ChangeLabel(label)
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
internal fun HistoryListChangeRow(label: StringResource, diff: HistoryListDiff?) {
    if (diff != null) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                ChangeLabel(label)
                Column(modifier = Modifier.weight(1f)) {
                    diff.added?.forEach {
                        Text(
                            text = stringResource(
                                Res.string.alley_edit_artist_field_label_list_added,
                                it
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    diff.deleted?.forEach {
                        Text(
                            text = stringResource(
                                Res.string.alley_edit_artist_field_label_list_deleted,
                                it
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AlleyTheme.colorScheme.negative,
                        )
                    }
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun ChangeLabel(label: StringResource) {
    Text(
        text = stringResource(label),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        autoSize = TextAutoSize.StepBased(
            minFontSize = 8.sp,
            maxFontSize = MaterialTheme.typography.labelMedium.fontSize,
        ),
        maxLines = 1,
        modifier = Modifier.width(120.dp)
            .padding(start = 4.dp, end = 16.dp)
    )
}
