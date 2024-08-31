@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.entry

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.entry.generated.resources.Res
import artistalleydatabase.modules.entry.generated.resources.different
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeResourceUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun EntryPrefilledAutocompleteDropdown(
    text: () -> String,
    predictions: List<EntrySection.MultiText.Entry>,
    showPredictions: () -> Boolean,
    onPredictionChosen: (Int) -> Unit,
    modifier: Modifier = Modifier,
    textField: @Composable ExposedDropdownMenuBoxScope.(BringIntoViewRequester) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        textField(bringIntoViewRequester)

        if (showPredictions() && predictions.isNotEmpty()) {
            LaunchedEffect(text(), predictions) {
                expanded = true
            }
            // DropdownMenu overrides the LocalUriHandler, so save it here and pass it down
            val uriHandler = LocalUriHandler.current
            val coroutineScope = rememberCoroutineScope()
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                predictions.forEachIndexed { index, entry ->
                    DropdownMenuItem(
                        onClick = {
                            onPredictionChosen(index)
                            coroutineScope.launch {
                                // TODO: Delay is necessary or column won't scroll
                                delay(500)
                                bringIntoViewRequester.bringIntoView()
                            }
                        },
                        text = {
                            val titleText: @Composable () -> String
                            var subtitleText: () -> String? = { null }
                            var image: () -> String? = { null }
                            var imageLink: () -> String? = { null }
                            var secondaryImage: (() -> String?)? = null
                            var secondaryImageLink: () -> String? = { null }
                            when (entry) {
                                is EntrySection.MultiText.Entry.Custom -> {
                                    titleText = { entry.text }
                                }
                                is EntrySection.MultiText.Entry.Prefilled<*> -> {
                                    titleText = { entry.titleText }
                                    subtitleText = { entry.subtitleText }
                                    image = { entry.image }
                                    imageLink = { entry.imageLink }
                                    secondaryImage = entry.secondaryImage?.let { { it } }
                                    secondaryImageLink = { entry.secondaryImageLink }
                                }
                                EntrySection.MultiText.Entry.Different -> {
                                    titleText = { stringResource(Res.string.different) }
                                }
                            }.run { /*exhaust*/ }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.animateContentSize(),
                            ) {
                                EntryImage(
                                    image = image,
                                    link = imageLink,
                                    uriHandler = uriHandler,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .heightIn(min = 54.dp)
                                        .width(42.dp)
                                )
                                Column(
                                    Modifier
                                        .weight(1f, true)
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 8.dp,
                                            bottom = 8.dp,
                                        )
                                ) {
                                    Text(
                                        text = titleText(),
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                                    )

                                    @Suppress("NAME_SHADOWING")
                                    val subtitleText = subtitleText()
                                    if (subtitleText != null) {
                                        Text(
                                            text = subtitleText,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(start = 24.dp)
                                        )
                                    }
                                }

                                entry.trailingIcon?.let { imageVector ->
                                    Icon(
                                        imageVector = imageVector,
                                        contentDescription = entry.trailingIconContentDescription
                                            ?.let { ComposeResourceUtils.stringResourceCompat(it) },
                                    )
                                }

                                if (secondaryImage != null) {
                                    EntryImage(
                                        image = secondaryImage,
                                        link = secondaryImageLink,
                                        uriHandler = uriHandler,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .heightIn(min = 54.dp)
                                            .width(42.dp)
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
