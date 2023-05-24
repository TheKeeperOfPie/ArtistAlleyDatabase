@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.StaffNavigationData
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@Composable
fun CharacterCard(
    id: String,
    image: String?,
    colorCalculationState: ColorCalculationState,
    onClick: () -> Unit,
    innerImage: String? = null,
    onClickInnerImage: (() -> Unit)? = null,
    onImageSuccess: (AsyncImagePainter.State.Success) -> Unit = {},
    onInnerImageSuccess: (AsyncImagePainter.State.Success) -> Unit = {},
    width: Dp = 100.dp,
    content: @Composable (textColor: Color) -> Unit,
) {
    val defaultTextColor = MaterialTheme.typography.bodyMedium.color
    val colors = colorCalculationState.colorMap[id]

    val animationProgress by animateIntAsState(
        if (colors == null) 0 else 255,
        label = "Character card color fade in",
    )

    val containerColor = when {
        colors == null || animationProgress == 0 ->
            MaterialTheme.colorScheme.surface
        animationProgress == 255 -> colors.first
        else -> Color(
            ColorUtils.compositeColors(
                ColorUtils.setAlphaComponent(
                    colors.first.toArgb(),
                    animationProgress
                ),
                MaterialTheme.colorScheme.surface.toArgb()
            )
        )
    }

    val textColor = when {
        colors == null || animationProgress == 0 -> defaultTextColor
        animationProgress == 255 -> colors.second
        else -> Color(
            ColorUtils.compositeColors(
                ColorUtils.setAlphaComponent(
                    colors.second.toArgb(),
                    animationProgress
                ),
                defaultTextColor.toArgb()
            )
        )
    }

    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        modifier = Modifier.width(width),
    ) {
        Box {
            val density = LocalDensity.current
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .allowHardware(colorCalculationState.hasColor(id))
                    .size(
                        width = density.run { width.roundToPx() },
                        height = density.run { (width * 1.5f).roundToPx() },
                    )
                    .build(),
                contentScale = ContentScale.Crop,
                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                contentDescription = stringResource(
                    R.string.anime_media_character_image_content_description
                ),
                onSuccess = {
                    onImageSuccess(it)
                    ComposeColorUtils.calculatePalette(
                        id = id,
                        success = it,
                        colorCalculationState = colorCalculationState,
                        heightStartThreshold = 3 / 4f,
                        // Only capture left 3/5ths to ignore
                        // part covered by voice actor
                        widthEndThreshold = if (innerImage == null) 1f else 3 / 5f,
                        selectMaxPopulation = true,
                    )
                },
                modifier = Modifier.size(width = width, height = width * 1.5f)
            )

            if (innerImage != null) {
                var showInnerImage by remember { mutableStateOf(true) }
                if (showInnerImage) {
                    var showBorder by remember(id) { mutableStateOf(false) }
                    val alpha by animateFloatAsState(
                        if (showBorder) 1f else 0f,
                        label = "Character card inner image fade",
                    )
                    val clipShape = RoundedCornerShape(topStart = 8.dp)
                    val size = LocalDensity.current.run { 40.dp.roundToPx() }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(innerImage)
                            .crossfade(false)
                            .listener(onError = { _, _ ->
                                showInnerImage = false
                            }, onSuccess = { _, _ ->
                                showBorder = true
                            })
                            .size(width = size, height = size)
                            .build(),
                        contentScale = ContentScale.Crop,
                        contentDescription = stringResource(
                            R.string.anime_media_voice_actor_image
                        ),
                        onSuccess = onInnerImageSuccess,
                        modifier = Modifier
                            .size(width = 40.dp, height = 40.dp)
                            .alpha(alpha)
                            .align(Alignment.BottomEnd)
                            .clip(clipShape)
                            .optionalClickable(onClickInnerImage)
                            .border(
                                width = 1.dp,
                                color = containerColor,
                                shape = clipShape
                            )
                    )
                }
            }
        }

        content(textColor)
    }
}

fun LazyListScope.charactersSection(
    @StringRes titleRes: Int,
    characters: List<DetailsCharacter>,
    onCharacterClick: (CharacterNavigationData, imageWidthToHeightRatio: Float) -> Unit,
    onCharacterLongClick: (String) -> Unit,
    onStaffClick: (StaffNavigationData, imageWidthToHeightRatio: Float) -> Unit,
    colorCalculationState: ColorCalculationState,
) {
    if (characters.isEmpty()) return
    item {
        DetailsSectionHeader(stringResource(titleRes))
    }

    item {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(characters, { it.id }) {
                var imageWidthToHeightRatio by remember { MutableSingle(1f) }
                var innerImageWidthToHeightRatio by remember { MutableSingle(1f) }
                val voiceActor = (it.languageToVoiceActor["Japanese"]
                    ?: it.languageToVoiceActor.values.firstOrNull())
                CharacterCard(
                    id = it.id,
                    image = it.image,
                    colorCalculationState = colorCalculationState,
                    onClick = {
                        it.character?.let {
                            onCharacterClick(it, imageWidthToHeightRatio)
                        }
                    },
                    innerImage = voiceActor?.image,
                    onClickInnerImage = voiceActor?.image?.let {
                        {
                            onStaffClick(voiceActor.staff, innerImageWidthToHeightRatio)
                        }
                    },
                    onImageSuccess = { imageWidthToHeightRatio = it.widthToHeightRatio() },
                    onInnerImageSuccess = {
                        innerImageWidthToHeightRatio = it.widthToHeightRatio()
                    },
                ) { textColor ->
                    AutoHeightText(
                        text = it.name.orEmpty(),
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineBreak = LineBreak(
                                strategy = LineBreak.Strategy.Balanced,
                                strictness = LineBreak.Strictness.Strict,
                                wordBreak = LineBreak.WordBreak.Default,
                            )
                        ),
                        minTextSizeSp = 8f,
                        modifier = Modifier
                            .size(width = 100.dp, height = 56.dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
