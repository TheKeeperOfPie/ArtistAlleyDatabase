package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen.SectionIndexInfo
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditState
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption

@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
class MediaDetailsScreenTest {

    // TODO: Actually add tests, all existing ones were obsoleted by activity section refactor

    @Composable
    private fun MediaDetailsScreen(
        viewer: AniListViewer?,
    ) {
        val state = AnimeMediaDetailsScreen.State("12345")
        val mediaEditState = MediaEditState()
        AnimeMediaDetailsScreen(
            upIconOption = UpIconOption.Back {},
            onRefresh = {},
            headerValues = MediaHeaderValues(null, { null }, { null }),
            viewer = { viewer },
            hasAuth = { viewer != null },
            state = state,
            sharedTransitionKey = null,
            coverImageState = null,
            charactersCount = { 0 },
            charactersSection = {},
            staffCount = { 0 },
            staffSection = {},
            songsSectionMetadata = null,
            songsSection = TODO(),//{ _, _ -> },
            cdsSectionMetadata = null,
            cdsSection = TODO(),//{},
            recommendationsSectionMetadata = SectionIndexInfo.SectionMetadata.Empty,
            recommendationsSection = TODO(),//{ _, _, _ -> },
            activitiesSectionMetadata = SectionIndexInfo.SectionMetadata.Empty,
            activitiesSection = TODO(),//{ _, _, _ -> },
            forumThreadsSectionMetadata = SectionIndexInfo.SectionMetadata.Empty,
            forumThreadsSection = TODO(),//{ _, _ -> },
            reviewsSectionMetadata = SectionIndexInfo.SectionMetadata.Empty,
            reviewsSection = TODO(),//{ _, _ -> },
            editState = { mediaEditState },
            editEventSink = {},
            onClickListEdit = {},
            onFavoriteChanged = {},
        )
    }
}
