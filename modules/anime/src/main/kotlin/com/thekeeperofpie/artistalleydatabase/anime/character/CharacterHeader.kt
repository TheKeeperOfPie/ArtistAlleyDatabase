package com.thekeeperofpie.artistalleydatabase.anime.character

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.anilist.fragment.CharacterHeaderData
import com.anilist.fragment.CharacterNavigationData
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@Composable
fun CharacterHeader(
    screenKey: String,
    characterId: String,
    progress: Float,
    headerValues: CharacterHeaderValues,
    colorCalculationState: ColorCalculationState,
    onImageWidthToHeightRatioAvailable: (Float) -> Unit = {},
) {
    SharedElement(
        key = "anime_character_${characterId}_header",
        screenKey = screenKey,
    ) {
        CoverAndBannerHeader(
            screenKey = AnimeNavDestinations.CHARACTER_DETAILS.id,
            entryId = EntryId("anime_character", characterId),
            progress = progress,
            color = { headerValues.color(colorCalculationState) },
            coverImage = { headerValues.image },
            coverImageWidthToHeightRatio = headerValues.imageWidthToHeightRatio,
            coverImageOnSuccess = {
                onImageWidthToHeightRatioAvailable(it.widthToHeightRatio())
                ComposeColorUtils.calculatePalette(characterId, it, colorCalculationState)
            }
        ) {
            AutoResizeHeightText(
                text = headerValues.name,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            )

            val subtitle = headerValues.subtitle
            AnimatedVisibility(subtitle.isNotEmpty(), label = "Character details subtitle text") {
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .wrapContentHeight(Alignment.Bottom)
                    )
                }
            }
        }
    }
}

class CharacterHeaderValues(
    arguments: Bundle,
    val imageWidthToHeightRatio: Float = arguments.getString("imageWidthToHeightRatio")
        ?.toFloatOrNull() ?: 1f,
    private val _name: String? = arguments.getString("name"),
    private val _subtitle: String? = arguments.getString("subtitle"),
    private val _image: String? = arguments.getString("image"),
    private val _color: Color? = arguments.getString("color")
        ?.toIntOrNull()
        ?.let(::Color),
    private val character: () -> CharacterHeaderData?,
) {
    companion object {
        const val routeSuffix = "&name={name}" +
                "&subtitle={subtitle}" +
                "&image={image}" +
                "&imageWidthToHeightRatio={imageWidthToHeightRatio}" +
                "&color={color}"

        fun routeSuffix(
            character: CharacterHeaderData?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) = if (character == null) "" else routeSuffix(
            name = character.name?.userPreferred,
            subtitle = CharacterUtils.subtitleName(
                userPreferred = character.name?.userPreferred,
                native = character.name?.native,
                full = character.name?.full,
            ),
            image = character.image?.large,
            imageWidthToHeightRatio = imageWidthToHeightRatio,
            color = color,
        )

        fun routeSuffix(
            character: CharacterNavigationData?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) = if (character == null) "" else routeSuffix(
            name = character.name?.userPreferred,
            subtitle = null,
            image = character.image?.large,
            imageWidthToHeightRatio = imageWidthToHeightRatio,
            color = color,
        )

        private fun routeSuffix(
            name: String?,
            subtitle: String?,
            image: String?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) = "&name=$name" +
                "&subtitle=$subtitle" +
                "&image=$image" +
                "&imageWidthToHeightRatio=$imageWidthToHeightRatio" +
                "&color=${color?.toArgb()}"

        fun navArguments() = listOf(
            "name",
            "subtitle",
            "image",
            "imageWidthToHeightRatio",
            "color",
        ).map {
            navArgument(it) {
                type = NavType.StringType
                nullable = true
            }
        }
    }

    val image
        get() = character()?.image?.large ?: _image
    val name
        get() = character()?.name?.userPreferred ?: _name ?: ""
    val subtitle
        get() = character()?.name?.run {
            CharacterUtils.subtitleName(
                userPreferred = userPreferred,
                native = native,
                full = full,
            )
        } ?: _subtitle ?: ""

    fun color(colorCalculationState: ColorCalculationState) =
        colorCalculationState.getColors(character()?.id?.toString()).first
            .takeOrElse { _color ?: Color.Unspecified }
}
