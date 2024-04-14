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
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComposeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.conditionally

val UpperHalfBiasAlignment = BiasAbsoluteAlignment(0f, -0.5f)

@Composable
fun MediaCoverImage(
    screenKey: String,
    mediaId: String?,
    image: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
) {
    SharedElement(
        key = "anime_media_${mediaId}_image",
        screenKey = screenKey,
    ) {
        AsyncImage(
            model = image,
            contentScale = contentScale,
            alignment = UpperHalfBiasAlignment,
            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
            onSuccess = onSuccess,
            onError = onError,
            contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
            modifier = modifier.blurForScreenshotMode(),
        )
    }
}

@Composable
fun MediaCoverImageNoSharedElement(
    image: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
) {
    AsyncImage(
        model = image,
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
    screenKey: String,
    mediaId: String?,
    image: ImageRequest,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
) {
    SharedElement(
        key = "anime_media_${mediaId}_image",
        screenKey = screenKey,
    ) {
        AsyncImage(
            model = image,
            contentScale = contentScale,
            alignment = UpperHalfBiasAlignment,
            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
            onSuccess = onSuccess,
            onError = onError,
            contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
            modifier = modifier.blurForScreenshotMode(),
        )
    }
}

@Composable
fun CharacterCoverImage(
    screenKey: String,
    characterId: String?,
    image: ImageRequest,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
) {
    SharedElement(
        key = "anime_character_${characterId}_image",
        screenKey = screenKey,
    ) {
        AsyncImage(
            model = image,
            contentScale = contentScale,
            alignment = UpperHalfBiasAlignment,
            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
            onSuccess = onSuccess,
            contentDescription = stringResource(R.string.anime_character_image_content_description),
            modifier = modifier.blurForScreenshotMode(),
        )
    }
}

@Composable
fun StaffCoverImage(
    screenKey: String,
    staffId: String?,
    image: ImageRequest,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    contentDescriptionTextRes: Int = R.string.anime_staff_image,
) {
    SharedElement(
        key = "anime_staff_${staffId}_image",
        screenKey = screenKey,
    ) {
        AsyncImage(
            model = image,
            contentDescription = stringResource(contentDescriptionTextRes),
            contentScale = contentScale,
            alignment = UpperHalfBiasAlignment,
            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
            onSuccess = onSuccess,
            modifier = modifier.blurForScreenshotMode(),
        )
    }
}

@Composable
fun UserAvatarImage(
    screenKey: String,
    userId: String?,
    image: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    contentDescriptionTextRes: Int = R.string.anime_user_image,
) {
    SharedElement(
        key = "anime_user_${userId}_image",
        screenKey = screenKey,
    ) {
        AsyncImage(
            model = image,
            contentDescription = stringResource(contentDescriptionTextRes),
            contentScale = contentScale,
            alignment = UpperHalfBiasAlignment,
            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
            onSuccess = onSuccess,
            modifier = modifier.blurForScreenshotMode(),
        )
    }
}

@Composable
fun UserAvatarImage(
    screenKey: String,
    userId: String?,
    image: ImageRequest,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    contentDescriptionTextRes: Int = R.string.anime_user_image,
) {
    SharedElement(
        key = "anime_user_${userId}_image",
        screenKey = screenKey,
    ) {
        AsyncImage(
            model = image,
            contentDescription = stringResource(contentDescriptionTextRes),
            contentScale = contentScale,
            alignment = UpperHalfBiasAlignment,
            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
            onSuccess = onSuccess,
            modifier = modifier.blurForScreenshotMode(),
        )
    }
}

private val ScreenshotBlur by lazy { Modifier.blur(6.dp) }

@Composable
fun Modifier.blurForScreenshotMode() =
    conditionally(LocalAnimeComposeSettings.current.screenshotMode) { ScreenshotBlur }
