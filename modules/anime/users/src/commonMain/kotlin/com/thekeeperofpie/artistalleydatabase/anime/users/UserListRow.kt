package com.thekeeperofpie.artistalleydatabase.anime.users

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.users.generated.resources.Res
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_image_long_press_preview
import coil3.annotation.ExperimentalCoilApi
import com.anilist.data.fragment.UserNavigationData
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.users.data.UserEntryProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class, ExperimentalCoilApi::class
)
object UserListRow {

    private val MIN_HEIGHT = 156.dp
    private val IMAGE_WIDTH = 108.dp

    @Composable
    operator fun <MediaEntry> invoke(
        entry: Entry<MediaEntry>?,
        mediaRow: LazyListScope.(List<MediaEntry?>) -> Unit,
    ) {
        val avatarImageState = rememberCoilImageState(entry?.user?.avatar?.large)
        val navigationController = LocalNavigationController.current
        val sharedTransitionKey = entry?.user?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        val onUserClick = {
            if (entry != null) {
                navigationController.navigate(
                    UserDestinations.User(
                        userId = entry.user.id.toString(),
                        sharedTransitionKey = sharedTransitionKey,
                        headerParams = UserHeaderParams(
                            name = entry.user.name,
                            bannerImage = null,
                            coverImage = avatarImageState.toImageState(),
                        ),
                    )
                )
            }
        }
        ElevatedCard(
            onClick = onUserClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MIN_HEIGHT)
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                UserImage(
                    entry = entry,
                    imageState = avatarImageState,
                    onClick = onUserClick,
                    modifier = Modifier.sharedElement(sharedTransitionKey, "user_image")
                )

                Column(
                    modifier = Modifier
                        .heightIn(min = MIN_HEIGHT)
                        .padding(bottom = 12.dp)
                ) {
                    NameText(
                        entry = entry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(Alignment.Top)
                    )

                    Spacer(Modifier.weight(1f))

                    MediaRow(
                        entry = entry,
                        mediaRow = mediaRow,
                    )
                }
            }
        }
    }

    @Composable
    private fun UserImage(
        entry: Entry<*>?,
        onClick: () -> Unit,
        imageState: CoilImageState,
        modifier: Modifier,
    ) {
        val fullscreenImageHandler = LocalFullscreenImageHandler.current
        UserAvatarImage(
            imageState = imageState,
            image = imageState.request().build(),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .heightIn(min = MIN_HEIGHT)
                .width(IMAGE_WIDTH)
                .then(modifier)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        entry?.user?.avatar?.large?.let(fullscreenImageHandler::openImage)
                    },
                    onLongClickLabel = stringResource(
                        Res.string.anime_user_image_long_press_preview
                    ),
                )
        )
    }

    @Composable
    private fun NameText(entry: Entry<*>?, modifier: Modifier = Modifier) {
        AutoHeightText(
            text = entry?.user?.name ?: "Placeholder username",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun <MediaEntry> MediaRow(
        entry: Entry<MediaEntry>?,
        mediaRow: LazyListScope.(List<MediaEntry?>) -> Unit,
    ) {
        val media = entry?.media?.takeIf { it.isNotEmpty() } ?: return
        LazyRow(
            contentPadding = PaddingValues(start = 16.dp, end = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(top = 8.dp)
                // SubcomposeLayout doesn't support fill max width, so use a really large number.
                // The parent will clamp the actual width so all content still fits on screen.
                .size(width = LocalWindowConfiguration.current.screenWidthDp, height = 120.dp)
                .fadingEdgeEnd()
        ) {
            mediaRow(media)
        }
    }

    data class Entry<MediaEntry>(val user: UserNavigationData, val media: List<MediaEntry>) {
        class Provider<MediaEntry> : UserEntryProvider<UserNavigationData, Entry<MediaEntry>, MediaEntry> {
            override fun userEntry(
                user: UserNavigationData,
                media: List<MediaEntry>,
            ) = Entry(user, media)

            override fun id(userEntry: Entry<MediaEntry>) = userEntry.user.id.toString()

            override fun media(userEntry: Entry<MediaEntry>) = userEntry.media

            override fun copyUserEntry(
                entry: Entry<MediaEntry>,
                media: List<MediaEntry>,
            ) = entry.copy(media = media)
        }
    }
}
