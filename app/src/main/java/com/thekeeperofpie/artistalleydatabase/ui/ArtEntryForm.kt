package com.thekeeperofpie.artistalleydatabase.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme

object ArtEntryForm {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        areSectionsLoading: Boolean = false,
        artistSection: FormSection = FormSection("ArtEntryForm"),
        locationSection: FormSection = FormSection("ArtEntryForm"),
        seriesSection: FormSection = FormSection("ArtEntryForm"),
        characterSection: FormSection = FormSection("ArtEntryForm"),
        tagSection: FormSection = FormSection("ArtEntryForm"),
        onClickSave: () -> Unit,
        errorRes: Pair<Int, Exception?>? = null,
        onErrorDismiss: () -> Unit = { },
        header: @Composable ColumnScope.() -> Unit = {},
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
                            header()

                            Crossfade(targetState = areSectionsLoading) {
                                if (it) {
                                    CircularProgressIndicator(
                                        Modifier
                                            .padding(16.dp)
                                            .align(Alignment.CenterHorizontally)
                                    )
                                } else {
                                    Column {
                                        SectionContent(
                                            R.string.add_entry_artists_header_zero,
                                            R.string.add_entry_artists_header_one,
                                            R.string.add_entry_artists_header_many,
                                            artistSection,
                                        )

                                        SectionContent(
                                            R.string.add_entry_locations_header_zero,
                                            R.string.add_entry_locations_header_one,
                                            R.string.add_entry_locations_header_many,
                                            locationSection,
                                        )

                                        SectionContent(
                                            R.string.add_entry_series_header_zero,
                                            R.string.add_entry_series_header_one,
                                            R.string.add_entry_series_header_many,
                                            seriesSection,
                                        )

                                        SectionContent(
                                            R.string.add_entry_characters_header_zero,
                                            R.string.add_entry_characters_header_one,
                                            R.string.add_entry_characters_header_many,
                                            characterSection,
                                        )

                                        SectionContent(
                                            R.string.add_entry_tags_header_zero,
                                            R.string.add_entry_tags_header_one,
                                            R.string.add_entry_tags_header_many,
                                            tagSection,
                                        )
                                    }
                                }
                            }
                        }


                        Crossfade(
                            targetState = areSectionsLoading,
                            Modifier.align(Alignment.End)
                        ) {
                            if (!it) {
                                ButtonFooter(onClickSave, R.string.save)
                            }
                        }
                    }
                }
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
            PrefilledSectionField(value) {
                section.contents[index] = it
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

    @Composable
    fun ImageSelectBox(
        onImageSelected: (Uri?) -> Unit,
        onImageSelectError: (Exception?) -> Unit,
        content: @Composable BoxScope.() -> Unit,
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
            content()
        }
    }

    class FormSection(initialPendingValue: String = "") {
        val contents = mutableStateListOf<String>()
        var pendingValue by mutableStateOf(initialPendingValue)

        fun finalContents() = (contents + pendingValue).filter { it.isNotEmpty() }
    }
}

@Preview
@Composable
fun Preview() {
    ArtEntryForm(
        artistSection = ArtEntryForm.FormSection("Lucidsky"),
        locationSection = ArtEntryForm.FormSection("Fanime 2022"),
        seriesSection = ArtEntryForm.FormSection("Dress Up Darling"),
        characterSection = ArtEntryForm.FormSection("Marin Kitagawa"),
        tagSection = ArtEntryForm.FormSection().apply {
            contents.addAll(listOf("cute", "portrait"))
            pendingValue = "schoolgirl uniform"
        },
        onClickSave = {}
    )
}