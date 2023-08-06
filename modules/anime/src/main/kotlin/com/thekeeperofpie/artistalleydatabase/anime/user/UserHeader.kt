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
import com.anilist.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@Composable
fun UserHeader(
    screenKey: String,
    upIconOption: UpIconOption?,
    userId: String,
    progress: Float,
    name: @Composable () -> String,
    coverImage: @Composable () -> String?,
    coverImageWidthToHeightRatio: Float = 1f,
    bannerImage: String? = null,
) {
    CoverAndBannerHeader(
        screenKey = screenKey,
        upIconOption = upIconOption,
        entryId = EntryId("anime_user", userId),
        progress = progress,
        coverImage = coverImage,
        coverImageAllowHardware = true,
        coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
        bannerImage = bannerImage,
        pinnedHeight = 104.dp,
        coverSize = 180.dp,
    ) {
        AutoResizeHeightText(
            text = name(),
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
    val imageWidthToHeightRatio: Float = arguments?.getString("imageWidthToHeightRatio")
        ?.toFloatOrNull() ?: 1f,
    private val _name: String? = arguments?.getString("name"),
    private val _image: String? = arguments?.getString("image"),
    private val user: () -> UserNavigationData?,
) {
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

    val image
        get() = user()?.avatar?.large ?: _image
    val name
        get() = user()?.name ?: _name ?: ""
}
