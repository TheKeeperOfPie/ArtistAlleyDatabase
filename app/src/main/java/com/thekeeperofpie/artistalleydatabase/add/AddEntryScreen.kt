package com.thekeeperofpie.artistalleydatabase.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.ui.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.ui.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme

object AddScreen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    operator fun invoke(
        imageUri: Uri? = null,
        onImageSelected: (Uri?) -> Unit = {},
        onImageSelectError: (Exception?) -> Unit = {},
        artistSection: FormSection = FormSection(""),
        locationSection: FormSection = FormSection(""),
        seriesSection: FormSection = FormSection(""),
        characterSection: FormSection = FormSection(""),
        tagSection: FormSection = FormSection(""),
        onClickSave: () -> Unit = {},
        errorRes: Pair<Int, Exception?>? = null,
        onErrorDismiss: () -> Unit = { },
    ) {
        ArtistAlleyDatabaseTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(snackbarHost = {
                    SnackbarErrorText(errorRes?.first, onErrorDismiss = onErrorDismiss)
                }) {
                    Column(Modifier.padding(it)) {
                        Column(
                            modifier = Modifier
                                .weight(1f, true)
                                .verticalScroll(rememberScrollState())
                        ) {
                            HeaderImage(imageUri, onImageSelected, onImageSelectError)

                            SectionContent(
                                R.string.add_entry_artists_header_zero,
                                R.string.add_entry_artists_header_one,
                                R.string.add_entry_artists_header_many,
                                artistSection
                            )

                            SectionContent(
                                R.string.add_entry_locations_header_zero,
                                R.string.add_entry_locations_header_one,
                                R.string.add_entry_locations_header_many,
                                locationSection
                            )

                            SectionContent(
                                R.string.add_entry_series_header_zero,
                                R.string.add_entry_series_header_one,
                                R.string.add_entry_series_header_many,
                                seriesSection
                            )

                            SectionContent(
                                R.string.add_entry_characters_header_zero,
                                R.string.add_entry_characters_header_one,
                                R.string.add_entry_characters_header_many,
                                characterSection
                            )

                            SectionContent(
                                R.string.add_entry_tags_header_zero,
                                R.string.add_entry_tags_header_one,
                                R.string.add_entry_tags_header_many,
                                tagSection
                            )
                        }

                        ButtonFooter(onClickSave, R.string.save)
                    }
                }
            }
        }
    }

    @Composable
    private fun HeaderImage(
        imageUri: Uri?,
        onImageSelected: (Uri?) -> Unit,
        onImageSelectError: (Exception?) -> Unit
    ) {
        val imageSelectLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
            onImageSelected
        )
        Box(
            Modifier
                .wrapContentHeight()
                .heightIn(0.dp, 400.dp)
                .verticalScroll(rememberScrollState())
                .clickable(onClick = {
                    try {
                        imageSelectLauncher.launch("image/*")
                    } catch (e: Exception) {
                        onImageSelectError(e)
                    }
                })
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(
                        R.string.art_entry_image_content_description
                    ),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            } else {
                Spacer(
                    Modifier
                        .heightIn(200.dp, 200.dp)
                        .fillMaxWidth()
                        .background(Color.LightGray)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_image_search_24),
                    contentDescription = stringResource(
                        R.string.add_entry_select_image_content_description
                    ),
                    Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }

    @Composable
    private fun SectionHeader(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        )
    }

    @Composable
    private fun SectionContent(
        @StringRes headerZero: Int,
        @StringRes headerOne: Int,
        @StringRes headerMany: Int,
        section: FormSection,
    ) {
        when (section.contents.size) {
            0 -> headerZero
            1 -> headerOne
            else -> headerMany
        }
            .let { stringResource(it) }
            .let { SectionHeader(it) }

        section.contents.forEachIndexed { index, value ->
            PrefilledSectionField(value) { newValue ->
                section.contents.toMutableList().apply {
                    this[index] = newValue
                }
            }
        }
        OpenSectionField(
            section.pendingValue,
            { section.pendingValue = it },
            {
                section.contents += it
                section.pendingValue = ""
            }
        )
    }

    @Composable
    private fun PrefilledSectionField(
        value: String,
        onValueChange: (value: String) -> Unit
    ) {
        TextField(
            value = value,
            onValueChange = { onValueChange(it) },
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        )
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun OpenSectionField(
        value: String,
        onValueChange: (value: String) -> Unit,
        onDone: (value: String) -> Unit,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onDone(value) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
                .onKeyEvent {
                    if (it.type == KeyEventType.KeyUp && it.key == Key.Enter) {
                        onDone(value)
                        true
                    } else false
                }
        )
    }

    class FormSection(initialPendingValue: String = "") {
        var contents by mutableStateOf(emptyList<String>())
        var pendingValue by mutableStateOf(initialPendingValue)

        fun finalContents() = (contents + pendingValue).filter { it.isNotEmpty() }
    }
}

@Preview
@Composable
fun Preview() {
    AddScreen(
        artistSection = AddScreen.FormSection("Lucidsky"),
        locationSection = AddScreen.FormSection("Fanime 2022"),
        seriesSection = AddScreen.FormSection("Dress Up Darling"),
        characterSection = AddScreen.FormSection("Marin Kitagawa"),
        tagSection = AddScreen.FormSection().apply {
            contents = listOf("cute", "portrait")
            pendingValue = "schoolgirl uniform"
        }
    )
}