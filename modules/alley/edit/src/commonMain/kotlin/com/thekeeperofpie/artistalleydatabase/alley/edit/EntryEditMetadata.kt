package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_last_modified_author_prefix
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_last_modified_prefix
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Stable
class EntryEditMetadata(
    lastEditor: String? = null,
    lastEditTime: Instant? = null,
) {
    var lastEditor by mutableStateOf(lastEditor)
    var lastEditTime by mutableStateOf(lastEditTime)

    object Saver : ComposeSaver<EntryEditMetadata, List<Any?>> {
        override fun SaverScope.save(value: EntryEditMetadata) = listOf(
            value.lastEditor,
            value.lastEditTime?.toString()
        )

        override fun restore(value: List<Any?>): EntryEditMetadata {
            val (lastEditor, lastEditTime) = value
            return EntryEditMetadata(
                lastEditor = lastEditor as String?,
                lastEditTime = (lastEditTime as? String?)?.let(Instant.Companion::parseOrNull)
            )
        }
    }
}

@Composable
internal fun MetadataSection(metadata: EntryEditMetadata) {
    val lastEditTime = metadata.lastEditTime
    if (lastEditTime != null) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val textColorDim = LocalContentColor.current.copy(alpha = 0.6f)
            val colorPrimary = MaterialTheme.colorScheme.primary
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = textColorDim)) {
                        append(stringResource(Res.string.alley_edit_artist_edit_last_modified_prefix))
                    }
                    append(' ')
                    withStyle(SpanStyle(color = colorPrimary)) {
                        append(LocalDateTimeFormatter.current.formatDateTime(lastEditTime))
                    }
                    val lastEditor = metadata.lastEditor
                    if (lastEditor != null) {
                        append(' ')
                        withStyle(SpanStyle(color = textColorDim)) {
                            append(stringResource(Res.string.alley_edit_artist_edit_last_modified_author_prefix))
                        }
                        append(' ')
                        append(lastEditor)
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
