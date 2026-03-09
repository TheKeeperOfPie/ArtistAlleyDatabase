package com.thekeeperofpie.artistalleydatabase.alley.edit.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_saving
import com.thekeeperofpie.artistalleydatabase.alley.ui.InfiniteProgressIndicator
import org.jetbrains.compose.resources.stringResource

@Composable
fun ContentSavingBox(
    saving: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        content()

        if (saving) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                modifier = Modifier.matchParentSize()
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        InfiniteProgressIndicator()
                        Text(stringResource(Res.string.alley_edit_saving))
                    }
                }
            }
        }
    }
}
