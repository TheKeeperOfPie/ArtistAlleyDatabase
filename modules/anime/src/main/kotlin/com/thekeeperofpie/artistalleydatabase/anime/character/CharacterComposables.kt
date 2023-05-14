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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import kotlinx.coroutines.CoroutineScope

@Composable
fun CharacterCard(
    coroutineScope: CoroutineScope,
    id: String,
    image: String?,
    colorMap: MutableMap<String, Pair<Color, Color>>,
    onClick: (id: String) -> Unit,
    innerImage: String? = null,
    content: @Composable (textColor: Color) -> Unit,
) {
    val defaultTextColor = MaterialTheme.typography.bodyMedium.color
    val colors = colorMap[id]

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
        onClick = { onClick(id) },
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        modifier = Modifier.width(100.dp),
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .allowHardware(false)
                    .build(),
                contentScale = ContentScale.Crop,
                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                contentDescription = stringResource(
                    R.string.anime_media_character_image
                ),
                onSuccess = {
                    ComposeColorUtils.calculatePalette(
                        id = id,
                        scope = coroutineScope,
                        success = it,
                        colorMap = colorMap,
                        // Only capture left 3/5ths to ignore
                        // part covered by voice actor
                        widthEndThreshold = if (innerImage == null) 1f else 3 / 5f,
                    )
                },
                modifier = Modifier.size(width = 100.dp, height = 150.dp)
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
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(innerImage)
                            .crossfade(false)
                            .listener(onError = { _, _ ->
                                showInnerImage = false
                            }, onSuccess = { _, _ ->
                                showBorder = true
                            })
                            .build(),
                        contentScale = ContentScale.Crop,
                        contentDescription = stringResource(
                            R.string.anime_media_voice_actor_image
                        ),
                        modifier = Modifier
                            .size(width = 40.dp, height = 40.dp)
                            .alpha(alpha)
                            .align(Alignment.BottomEnd)
                            .clip(clipShape)
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
    onCharacterClicked: (String) -> Unit,
    onCharacterLongClicked: (String) -> Unit
) {
    if (characters.isEmpty()) return
    item {
        DetailsSectionHeader(stringResource(titleRes))
    }

    item {
        val coroutineScope = rememberCoroutineScope()
        // TODO: Even wider scoped cache?
        // Cache character color calculation
        val colorMap = remember { mutableStateMapOf<String, Pair<Color, Color>>() }

        val uriHandler = LocalUriHandler.current
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(characters, { it.id }) {
                CharacterCard(
                    coroutineScope = coroutineScope,
                    id = it.id,
                    image = it.image,
                    colorMap = colorMap,
                    onClick = { uriHandler.openUri(AniListUtils.characterUrl(it)) },
                    innerImage = (it.languageToVoiceActor["Japanese"]
                        ?: it.languageToVoiceActor.values.firstOrNull())?.image,
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
