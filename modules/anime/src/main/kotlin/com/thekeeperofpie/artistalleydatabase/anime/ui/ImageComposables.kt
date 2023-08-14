package com.thekeeperofpie.artistalleydatabase.anime.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComposeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.conditionally

@Composable
fun MediaCoverImage(
    screenKey: String,
    mediaId: String?,
    image: ImageRequest,
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    SharedElement(
        key = "anime_media_${mediaId}_image",
        screenKey = screenKey,
    ) {
        AsyncImage(
            model = image,
            contentScale = contentScale,
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
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    SharedElement(
        key = "anime_character_${characterId}_image",
        screenKey = screenKey,
    ) {
        AsyncImage(
            model = image,
            contentScale = contentScale,
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
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    contentDescriptionTextRes: Int = R.string.anime_staff_image,
    modifier: Modifier = Modifier,
) {
    SharedElement(
        key = "anime_staff_${staffId}_image",
        screenKey = screenKey,
    ) {
        AsyncImage(
            model = image,
            contentDescription = stringResource(contentDescriptionTextRes),
            contentScale = contentScale,
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
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    contentDescriptionTextRes: Int = R.string.anime_user_image,
    modifier: Modifier = Modifier,
) {
    SharedElement(
        key = "anime_user_${userId}_image",
        screenKey = screenKey,
    ) {
        AsyncImage(
            model = image,
            contentDescription = stringResource(contentDescriptionTextRes),
            contentScale = contentScale,
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
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    contentDescriptionTextRes: Int = R.string.anime_user_image,
    modifier: Modifier = Modifier,
) {
    SharedElement(
        key = "anime_user_${userId}_image",
        screenKey = screenKey,
    ) {
        AsyncImage(
            model = image,
            contentDescription = stringResource(contentDescriptionTextRes),
            contentScale = contentScale,
            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
            onSuccess = onSuccess,
            modifier = modifier.blurForScreenshotMode(),
        )
    }
}

fun Modifier.blurForScreenshotMode() = composed {
    conditionally(LocalAnimeComposeSettings.current.screenshotMode) {
        blur(6.dp)
    }
}
