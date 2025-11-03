package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

class MangaSearchSortFilterViewModel(
    aniListApi: AuthedAniListApi,
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    mediaTagsController: MediaTagsController,
    mediaDataSettings: MediaDataSettings,
    @Assisted initialParams: InitialParams<MediaSortOption>,
    @Assisted savedStateHandle: SavedStateHandle,
) : MangaSortFilterViewModel<MediaSortOption>(
    aniListApi = aniListApi,
    featureOverrideProvider = featureOverrideProvider,
    json = json,
    mediaGenresController = mediaGenresController,
    mediaLicensorsController = mediaLicensorsController,
    mediaTagsController = mediaTagsController,
    mediaDataSettings = mediaDataSettings,
    sortOptions = MutableStateFlow(MediaSortOption.entries.filter { it != MediaSortOption.EPISODES }),
    initialParams = initialParams,
    savedStateHandle = savedStateHandle,
) {
    @AssistedInject
    class TypedFactory(
        private val aniListApi: AuthedAniListApi,
        private val featureOverrideProvider: FeatureOverrideProvider,
        private val json: Json,
        private val mediaGenresController: MediaGenresController,
        private val mediaLicensorsController: MediaLicensorsController,
        private val mediaTagsController: MediaTagsController,
        private val mediaDataSettings: MediaDataSettings,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {
        fun create(initialParams: InitialParams<MediaSortOption>) =
            MangaSearchSortFilterViewModel(
                aniListApi = aniListApi,
                featureOverrideProvider = featureOverrideProvider,
                json = json,
                mediaGenresController = mediaGenresController,
                mediaLicensorsController = mediaLicensorsController,
                mediaTagsController = mediaTagsController,
                mediaDataSettings = mediaDataSettings,
                initialParams = initialParams,
                savedStateHandle = savedStateHandle,
            )

        @AssistedFactory
        interface Factory {
            fun create(savedStateHandle: SavedStateHandle) : TypedFactory
        }
    }
}
