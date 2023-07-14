package com.thekeeperofpie.artistalleydatabase.anime.staff

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
import com.anilist.fragment.StaffHeaderData
import com.anilist.fragment.StaffNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

// TODO: Collapse this with CharacterHeader?
@Composable
fun StaffHeader(
    staffId: String,
    progress: Float,
    headerValues: StaffHeaderValues,
    colorCalculationState: ColorCalculationState,
    onImageWidthToHeightRatioAvailable: (Float) -> Unit = {},
) {
    CoverAndBannerHeader(
        screenKey = AnimeNavDestinations.STAFF_DETAILS.id,
        entryId = EntryId("anime_staff", staffId),
        progress = progress,
        color = { headerValues.color(colorCalculationState) },
        coverImage = { headerValues.image },
        coverImageWidthToHeightRatio = headerValues.imageWidthToHeightRatio,
        coverImageOnSuccess = {
            onImageWidthToHeightRatioAvailable(it.widthToHeightRatio())
            ComposeColorUtils.calculatePalette(staffId, it, colorCalculationState)
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

        val subtitleText = headerValues.subtitle
        AnimatedVisibility(subtitleText.isNotEmpty(), label = "Staff details subtitle text") {
            if (subtitleText.isNotEmpty()) {
                Text(
                    text = subtitleText,
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

class StaffHeaderValues(
    arguments: Bundle,
    val imageWidthToHeightRatio: Float = arguments.getString("imageWidthToHeightRatio")
        ?.toFloatOrNull() ?: 1f,
    private val _name: String? = arguments.getString("name"),
    private val _subtitle: String? = arguments.getString("subtitle"),
    private val _image: String? = arguments.getString("image"),
    private val _color: Color? = arguments.getString("color")
        ?.toIntOrNull()
        ?.let(::Color),
    private val staff: () -> StaffHeaderData?,
) {
    companion object {
        const val routeSuffix = "&name={name}" +
                "&subtitle={subtitle}" +
                "&image={image}" +
                "&imageWidthToHeightRatio={imageWidthToHeightRatio}" +
                "&color={color}"

        fun routeSuffix(
            staff: StaffHeaderData?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) = if (staff == null) "" else routeSuffix(
            name = staff.name?.userPreferred,
            subtitle = StaffUtils.subtitleName(
                userPreferred = staff.name?.userPreferred,
                native = staff.name?.native,
                full = staff.name?.full,
            ),
            image = staff.image?.large,
            imageWidthToHeightRatio = imageWidthToHeightRatio,
            color = color,
        )

        fun routeSuffix(
            staff: StaffNavigationData?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) = if (staff == null) "" else routeSuffix(
            name = staff.name?.userPreferred,
            subtitle = null,
            image = staff.image?.large,
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
        get() = staff()?.image?.large ?: _image
    val name
        get() = staff()?.name?.userPreferred ?: _name ?: ""
    val subtitle
        get() = staff()?.name?.run {
            StaffUtils.subtitleName(
                userPreferred = userPreferred,
                native = native,
                full = full,
            )
        } ?: _subtitle ?: ""

    fun color(colorCalculationState: ColorCalculationState) =
        colorCalculationState.getColors(staff()?.id?.toString()).first
            .takeOrElse { _color ?: Color.Unspecified }
}
