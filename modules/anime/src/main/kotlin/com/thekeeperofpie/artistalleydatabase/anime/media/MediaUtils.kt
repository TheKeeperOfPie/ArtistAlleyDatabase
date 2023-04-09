package com.thekeeperofpie.artistalleydatabase.anime.media

import android.view.animation.PathInterpolator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.twotone._18UpRating
import androidx.compose.ui.graphics.Color
import com.thekeeperofpie.artistalleydatabase.anime.R

object MediaUtils {

    // Uses a cubic bezier to interpolate tag IDs to more distinct colors,
    // as the tag IDs are not uniformly distributed
    private val interpolator = PathInterpolator(0.35f, 0.9f, 0.39f, 0.39f)

    // TODO: More distinct colors
    fun calculateTagColor(tagId: Int) = Color.hsl(
        hue = interpolator.getInterpolation((tagId % 2000) / 2000f) * 360,
        lightness = 0.25f,
        saturation = 0.25f,
    )

    fun tagLeadingIcon(
        isAdult: Boolean? = false,
        isGeneralSpoiler: Boolean? = false,
        isMediaSpoiler: Boolean? = null
    ) = when {
        isAdult ?: false -> Icons.TwoTone._18UpRating
        (isGeneralSpoiler ?: false) || (isMediaSpoiler ?: false) ->
            Icons.Filled.Warning
        else -> null
    }

    fun tagLeadingIconContentDescription(
        isAdult: Boolean? = false,
        isGeneralSpoiler: Boolean? = false,
        isMediaSpoiler: Boolean? = null
    ) = when {
        isAdult ?: false -> R.string.anime_media_tag_is_adult
        (isGeneralSpoiler ?: false) || (isMediaSpoiler
            ?: false) -> R.string.anime_media_tag_is_spoiler
        else -> null
    }
}