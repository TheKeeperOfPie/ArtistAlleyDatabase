package com.thekeeperofpie.artistalleydatabase.detail

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import com.thekeeperofpie.artistalleydatabase.NavDestinations
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.ui.ArtEntryForm
import com.thekeeperofpie.artistalleydatabase.ui.ArtEntryForm.ImageSelectBox
import java.io.File

object DetailsScreen {

    @Composable
    operator fun invoke(
        entryId: String = "",
        entryImageFile: File? = null,
        entryImageRatio: Float = 1f,
        imageUri: Uri? = null,
        onImageSelected: (Uri?) -> Unit = {},
        onImageSelectError: (Exception?) -> Unit = {},
        areSectionsLoading: Boolean = false,
        artistSection: ArtEntryForm.FormSection = ArtEntryForm.FormSection(),
        locationSection: ArtEntryForm.FormSection = ArtEntryForm.FormSection(),
        seriesSection: ArtEntryForm.FormSection = ArtEntryForm.FormSection(),
        characterSection: ArtEntryForm.FormSection = ArtEntryForm.FormSection(),
        tagSection: ArtEntryForm.FormSection = ArtEntryForm.FormSection(),
        onClickSave: () -> Unit,
        errorRes: Pair<Int, Exception?>? = null,
        onErrorDismiss: () -> Unit = {},
    ) {
        ArtEntryForm(
            areSectionsLoading,
            artistSection,
            locationSection,
            seriesSection,
            characterSection,
            tagSection,
            onClickSave,
            errorRes,
            onErrorDismiss
        ) {
            HeaderImage(
                entryId,
                entryImageFile,
                entryImageRatio,
                imageUri,
                onImageSelected,
                onImageSelectError
            )
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