package com.thekeeperofpie.artistalleydatabase.detail

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.NavDestinations
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

object DetailScreen {

    @Composable
    operator fun invoke(entryId: String, entryImageFile: File?, entryImageRatio: Float) {
        ArtistAlleyDatabaseTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Content(entryId, entryImageFile, entryImageRatio)
            }
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    private fun Content(entryId: String, entryImageFile: File?, entryImageRatio: Float) {
        Box(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            if (entryImageFile != null) {
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