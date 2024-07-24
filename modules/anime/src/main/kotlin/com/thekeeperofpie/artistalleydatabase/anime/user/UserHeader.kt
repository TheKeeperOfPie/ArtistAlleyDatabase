package com.thekeeperofpie.artistalleydatabase.anime.user

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.anilist.UserByIdQuery
import com.anilist.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsHeaderValues
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ImageState
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

@Composable
fun UserHeader(
    upIconOption: UpIconOption?,
    progress: Float,
    headerValues: UserHeaderValues,
) {
    CoverAndBannerHeader(
        upIconOption = upIconOption,
        headerValues = headerValues,
        coverImageAllowHardware = true,
        pinnedHeight = 104.dp,
        progress = progress,
        coverSize = 180.dp,
    ) {
        AutoResizeHeightText(
            text = headerValues.name,
            style = MaterialTheme.typography.headlineLarge,
            maxLines = 1,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 10.dp,
                    bottom = 10.dp
                ),
        )
    }
}

class UserHeaderValues(
    arguments: Bundle?,
    val coverImageWidthToHeightRatio: Float = arguments?.getString("imageWidthToHeightRatio")
        ?.toFloatOrNull() ?: 1f,
    private val _name: String? = arguments?.getString("name"),
    private val _image: String? = arguments?.getString("image"),
    private val user: () -> UserByIdQuery.Data.User?,
) : DetailsHeaderValues {
    companion object {
        const val routeSuffix = "&name={name}" +
                "&image={image}" +
                "&imageWidthToHeightRatio={imageWidthToHeightRatio}"

        fun routeSuffix(
            user: UserNavigationData?,
            imageWidthToHeightRatio: Float,
        ) = if (user == null) "" else routeSuffix(
            name = user.name,
            image = user.avatar?.large,
            imageWidthToHeightRatio = imageWidthToHeightRatio,
        )

        private fun routeSuffix(
            name: String?,
            image: String?,
            imageWidthToHeightRatio: Float,
        ) = "&name=$name" +
                "&image=$image" +
                "&imageWidthToHeightRatio=$imageWidthToHeightRatio"

        fun navArguments() = listOf(
            "name",
            "image",
            "imageWidthToHeightRatio",
        ).map {
            navArgument(it) {
                type = NavType.StringType
                nullable = true
            }
        }
    }

    // TODO:
    override val bannerImage
        get() = ImageState(user()?.bannerImage)
    override val coverImage
        get() = ImageState(user()?.avatar?.large ?: _image)
    val name
        get() = user()?.name ?: _name ?: ""
}
