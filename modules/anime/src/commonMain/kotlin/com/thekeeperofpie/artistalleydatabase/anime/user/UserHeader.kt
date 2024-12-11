package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anilist.data.UserByIdQuery
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsHeaderValues
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.maybeOverride
import kotlinx.serialization.Serializable

@Composable
fun UserHeader(
    upIconOption: UpIconOption?,
    progress: Float,
    headerValues: UserHeaderValues,
) {
    CoverAndBannerHeader(
        upIconOption = upIconOption,
        headerValues = headerValues,
        // TODO: SharedTransitionKey
        sharedTransitionKey = null,
        coverImageSharedTransitionIdentifier = "user_image",
        bannerImageSharedTransitionIdentifier = "user_banner_image",
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

@Serializable
data class UserHeaderParams(
    val name: String?,
    val bannerImage: ImageState?,
    val coverImage: ImageState?,
)

class UserHeaderValues(
    private val params: UserHeaderParams?,
    val user: () -> UserByIdQuery.Data.User?,
) : DetailsHeaderValues {
    override val coverImage
        get() = params?.coverImage.maybeOverride(user()?.avatar?.large)
    override val bannerImage
        get() = params?.bannerImage.maybeOverride(user()?.bannerImage)
    val name
        get() = user()?.name ?: params?.name ?: ""
}
