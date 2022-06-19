package com.thekeeperofpie.artistalleydatabase.detail

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.navigation.NavDestinations
import com.thekeeperofpie.artistalleydatabase.ui.ArtEntryForm
import com.thekeeperofpie.artistalleydatabase.ui.ArtEntryFormSection
import com.thekeeperofpie.artistalleydatabase.ui.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.ui.ImageSelectBox
import com.thekeeperofpie.artistalleydatabase.ui.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import java.io.File

object DetailsScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        entryId: String = "",
        entryImageFile: File? = null,
        entryImageRatio: Float = 1f,
        imageUri: Uri? = null,
        onImageSelected: (Uri?) -> Unit = {},
        onImageSelectError: (Exception?) -> Unit = {},
        areSectionsLoading: Boolean = false,
        artistSection: ArtEntryFormSection = ArtEntryFormSection(),
        locationSection: ArtEntryFormSection = ArtEntryFormSection(),
        seriesSection: ArtEntryFormSection = ArtEntryFormSection(),
        characterSection: ArtEntryFormSection = ArtEntryFormSection(),
        tagSection: ArtEntryFormSection = ArtEntryFormSection(),
        onClickSave: () -> Unit = {},
        errorRes: Pair<Int, Exception?>? = null,
        onErrorDismiss: () -> Unit = {},
        showDeleteDialog: Boolean = false,
        onDismissDeleteDialog: () -> Unit = {},
        onClickDelete: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
    ) {
        ArtistAlleyDatabaseTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(snackbarHost = {
                    SnackbarErrorText(errorRes?.first, onErrorDismiss = onErrorDismiss)
                }) {
                    Column(
                        Modifier
                            .padding(it)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f, true)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            HeaderImage(
                                entryId,
                                entryImageFile,
                                entryImageRatio,
                                imageUri,
                                onImageSelected,
                                onImageSelectError
                            )

                            ArtEntryForm(
                                areSectionsLoading,
                                artistSection,
                                locationSection,
                                seriesSection,
                                characterSection,
                                tagSection,
                            )
                        }


                        Crossfade(
                            targetState = areSectionsLoading,
                            Modifier.align(Alignment.End)
                        ) {
                            if (!it) {
                                ButtonFooter(
                                    R.string.save to onClickSave,
                                    R.string.delete to onClickDelete,
                                )
                            }
                        }
                    }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = onDismissDeleteDialog,
                            title = { Text(stringResource(R.string.art_entry_delete_dialog_title)) },
                            confirmButton = {
                                TextButton(onClick = onConfirmDelete) {
                                    Text(stringResource(R.string.confirm))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = onDismissDeleteDialog) {
                                    Text(stringResource(R.string.cancel))
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun HeaderImage(
        entryId: String,
        entryImageFile: File?,
        entryImageRatio: Float,
        imageUri: Uri?,
        onImageSelected: (Uri?) -> Unit,
        onImageSelectError: (Exception?) -> Unit
    ) {
        ImageSelectBox(onImageSelected, onImageSelectError) {
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
            } else if (entryImageFile != null) {
                SharedElement(
                    key = "${entryId}_image",
                    screenKey = NavDestinations.ENTRY_DETAILS
                ) {
                    val configuration = LocalConfiguration.current
                    val screenWidth = configuration.screenWidthDp.dp
                    val minimumHeight = screenWidth * entryImageRatio
                    AsyncImage(
                        ImageRequest.Builder(LocalContext.current)
                            .data(entryImageFile)
                            .placeholderMemoryCacheKey("coil_memory_entry_image_search_$entryId")
                            .build(),
                        contentDescription = stringResource(
                            R.string.art_entry_image_content_description
                        ),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = minimumHeight)
                    )
                }
            } else {
                Spacer(
                    Modifier
                        .heightIn(200.dp, 200.dp)
                        .fillMaxWidth()
                        .background(Color.LightGray)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_image_not_supported_24),
                    contentDescription = stringResource(
                        R.string.art_entry_no_image_content_description
                    ),
                    Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    DetailsScreen(
        artistSection = ArtEntryFormSection("Lucidsky"),
        locationSection = ArtEntryFormSection("Fanime 2022"),
        seriesSection = ArtEntryFormSection("Dress Up Darling"),
        characterSection = ArtEntryFormSection("Marin Kitagawa"),
        tagSection = ArtEntryFormSection().apply {
            contents.addAll(listOf("cute", "portrait"))
            pendingValue = "schoolgirl uniform"
        },
    )
}