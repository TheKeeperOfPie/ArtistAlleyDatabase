package com.thekeeperofpie.artistalleydatabase.anime.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import artistalleydatabase.modules.anime.ui.generated.resources.Res
import artistalleydatabase.modules.anime.ui.generated.resources.anime_character_image_content_description
import artistalleydatabase.modules.anime.ui.generated.resources.anime_media_cover_image_content_description
import artistalleydatabase.modules.anime.ui.generated.resources.anime_staff_image
import artistalleydatabase.modules.anime.ui.generated.resources.anime_user_image
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.blurForScreenshotMode
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val UpperHalfBiasAlignment = BiasAbsoluteAlignment(0f, -0.5f)

@Composable
fun MediaCoverImage(
    imageState: CoilImageState?,
    modifier: Modifier = Modifier.Companion,
    contentScale: ContentScale = ContentScale.Companion.Fit,
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
        contentDescription = stringResource(Res.string.anime_media_cover_image_content_description),
        modifier = modifier.blurForScreenshotMode(),
    )
}

@Composable
fun MediaCoverImage(
    imageState: CoilImageState,
    image: ImageRequest,
    modifier: Modifier = Modifier.Companion,
    contentScale: ContentScale = ContentScale.Companion.Fit,
) {
    CoilImage(
        state = imageState,
        model = image,
        contentScale = contentScale,
        alignment = UpperHalfBiasAlignment,
        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
        contentDescription = stringResource(Res.string.anime_media_cover_image_content_description),
        modifier = modifier.blurForScreenshotMode()
    )
}

@Composable
fun CharacterCoverImage(
    imageState: CoilImageState?,
    image: ImageRequest,
    modifier: Modifier = Modifier.Companion,
    contentScale: ContentScale = ContentScale.Companion.Fit,
) {
    CoilImage(
        state = imageState,
        model = image,
        contentScale = contentScale,
        alignment = UpperHalfBiasAlignment,
        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
        contentDescription = stringResource(Res.string.anime_character_image_content_description),
        modifier = modifier.blurForScreenshotMode(),
    )
}

@Composable
fun StaffCoverImage(
    imageState: CoilImageState?,
    image: ImageRequest,
    modifier: Modifier = Modifier.Companion,
    contentScale: ContentScale = ContentScale.Companion.Fit,
    contentDescriptionTextRes: StringResource = Res.string.anime_staff_image,
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
    modifier: Modifier = Modifier.Companion,
    contentScale: ContentScale = ContentScale.Companion.Fit,
    contentDescriptionTextRes: StringResource = Res.string.anime_user_image,
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
