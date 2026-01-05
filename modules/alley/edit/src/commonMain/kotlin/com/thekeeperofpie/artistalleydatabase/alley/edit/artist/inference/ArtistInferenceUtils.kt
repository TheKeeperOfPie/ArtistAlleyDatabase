package com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry

object ArtistInferenceUtils {

    fun mergeEntry(
        formEntry: ArtistDatabaseEntry.Impl,
        previousYearData: ArtistPreviousYearData,
        fieldState: Map<ArtistInferenceField, Boolean>,
    ) = formEntry.copy(
        name = previousYearData.name.takeIf {
            fieldState[ArtistInferenceField.NAME] ?: false
        } ?: formEntry.name,
        socialLinks = previousYearData.socialLinks.takeIf {
            fieldState[ArtistInferenceField.SOCIAL_LINKS] ?: false
        }.orEmpty(),
        storeLinks = previousYearData.storeLinks.takeIf {
            fieldState[ArtistInferenceField.STORE_LINKS] ?: false
        }.orEmpty(),
        seriesInferred = previousYearData.seriesInferred.takeIf {
            fieldState[ArtistInferenceField.SERIES] ?: false
        }.orEmpty(),
        merchInferred = previousYearData.merchInferred.takeIf {
            fieldState[ArtistInferenceField.MERCH] ?: false
        }.orEmpty(),
    )
}
