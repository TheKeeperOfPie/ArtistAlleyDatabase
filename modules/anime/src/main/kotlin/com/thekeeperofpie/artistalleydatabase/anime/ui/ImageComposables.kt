package com.thekeeperofpie.artistalleydatabase.anime.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComposeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally

val UpperHalfBiasAlignment = BiasAbsoluteAlignment(0f, -0.5f)

@Composable
fun MediaCoverImage(
    imageState: CoilImageState?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
) {
    CoilImage(
        state = imageState,
        model = imageState.request().build(),
        contentScale = contentScale,
        alignment = UpperHalfBiasAlignment,
        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
        onSuccess = onSuccess,
        onError = onError,
        contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
        modifier = modifier.blurForScreenshotMode(),
    )
}

@Composable
fun MediaCoverImage(
    imageState: CoilImageState,
    image: ImageRequest,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    CoilImage(
        state = imageState,
        model = image,
        contentScale = contentScale,
        alignment = UpperHalfBiasAlignment,
        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
        contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
        modifier = modifier.blurForScreenshotMode()
    )
}

@Composable
fun CharacterCoverImage(
    imageState: CoilImageState?,
    image: ImageRequest,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    CoilImage(
        state = imageState,
        model = image,
        contentScale = contentScale,
        alignment = UpperHalfBiasAlignment,
        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
        contentDescription = stringResource(R.string.anime_character_image_content_description),
        modifier = modifier.blurForScreenshotMode(),
    )
}

@Composable
fun StaffCoverImage(
    imageState: CoilImageState?,
    image: ImageRequest,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescriptionTextRes: Int = R.string.anime_staff_image,
) {
    CoilImage(
        state = imageState,
        model = image,
        contentDescription = stringResource(contentDescriptionTextRes),
        contentScale = contentScale,
        alignment = UpperHalfBiasAlignment,
        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
        modifier = modifier.blurForScreenshotMode(),
    )
}

@Composable
fun UserAvatarImage(
    imageState: CoilImageState?,
    image: ImageRequest,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescriptionTextRes: Int = R.string.anime_user_image,
) {
    CoilImage(
        state = imageState,
        model = image,
        contentDescription = stringResource(contentDescriptionTextRes),
        contentScale = contentScale,
        alignment = UpperHalfBiasAlignment,
        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
        modifier = modifier.blurForScreenshotMode(),
    )
}

private val ScreenshotBlur by lazy { Modifier.blur(6.dp) }

@Composable
fun Modifier.blurForScreenshotMode() =
    conditionally(LocalAnimeComposeSettings.current.screenshotMode) { ScreenshotBlur }
