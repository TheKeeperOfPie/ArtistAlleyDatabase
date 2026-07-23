package com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry

object ArtistInferenceUtils {

    fun mergeEntry(
        formEntry: ArtistDatabaseEntry.Impl,
        previousYearData: ArtistPreviousYearData,
        fieldState: Map<ArtistInferenceField, Boolean>,
    ) = formEntry.copy(
        name = previousYearData.name?.second.takeIf {
            fieldState[ArtistInferenceField.NAME] ?: false
        } ?: formEntry.name,
        socialLinks = previousYearData.socialLinks?.second.takeIf {
            fieldState[ArtistInferenceField.SOCIAL_LINKS] ?: false
        }.orEmpty(),
        storeLinks = previousYearData.storeLinks?.second.takeIf {
            fieldState[ArtistInferenceField.STORE_LINKS] ?: false
        }.orEmpty(),
        seriesInferred = previousYearData.series?.second.takeIf {
            fieldState[ArtistInferenceField.SERIES] ?: false
        }.orEmpty(),
        merchInferred = previousYearData.merch?.second.takeIf {
            fieldState[ArtistInferenceField.MERCH] ?: false
        }.orEmpty(),
    )
}
