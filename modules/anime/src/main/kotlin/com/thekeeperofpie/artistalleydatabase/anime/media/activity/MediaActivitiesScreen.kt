package com.thekeeperofpie.artistalleydatabase.anime.media.activity

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.MediaActivityQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ListActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndMediaListScreen
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

object MediaActivitiesScreen {

    private val SCREEN_KEY = AnimeNavDestinations.MEDIA_ACTIVITIES.id

    @Composable
    operator fun invoke(
        viewModel: MediaActivitiesViewModel,
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
    ) {
        val entry = viewModel.entry
        val media = entry?.data?.media
        var coverImageWidthToHeightRatio by remember {
            mutableFloatStateOf(headerValues.coverImageWidthToHeightRatio)
        }

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        val viewer by viewModel.viewer.collectAsState()
        HeaderAndMediaListScreen(
            screenKey = SCREEN_KEY,
            viewModel = viewModel,
            editViewModel = editViewModel,
            headerTextRes = R.string.anime_media_activities_header,
            header = {
                MediaHeader(
                    screenKey = SCREEN_KEY,
                    upIconOption = upIconOption,
                    mediaId = viewModel.headerId,
                    mediaType = viewModel.entry?.data?.media?.type,
                    titles = entry?.titlesUnique,
                    averageScore = media?.averageScore,
                    popularity = media?.popularity,
                    progress = it,
                    headerValues = headerValues,
                    onFavoriteChanged = {
                        viewModel.favoritesToggleHelper.set(
                            headerValues.type.toFavoriteType(),
                            viewModel.headerId,
                            it,
                        )
                    },
                    enableCoverImageSharedElement = false,
                    onImageWidthToHeightRatioAvailable = {
                        coverImageWidthToHeightRatio = it
                    }
                )
            },
            itemKey = { it.activityId },
            item = {
                ListActivitySmallCard(
                    screenKey = SCREEN_KEY,
                    viewer = viewer,
                    activity = it?.activity,
                    entry = it,
                    onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                    onClickListEdit = { editViewModel.initialize(it.media) },
                    clickable = true,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            },
        )
    }

    data class Entry(
        val data: MediaActivityQuery.Data,
    ) {
        val titlesUnique = data.media?.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()
    }
}
