package com.thekeeperofpie.artistalleydatabase.detail

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryForm
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntrySection
import com.thekeeperofpie.artistalleydatabase.art.details.ImageSelectBox
import com.thekeeperofpie.artistalleydatabase.art.details.SampleArtEntrySectionsProvider
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGrid
import com.thekeeperofpie.artistalleydatabase.navigation.NavDestinations
import com.thekeeperofpie.artistalleydatabase.ui.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.ui.SnackbarErrorText
import java.io.File

object DetailsScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        entryId: () -> String = { "" },
        entryImageFile: () -> File? = { null },
        entryImageRatio: () -> Float = { 1f },
        imageUri: () -> Uri? = { null },
        onImageSelected: (Uri?) -> Unit = {},
        onImageSelectError: (Exception?) -> Unit = {},
        onImageSizeResult: (Int, Int) -> Unit = { _, _ -> },
        onImageClickOpen: () -> Unit = {},
        areSectionsLoading: () -> Boolean = { false },
        sections: () -> List<ArtEntrySection> = { emptyList() },
        onClickSave: () -> Unit = {},
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarErrorText(errorRes()?.first, onErrorDismiss = onErrorDismiss)
            },
            modifier = Modifier.imePadding()
        ) {
            var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

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
                        entryId = entryId,
                        entryImageFile = entryImageFile,
                        entryImageRatio = entryImageRatio,
                        loading = areSectionsLoading,
                        imageUri = imageUri,
                        onImageSelected = onImageSelected,
                        onImageSelectError = onImageSelectError,
                        onImageSizeResult = onImageSizeResult,
                        onImageClickOpen = onImageClickOpen,
                    )

                    ArtEntryForm(areSectionsLoading, sections)
                }

                AnimatedVisibility(
                    visible = !areSectionsLoading(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    ButtonFooter(
                        R.string.delete to { showDeleteDialog = true },
                        R.string.save to onClickSave,
                    )
                }
            }

            ArtEntryGrid.DeleteDialog(
                showDeleteDialog,
                { showDeleteDialog = false },
                onConfirmDelete
            )
        }
    }

    @Composable
    private fun HeaderImage(
        entryId: () -> String,
        entryImageFile: () -> File?,
        entryImageRatio: () -> Float,
        loading: () -> Boolean = { false },
        imageUri: () -> Uri?,
        onImageSelected: (Uri?) -> Unit,
        onImageSelectError: (Exception?) -> Unit,
        onImageSizeResult: (Int, Int) -> Unit = { _, _ -> },
        onImageClickOpen: () -> Unit,
    ) {
        Box {
            ImageSelectBox(onImageSelected, onImageSelectError) {
                @Suppress("NAME_SHADOWING")
                val imageUri = imageUri()
                if (imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            .listener { _, result ->
                                onImageSizeResult(
                                    result.drawable.intrinsicWidth,
                                    result.drawable.intrinsicHeight,
                                )
                            }
                            .build(),
                        contentDescription = stringResource(
                            R.string.art_entry_image_content_description
                        ),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                } else {
                    @Suppress("NAME_SHADOWING")
                    val entryImageFile = entryImageFile()
                    if (entryImageFile != null) {
                        @Suppress("NAME_SHADOWING")
                        val entryImageRatio = entryImageRatio()
                        @Suppress("NAME_SHADOWING")
                        val entryId = entryId()
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
                                    .placeholderMemoryCacheKey("coil_memory_entry_image_home_$entryId")
                                    .build(),
                                contentDescription = stringResource(
                                    R.string.art_entry_image_content_description
                                ),
                                contentScale = ContentScale.FillWidth,
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
                            imageVector = Icons.Default.ImageNotSupported,
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

            AnimatedVisibility(
                visible = !loading() && entryImageFile() != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                FloatingActionButton(
                    onClick = onImageClickOpen,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = stringResource(
                            R.string.art_entry_open_full_image_content_description
                        ),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun Preview(
    @PreviewParameter(SampleArtEntrySectionsProvider::class) sections: List<ArtEntrySection>
) {
    DetailsScreen(sections = { sections })
}