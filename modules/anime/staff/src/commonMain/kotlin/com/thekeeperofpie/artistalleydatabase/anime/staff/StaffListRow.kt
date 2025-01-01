package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.staff.generated.resources.Res
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_image_long_press_preview
import coil3.annotation.ExperimentalCoilApi
import com.anilist.data.StaffSearchQuery
import com.anilist.data.UserFavoritesStaffQuery
import com.anilist.data.fragment.CharacterNavigationData
import com.anilist.data.fragment.StaffNavigationData
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowFavoritesSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffCoverImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class,
    ExperimentalCoilApi::class
)
object StaffListRow {

    private val MIN_HEIGHT = 156.dp
    private val IMAGE_WIDTH = 108.dp

    @Composable
    operator fun <MediaEntry> invoke(
        entry: Entry<MediaEntry>?,
        charactersSection: LazyListScope.(List<CharacterNavigationData>) -> Unit,
        mediaSection: LazyListScope.(List<MediaEntry>) -> Unit,
    ) {
        val coverImageState = rememberCoilImageState(entry?.staff?.image?.large)
        val navigationController = LocalNavigationController.current
        val staffName = entry?.staff?.name?.primaryName()
        val staffSubtitle = entry?.staff?.name?.subtitleName()
        val sharedTransitionKey = entry?.staff?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        val onClick = {
            if (entry != null) {
                navigationController.navigate(
                    StaffDestinations.StaffDetails(
                        staffId = entry.staff.id.toString(),
                        sharedTransitionKey = sharedTransitionKey,
                        headerParams = StaffHeaderParams(
                            name = staffName,
                            subtitle = staffSubtitle,
                            coverImage = coverImageState.toImageState(),
                            favorite = null,
                        )
                    )
                )
            }
        }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MIN_HEIGHT)
                .clickable(
                    enabled = true, // TODO: placeholder,
                    onClick = onClick,
                )
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                StaffImage(
                    entry = entry,
                    coverImageState = coverImageState,
                    onClick = onClick,
                )

                Column(
                    modifier = Modifier
                        .heightIn(min = MIN_HEIGHT)
                        .padding(bottom = 12.dp)
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            NameText(entry = entry)
                            OccupationsText(entry = entry)
                        }

                        ListRowFavoritesSection(
                            loading = entry == null,
                            favorites = entry?.favorites,
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    CharactersAndMediaRow(
                        entry = entry,
                        charactersSection = charactersSection,
                        mediaSection = mediaSection,
                    )
                }
            }
        }
    }

    @Composable
    private fun <MediaEntry> StaffImage(
        entry: Entry<MediaEntry>?,
        coverImageState: CoilImageState,
        onClick: () -> Unit,
    ) {
        val fullscreenImageHandler = LocalFullscreenImageHandler.current
        StaffCoverImage(
            imageState = coverImageState,
            image = coverImageState.request().build(),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .fillMaxHeight()
                .heightIn(min = MIN_HEIGHT)
                .width(IMAGE_WIDTH)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        entry?.staff?.image?.large?.let(fullscreenImageHandler::openImage)
                    },
                    onLongClickLabel = stringResource(
                        Res.string.anime_staff_image_long_press_preview
                    ),
                ),
            contentScale = ContentScale.Crop
        )
    }

    @Composable
    private fun <MediaEntry> NameText(entry: Entry<MediaEntry>?, modifier: Modifier = Modifier) {
        Text(
            text = entry?.staff?.name?.primaryName() ?: "Loading...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun <MediaEntry> OccupationsText(entry: Entry<MediaEntry>?) {
        if (entry?.occupations?.isEmpty() != false) return
        Text(
            text = entry.occupations.joinToString(separator = " - "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current }
                .copy(alpha = 0.8f),
            modifier = Modifier
                .wrapContentHeight()
                .padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 10.dp)
                .placeholder(
                    visible = false, // TODO: placeholder,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun <MediaEntry> CharactersAndMediaRow(
        entry: Entry<MediaEntry>?,
        charactersSection: LazyListScope.(List<CharacterNavigationData>) -> Unit,
        mediaSection: LazyListScope.(List<MediaEntry>) -> Unit,
    ) {
        val media = entry?.media.orEmpty()
        val characters = entry?.characters.orEmpty()
        if (media.isEmpty() && characters.isEmpty()) return
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                // SubcomposeLayout doesn't support fill max width, so use a really large number.
                // The parent will clamp the actual width so all content still fits on screen.
                .size(width = LocalWindowConfiguration.current.screenWidthDp, height = 120.dp)
        ) {
            charactersSection(characters)
            mediaSection(media)
        }
    }

    data class Entry<MediaEntry>(
        val staff: StaffNavigationData,
        val media: List<MediaEntry>,
        val characters: List<CharacterNavigationData>,
        val favorites: Int?,
        val occupations: List<String>,
    ) {
        constructor(
            searchStaff: StaffSearchQuery.Data.Page.Staff,
            media: List<MediaEntry>,
        ) : this(
            staff = searchStaff,
            media = media,
            characters = searchStaff.characters?.nodes?.filterNotNull().orEmpty().distinctBy { it.id },
            favorites = searchStaff.favourites,
            occupations = searchStaff.primaryOccupations?.filterNotNull().orEmpty(),
        )

        constructor(
            userFavoritesStaff: UserFavoritesStaffQuery.Data.User.Favourites.Staff.Node,
            media: List<MediaEntry>,
        ) : this(
            staff = userFavoritesStaff,
            media = media,
            characters = userFavoritesStaff.characters?.nodes?.filterNotNull().orEmpty().distinctBy { it.id },
            favorites = userFavoritesStaff.favourites,
            occupations = userFavoritesStaff.primaryOccupations?.filterNotNull().orEmpty(),
        )

        class Provider<MediaEntry> :
            StaffEntryProvider<UserFavoritesStaffQuery.Data.User.Favourites.Staff.Node, Entry<MediaEntry>, MediaEntry> {
            override fun staffEntry(
                staff: UserFavoritesStaffQuery.Data.User.Favourites.Staff.Node,
                media: List<MediaEntry>,
            ) = Entry(userFavoritesStaff = staff, media = media)

            override fun id(entry: Entry<MediaEntry>) = entry.staff.id.toString()
            override fun media(entry: Entry<MediaEntry>) = entry.media
            override fun copyStaffEntry(
                entry: Entry<MediaEntry>,
                media: List<MediaEntry>,
            ) = entry.copy(media = media)
        }

        class SearchProvider<MediaEntry> :
            StaffEntryProvider<StaffSearchQuery.Data.Page.Staff, Entry<MediaEntry>, MediaEntry> {
            override fun staffEntry(
                staff: StaffSearchQuery.Data.Page.Staff,
                media: List<MediaEntry>,
            ) = Entry(searchStaff = staff, media = media)

            override fun id(entry: Entry<MediaEntry>) = entry.staff.id.toString()
            override fun media(entry: Entry<MediaEntry>) = entry.media
            override fun copyStaffEntry(
                entry: Entry<MediaEntry>,
                media: List<MediaEntry>,
            ) = entry.copy(media = media)
        }
    }
}
