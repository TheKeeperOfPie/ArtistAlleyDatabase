package com.thekeeperofpie.artistalleydatabase.edit

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.art.ArtStringR
import com.thekeeperofpie.artistalleydatabase.art.details.SampleArtEntrySectionsProvider
import com.thekeeperofpie.artistalleydatabase.compose.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.topBorder
import com.thekeeperofpie.artistalleydatabase.form.EntryForm
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.form.ImageSelectBox
import com.thekeeperofpie.artistalleydatabase.utils.Either
import java.io.File

object MultiEditScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        imageUris: () -> List<Either<File, Uri?>> = { emptyList() },
        onImageSelected: (index: Int, uri: Uri?) -> Unit = { _, _ -> },
        onImageSelectError: (Exception?) -> Unit = {},
        loading: () -> Boolean = { false },
        sections: () -> List<EntrySection> = { emptyList() },
        saving: () -> Boolean = { false },
        onClickSave: () -> Unit = {},
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = {},
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarErrorText(errorRes()?.first, onErrorDismiss = onErrorDismiss)
            }
        ) {
            Column(Modifier.padding(it)) {
                Column(
                    modifier = Modifier
                        .weight(1f, true)
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                ) {
                    HeaderImage(
                        imageUris,
                        onImageSelected,
                        onImageSelectError,
                    )

                    EntryForm(loading, sections)
                }



                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .topBorder(1.dp, MaterialTheme.colorScheme.inversePrimary)
                ) {
                    TextButton(onClick = onClickSave) {
                        Crossfade(targetState = saving()) {
                            if (it) {
                                CircularProgressIndicator()
                            } else {
                                Text(
                                    text = stringResource(R.string.save),
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 10.dp,
                                        bottom = 10.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    private fun HeaderImage(
        imageUris: () -> List<Either<File, Uri?>> = { emptyList() },
        onImageSelected: (index: Int, uri: Uri?) -> Unit = { _, _ -> },
        onImageSelectError: (Exception?) -> Unit = {},
    ) {
        Box {
            @Suppress("NAME_SHADOWING")
            val imageUris = imageUris()
            val pagerState = rememberPagerState()
            HorizontalPager(
                state = pagerState,
                count = imageUris.size,
                modifier = Modifier.heightIn(min = 200.dp, max = 400.dp)
            ) { index ->
                ImageSelectBox({ onImageSelected(index, it) }, onImageSelectError) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUris[index].eitherValueUnchecked())
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(
                            ArtStringR.art_entry_image_content_description
                        ),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            if (imageUris.size > 1) {
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview(
    @PreviewParameter(SampleArtEntrySectionsProvider::class) sections: List<EntrySection>
) {
    MultiEditScreen(sections = { sections })
}